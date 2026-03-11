package com.example.orderprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        // Test users are created via @Sql
    }

    @Test
    @WithUserDetails("customer")
    void createOrder_thenGetById() throws Exception {
        Long id = createSampleOrder();

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithUserDetails("admin")
    void listOrdersFilteredByStatus() throws Exception {
        createSampleOrder();
        Long processingId = createSampleOrder();

        mockMvc.perform(patch("/api/orders/{id}/status", processingId)
                        .param("status", "PROCESSING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));

        MvcResult pendingResult = mockMvc.perform(get("/api/orders").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andReturn();

        String json = pendingResult.getResponse().getContentAsString();
        Map<?, ?> pageResponse = objectMapper.readValue(json, Map.class);
        List<?> pendingOrders = (List<?>) pageResponse.get("content");
        assertThat(pendingOrders).isNotEmpty();

        MvcResult processingResult = mockMvc.perform(get("/api/orders").param("status", "PROCESSING"))
                .andExpect(status().isOk())
                .andReturn();
        String jsonProcessing = processingResult.getResponse().getContentAsString();
        Map<?, ?> processingPageResponse = objectMapper.readValue(jsonProcessing, Map.class);
        List<?> processingOrders = (List<?>) processingPageResponse.get("content");
        assertThat(processingOrders).isNotEmpty();
    }

    @Test
    @WithUserDetails("customer")
    void cancelOrder_onlyWhenPending() throws Exception {
        Long id = createSampleOrder();

        mockMvc.perform(post("/api/orders/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(post("/api/orders/{id}/cancel", id))
                .andExpect(status().isConflict());
    }

    private Long createSampleOrder() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("shippingAddress", "123 Test Street");

        Map<String, Object> item = new HashMap<>();
        item.put("productId", 1);
        item.put("quantity", 2);

        body.put("items", List.of(item));

        String json = objectMapper.writeValueAsString(body);

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Map<?, ?> created = objectMapper.readValue(response, Map.class);
        Object idValue = created.get("id");
        assertThat(idValue).isNotNull();
        return Long.valueOf(String.valueOf(idValue));
    }
}

