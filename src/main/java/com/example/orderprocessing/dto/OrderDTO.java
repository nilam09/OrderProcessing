package com.example.orderprocessing.dto;

import com.example.orderprocessing.model.Order;
import com.example.orderprocessing.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

public class OrderDTO {
    private Long id;
    private OrderStatus status;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderProductDTO> products;
    private BigDecimal totalAmount;

    public OrderDTO() {}

    public OrderDTO(Order order, boolean includeProducts) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.customerName = order.getUser() != null ? order.getUser().getName() : null;
        this.customerEmail = order.getUser() != null ? order.getUser().getEmail() : null;
        this.shippingAddress = order.getShippingAddress();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
        if (order.getProducts() != null) {
            this.products = order.getProducts().stream()
                .map(item -> new OrderProductDTO(item, includeProducts))
                .collect(Collectors.toList());
        }
        this.totalAmount = order.getTotalAmount();
    }

    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getShippingAddress() { return shippingAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<OrderProductDTO> getProducts() { return products; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
