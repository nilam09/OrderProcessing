package com.example.orderprocessing.dto;

import com.example.orderprocessing.model.Order;
import com.example.orderprocessing.model.OrderStatus;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class OrderSummaryDTO {
    private Long id;
    private OrderStatus status;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal totalAmount;

    public OrderSummaryDTO() {}

    public OrderSummaryDTO(Order order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.customerName = order.getUser() != null ? order.getUser().getName() : null;
        this.customerEmail = order.getUser() != null ? order.getUser().getEmail() : null;
        this.shippingAddress = order.getShippingAddress();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
        this.totalAmount = order.getTotalAmount();
    }

    public Long getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
