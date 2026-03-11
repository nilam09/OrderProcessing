package com.example.orderprocessing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateOrderRequest {

    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address must be less than 500 characters")
    private String shippingAddress;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<CreateOrderProductRequest> items;

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<CreateOrderProductRequest> getItems() {
        return items;
    }

    public void setItems(List<CreateOrderProductRequest> items) {
        this.items = items;
    }
}

