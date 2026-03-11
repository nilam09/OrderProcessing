package com.example.orderprocessing.controller;

import com.example.orderprocessing.dto.CreateOrderRequest;
import com.example.orderprocessing.dto.OrderDTO;
import com.example.orderprocessing.model.Order;
import com.example.orderprocessing.dto.OrderSummaryDTO;
import com.example.orderprocessing.dto.ProductDTO;
import com.example.orderprocessing.model.OrderStatus;
import com.example.orderprocessing.model.Product;
import com.example.orderprocessing.model.User;
import com.example.orderprocessing.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        User currentUser = getCurrentUser();
        Order created = orderService.createOrder(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id,
                                      @RequestParam(name = "expand", required = false) String expand) {
        boolean wantProduct = expand != null && expand.contains("products");
        return orderService.getOrder(id)
                .map(order -> {
                    if (wantProduct) {
                        com.example.orderprocessing.dto.OrderDTO dto = orderService.toOrderDTO(order, true);
                        return ResponseEntity.ok(dto);
                    } else {
                        OrderSummaryDTO dto = orderService.toOrderSummaryDTO(order);
                        return ResponseEntity.ok(dto);
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @RequestMapping(value = "/{id}/products", method = RequestMethod.GET, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<java.util.List<ProductDTO>> getOrderProducts(@PathVariable Long id) {
        try {
            java.util.List<Product> products = orderService.getProductsForOrder(id);
            java.util.List<ProductDTO> dtos = products.stream().map(ProductDTO::new).toList();
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<?> listOrders(
            @RequestParam(name = "status", required = false) OrderStatus status,
            @RequestParam(name = "expand", required = false) String expand,
            @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        User currentUser = getCurrentUser();
        boolean wantProduct = expand != null && expand.contains("products");
        Page<Order> orders = orderService.listOrders(Optional.ofNullable(status), currentUser, pageable);
        if (wantProduct) {
            Page<OrderDTO> dtos = orders.map(o -> orderService.toOrderDTO(o, true));
            return ResponseEntity.ok(dtos);
        } else {
            Page<OrderSummaryDTO> dtoPage = orders.map(OrderSummaryDTO::new);
            return ResponseEntity.ok(dtoPage);
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id,
                                                   @RequestParam("status") OrderStatus status) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != com.example.orderprocessing.model.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Order> updated = orderService.updateStatus(id, status);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        Optional<Order> cancelled = orderService.cancelOrder(id);
        if (cancelled.isEmpty()) {
            // Could be not found or not in PENDING status
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok(cancelled.get());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }

    @GetMapping("/user")
    public ResponseEntity<User> getCurrentUserDetails() {
        User user = getCurrentUser();
        return ResponseEntity.ok(user);
    }
}

