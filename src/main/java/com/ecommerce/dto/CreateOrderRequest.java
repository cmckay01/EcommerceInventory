package com.ecommerce.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private String customerEmail;
    private Long warehouseId;
    private List<OrderItemRequest> items;
}