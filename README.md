# E-Commerce Inventory Management System

A scalable inventory management system built with Java and Spring Boot, featuring multi-warehouse support, real-time stock tracking, order processing, and ACID-compliant transactions.

## Features

- **Multi-Warehouse Support**: Track inventory across multiple warehouse locations
- **Real-time Stock Management**: Immediate updates with inventory reservation system
- **Order Processing**: Complete order lifecycle from creation to shipment
- **Stock Reservation**: Reserve inventory during order confirmation
- **Transaction Safety**: JDBC-based operations with ACID compliance
- **Reorder Alerts**: Automatic detection of low-stock items
- **RESTful API**: Complete API for product catalog and order management

## Tech Stack

- Java 17
- Spring Boot 3.2.0
- MySQL
- Maven
- AWS SDK for Java (S3 integration)
- Lombok
- JUnit

## Architecture

### Models
- **Product**: Product catalog with SKU, pricing, categories
- **Warehouse**: Warehouse locations and details
- **Inventory**: Stock levels per product per warehouse
- **Order**: Customer orders with status tracking
- **OrderItem**: Individual line items in orders

### Services

#### InventoryService
- Create and manage inventory records
- Add/remove stock with validation
- Reserve stock for pending orders
- Release reservations on cancellation
- Confirm reservations when orders ship
- Track items needing reorder

#### OrderService
- Create orders with automatic inventory reservation
- Process orders and confirm stock deductions
- Ship orders and update status
- Cancel orders with automatic reservation release
- Transaction management across all operations

### API Endpoints

#### Inventory Management
```
GET    /api/inventory/{productId}/{warehouseId}       - Get inventory for specific location
GET    /api/inventory/product/{productId}             - Get all inventory for a product
GET    /api/inventory/product/{productId}/available   - Get total available stock
POST   /api/inventory                                 - Create inventory record
PUT    /api/inventory/{id}/add                        - Add stock
PUT    /api/inventory/{id}/remove                     - Remove stock
GET    /api/inventory/reorder                         - Get items needing reorder
```

#### Order Management
```
POST   /api/orders               - Create new order
GET    /api/orders/{id}          - Get order details
POST   /api/orders/{id}/process  - Process confirmed order
POST   /api/orders/{id}/ship     - Mark order as shipped
POST   /api/orders/{id}/cancel   - Cancel order
```

## Setup

1. **Prerequisites**
   - Java 17+
   - MySQL
   - Maven

2. **Database Setup**
```sql
CREATE DATABASE ecommerce;
```

3. **Configuration**
Update `application.yml` with your database credentials

4. **Build & Run**
```bash
mvn clean install
mvn spring-boot:run
```

## Usage Example

### Create Product and Warehouse
```bash
# Create a warehouse
curl -X POST http://localhost:8080/api/warehouses \
  -H "Content-Type: application/json" \
  -d '{
    "code": "WH001",
    "name": "East Coast Distribution",
    "city": "New York",
    "state": "NY"
  }'

# Create inventory
curl -X POST http://localhost:8080/api/inventory \
  -d "productId=1&warehouseId=1&initialQuantity=100"
```

### Process an Order
```bash
# Create order (automatically reserves inventory)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "customer@example.com",
    "warehouseId": 1,
    "items": [
      {"productId": 1, "quantity": 2}
    ]
  }'

# Process order (confirms reservation and deducts stock)
curl -X POST http://localhost:8080/api/orders/1/process

# Ship order
curl -X POST http://localhost:8080/api/orders/1/ship
```

## Key Implementation Details

### Transaction Management
- All service methods use `@Transactional` for data consistency
- Read-only transactions for query operations
- Automatic rollback on exceptions

### Inventory Reservation Flow
1. **Order Creation**: Stock reserved but not deducted
2. **Order Processing**: Reserved stock confirmed and deducted
3. **Order Cancellation**: Reserved stock released back to available

### JDBC Operations
- Custom JDBC queries in repositories for atomic stock operations
- Optimistic updates with row count validation
- Prevents race conditions in concurrent stock updates

### Stock Validation
- Availability checks before reservation
- Reserved quantity tracked separately from total quantity
- Available stock = Total stock - Reserved stock