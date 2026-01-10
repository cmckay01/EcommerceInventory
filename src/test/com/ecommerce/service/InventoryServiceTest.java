package com.ecommerce.service;

import com.ecommerce.model.Inventory;
import com.ecommerce.model.Product;
import com.ecommerce.model.Warehouse;
import com.ecommerce.repository.InventoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    
    @Mock
    private InventoryRepository inventoryRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private WarehouseRepository warehouseRepository;
    
    @InjectMocks
    private InventoryService inventoryService;
    
    private Product testProduct;
    private Warehouse testWarehouse;
    private Inventory testInventory;
    
    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .sku("TEST-001")
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .category("Electronics")
                .active(true)
                .build();
        
        testWarehouse = Warehouse.builder()
                .id(1L)
                .code("WH001")
                .name("Main Warehouse")
                .city("New York")
                .state("NY")
                .active(true)
                .build();
        
        testInventory = Inventory.builder()
                .id(1L)
                .product(testProduct)
                .warehouse(testWarehouse)
                .quantity(100)
                .reservedQuantity(0)
                .reorderLevel(10)
                .reorderQuantity(50)
                .build();
    }
    
    @Test
    void createInventory_ShouldCreateSuccessfully() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        
        // Act
        Inventory result = inventoryService.createInventory(1L, 1L, 100);
        
        // Assert
        assertNotNull(result);
        assertEquals(100, result.getQuantity());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }
    
    @Test
    void reserveStock_WhenSufficientStock_ShouldReturnTrue() {
        // Arrange
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.reserveStock(1L, 10)).thenReturn(1);
        
        // Act
        boolean result = inventoryService.reserveStock(1L, 1L, 10);
        
        // Assert
        assertTrue(result);
        verify(inventoryRepository, times(1)).reserveStock(1L, 10);
    }
    
    @Test
    void reserveStock_WhenInsufficientStock_ShouldReturnFalse() {
        // Arrange
        testInventory.setQuantity(5);
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(testInventory));
        
        // Act
        boolean result = inventoryService.reserveStock(1L, 1L, 10);
        
        // Assert
        assertFalse(result);
        verify(inventoryRepository, never()).reserveStock(anyLong(), anyInt());
    }
    
    @Test
    void addStock_WithPositiveQuantity_ShouldSucceed() {
        // Arrange
        doNothing().when(inventoryRepository).addStock(1L, 50);
        
        // Act
        inventoryService.addStock(1L, 50);
        
        // Assert
        verify(inventoryRepository, times(1)).addStock(1L, 50);
    }
    
    @Test
    void addStock_WithNegativeQuantity_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
                () -> inventoryService.addStock(1L, -10));
    }
    
    @Test
    void getTotalAvailableStock_ShouldSumAllLocations() {
        // Arrange
        Inventory inv1 = Inventory.builder()
                .quantity(100)
                .reservedQuantity(10)
                .build();
        Inventory inv2 = Inventory.builder()
                .quantity(50)
                .reservedQuantity(5)
                .build();
        
        when(inventoryRepository.findByProductId(1L))
                .thenReturn(java.util.Arrays.asList(inv1, inv2));
        
        // Act
        Integer total = inventoryService.getTotalAvailableStock(1L);
        
        // Assert
        assertEquals(135, total); // (100-10) + (50-5) = 135
    }
}