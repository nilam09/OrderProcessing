package com.example.orderprocessing.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate7.Hibernate7Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        Hibernate7Module hibernate7Module = new Hibernate7Module();
        hibernate7Module.configure(Hibernate7Module.Feature.FORCE_LAZY_LOADING, false);
        objectMapper.registerModule(hibernate7Module);
        return objectMapper;
    }
}