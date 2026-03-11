package com.example.orderprocessing.dto;

import com.example.orderprocessing.model.OrderProduct;

import java.math.BigDecimal;

public class OrderProductDTO {
    // product fields flattened under the item
    private Long id; // product id
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal totalPrice;

    // item-specific
    private Long productId;
    private int quantity;

    public OrderProductDTO() {}

    public OrderProductDTO(OrderProduct item, boolean includeProduct) {
        this.quantity = item.getQuantity() == null ? 0 : item.getQuantity();
        this.totalPrice = item.getTotalPrice();
        if (item.getProduct() != null) {
            Long pid = item.getProduct().getId();
            this.id = pid;
            this.productId = pid;
            if (includeProduct) {
                this.name = item.getProduct().getName();
                this.description = item.getProduct().getDescription();
                this.imageUrl = item.getProduct().getImageUrl();
                this.price = item.getProduct().getPrice();
            }
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public BigDecimal getPrice() { return price; }

    public BigDecimal getTotalPrice() { return totalPrice; }

    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }
}
