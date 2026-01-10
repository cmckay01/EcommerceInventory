package com.ecommerce.repository;

import com.ecommerce.model.Order;
import com.ecommerce.model.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByCustomerEmail(String customerEmail);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<Order> findByWarehouseId(Long warehouseId);
}