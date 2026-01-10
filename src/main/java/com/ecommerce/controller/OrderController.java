package com.ecommerce.controller;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.model.Order;
import com.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }
    
    @PostMapping("/{orderId}/process")
    public ResponseEntity<Order> processOrder(@PathVariable Long orderId) {
        Order order = orderService.processOrder(orderId);
        return ResponseEntity.ok(order);
    }
    
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<Order> shipOrder(@PathVariable Long orderId) {
        Order order = orderService.shipOrder(orderId);
        return ResponseEntity.ok(order);
    }
    
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long orderId) {
        Order order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(order);
    }
}