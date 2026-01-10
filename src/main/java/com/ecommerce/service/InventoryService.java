package com.ecommerce.service;

import com.ecommerce.model.Inventory;
import com.ecommerce.model.Product;
import com.ecommerce.model.Warehouse;
import com.ecommerce.repository.InventoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    
    @Transactional(readOnly = true)
    public Inventory getInventory(Long productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));
    }
    
    @Transactional(readOnly = true)
    public List<Inventory> getProductInventory(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }
    
    @Transactional(readOnly = true)
    public Integer getTotalAvailableStock(Long productId) {
        return inventoryRepository.findByProductId(productId).stream()
                .mapToInt(Inventory::getAvailableQuantity)
                .sum();
    }
    
    @Transactional
    public Inventory createInventory(Long productId, Long warehouseId, Integer initialQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));
        
        Inventory inventory = Inventory.builder()
                .product(product)
                .warehouse(warehouse)
                .quantity(initialQuantity)
                .build();
        
        return inventoryRepository.save(inventory);
    }
    
    @Transactional
    public void addStock(Long inventoryId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        inventoryRepository.addStock(inventoryId, quantity);
        log.info("Added {} units to inventory {}", quantity, inventoryId);
    }
    
    @Transactional
    public void removeStock(Long inventoryId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        int updated = inventoryRepository.removeStock(inventoryId, quantity);
        if (updated == 0) {
            throw new RuntimeException("Insufficient stock or inventory not found");
        }
        
        log.info("Removed {} units from inventory {}", quantity, inventoryId);
    }
    
    @Transactional
    public boolean reserveStock(Long productId, Long warehouseId, Integer quantity) {
        Inventory inventory = getInventory(productId, warehouseId);
        
        if (inventory.getAvailableQuantity() < quantity) {
            log.warn("Insufficient available stock for product {} in warehouse {}", productId, warehouseId);
            return false;
        }
        
        int updated = inventoryRepository.reserveStock(inventory.getId(), quantity);
        if (updated > 0) {
            log.info("Reserved {} units for product {} in warehouse {}", quantity, productId, warehouseId);
            return true;
        }
        
        return false;
    }
    
    @Transactional
    public void releaseReservedStock(Long productId, Long warehouseId, Integer quantity) {
        Inventory inventory = getInventory(productId, warehouseId);
        inventoryRepository.releaseReservedStock(inventory.getId(), quantity);
        log.info("Released {} reserved units for product {} in warehouse {}", quantity, productId, warehouseId);
    }
    
    @Transactional
    public void confirmReservation(Long productId, Long warehouseId, Integer quantity) {
        Inventory inventory = getInventory(productId, warehouseId);
        
        // Remove from both total quantity and reserved quantity
        inventoryRepository.removeStock(inventory.getId(), quantity);
        inventoryRepository.releaseReservedStock(inventory.getId(), quantity);
        
        log.info("Confirmed reservation and removed {} units for product {} in warehouse {}", 
                quantity, productId, warehouseId);
    }
    
    @Transactional(readOnly = true)
    public List<Inventory> getItemsNeedingReorder() {
        return inventoryRepository.findItemsNeedingReorder();
    }
}