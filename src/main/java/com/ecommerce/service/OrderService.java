package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerEmail());
        
        // Validate warehouse exists
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));
        
        // Create order
        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setWarehouse(warehouse);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Process each order item
        for (var itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));
            
            // Check and reserve inventory
            Inventory inventory = inventoryRepository
                    .findByProductIdAndWarehouseId(itemRequest.getProductId(), request.getWarehouseId())
                    .orElseThrow(() -> new RuntimeException("No inventory found for product in warehouse"));
            
            if (inventory.getAvailableQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient inventory for product: " + product.getName());
            }
            
            // Reserve inventory
            inventory.setReservedQuantity(inventory.getReservedQuantity() + itemRequest.getQuantity());
            inventoryRepository.save(inventory);
            
            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            
            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }
        
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        log.info("Created order {} with total amount: {}", savedOrder.getOrderNumber(), totalAmount);
        return savedOrder;
    }
    
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    @Transactional
    public Order confirmOrder(Long orderId) {
        Order order = getOrder(orderId);
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Order is not in PENDING status");
        }
        
        // Confirm all reserved inventory
        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), order.getWarehouse().getId())
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));
            
            // Deduct from actual quantity and reserved quantity
            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventory.setReservedQuantity(inventory.getReservedQuantity() - item.getQuantity());
            inventoryRepository.save(inventory);
        }
        
        order.setStatus(Order.OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order);
        
        log.info("Confirmed order: {}", order.getOrderNumber());
        return saved;
    }
    
    @Transactional
    public Order processOrder(Long orderId) {
        Order order = getOrder(orderId);
        
        if (order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new RuntimeException("Order must be CONFIRMED before processing");
        }
        
        order.setStatus(Order.OrderStatus.PROCESSING);
        Order saved = orderRepository.save(order);
        
        log.info("Processing order: {}", order.getOrderNumber());
        return saved;
    }
    
    @Transactional
    public Order shipOrder(Long orderId) {
        Order order = getOrder(orderId);
        
        if (order.getStatus() != Order.OrderStatus.PROCESSING) {
            throw new RuntimeException("Order must be PROCESSING before shipping");
        }
        
        order.setStatus(Order.OrderStatus.SHIPPED);
        Order saved = orderRepository.save(order);
        
        log.info("Shipped order: {}", order.getOrderNumber());
        return saved;
    }
    
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        
        if (order.getStatus() == Order.OrderStatus.SHIPPED) {
            throw new RuntimeException("Cannot cancel shipped order");
        }
        
        // Release all reserved inventory
        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), order.getWarehouse().getId())
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));
            
            inventory.setReservedQuantity(inventory.getReservedQuantity() - item.getQuantity());
            
            // If order was confirmed, add back to quantity
            if (order.getStatus() == Order.OrderStatus.CONFIRMED || 
                order.getStatus() == Order.OrderStatus.PROCESSING) {
                inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
            }
            
            inventoryRepository.save(inventory);
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        
        log.info("Cancelled order: {}", order.getOrderNumber());
        return saved;
    }
}