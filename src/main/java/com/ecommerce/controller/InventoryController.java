package com.ecommerce.controller;

import com.ecommerce.model.Inventory;
import com.ecommerce.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @GetMapping("/{productId}/{warehouseId}")
    public ResponseEntity<Inventory> getInventory(
            @PathVariable Long productId,
            @PathVariable Long warehouseId) {
        Inventory inventory = inventoryService.getInventory(productId, warehouseId);
        return ResponseEntity.ok(inventory);
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Inventory>> getProductInventory(@PathVariable Long productId) {
        List<Inventory> inventories = inventoryService.getProductInventory(productId);
        return ResponseEntity.ok(inventories);
    }
    
    @GetMapping("/product/{productId}/available")
    public ResponseEntity<Integer> getTotalAvailableStock(@PathVariable Long productId) {
        Integer total = inventoryService.getTotalAvailableStock(productId);
        return ResponseEntity.ok(total);
    }
    
    @PostMapping
    public ResponseEntity<Inventory> createInventory(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Integer initialQuantity) {
        Inventory inventory = inventoryService.createInventory(productId, warehouseId, initialQuantity);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventory);
    }
    
    @PutMapping("/{inventoryId}/add")
    public ResponseEntity<Void> addStock(
            @PathVariable Long inventoryId,
            @RequestParam Integer quantity) {
        inventoryService.addStock(inventoryId, quantity);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{inventoryId}/remove")
    public ResponseEntity<Void> removeStock(
            @PathVariable Long inventoryId,
            @RequestParam Integer quantity) {
        inventoryService.removeStock(inventoryId, quantity);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/reorder")
    public ResponseEntity<List<Inventory>> getItemsNeedingReorder() {
        List<Inventory> items = inventoryService.getItemsNeedingReorder();
        return ResponseEntity.ok(items);
    }
}