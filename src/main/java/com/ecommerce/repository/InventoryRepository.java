package com.ecommerce.repository;

import com.ecommerce.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
    
    List<Inventory> findByProductId(Long productId);
    
    List<Inventory> findByWarehouseId(Long warehouseId);
    
    @Query("SELECT i FROM Inventory i WHERE i.quantity - i.reservedQuantity <= i.reorderLevel")
    List<Inventory> findItemsNeedingReorder();
    
    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity + :quantity WHERE i.id = :id")
    void addStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity - :quantity WHERE i.id = :id AND i.quantity >= :quantity")
    int removeStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity + :quantity WHERE i.id = :id AND (i.quantity - i.reservedQuantity) >= :quantity")
    int reserveStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    @Modifying
    @Query("UPDATE Inventory i SET i.reservedQuantity = i.reservedQuantity - :quantity WHERE i.id = :id")
    void releaseReservedStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}