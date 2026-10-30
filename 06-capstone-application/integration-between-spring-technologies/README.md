# Integration Between Spring Technologies

This example demonstrates how multiple technologies in the Spring ecosystem work together to build a realistic Order Management Application.

It combines Spring Boot, Spring Data JPA, Spring Web, Spring Security, Spring transactions, and Spring Modulith to demonstrate how a modular Spring application can handle product catalog management, inventory reservation, order placement, and audit recording.

The focus is on integration between Spring technologies rather than demonstrating each technology in isolation.

## What This Example Demonstrates

- Building a modular application with Spring Boot.
- Organizing application functionality using Spring Modulith.
- Defining module boundaries and dependencies.
- Exposing REST APIs using Spring Web.
- Persisting application data using Spring Data JPA.
- Mapping entities and relationships using Hibernate.
- Configuring HTTP Basic authentication with Spring Security.
- Applying role-based authorization.
- Managing database transactions with `@Transactional`.
- Coordinating operations across application services.
- Publishing and handling application events.
- Recording audit information after successful transactions.
- Handling application errors using `@RestControllerAdvice`.
- Validating REST requests using Jakarta Bean Validation.
- Testing the integration of Spring technologies using Spring Boot Test and MockMvc.
- Verifying application module boundaries using Spring Modulith.

---

## Application Overview

The example implements a small Order Management Application.

Customers can browse available products and place orders. When an order is placed, the application checks product availability, reserves inventory, persists the order, and publishes an application event.

An audit listener receives the event after the order transaction commits and records the operation.

Administrators can access inventory information, while customers can only access their own orders.

The application uses an H2 in-memory database, allowing it to run without an external database installation.

### Main Application Capabilities

| Capability             | Description                                                            |
|------------------------|------------------------------------------------------------------------|
| Product Catalog        | Browse available products.                                             |
| Inventory Management   | Retrieve available stock quantities.                                   |
| Order Management       | Place orders and retrieve customer orders.                             |
| Authentication         | Authenticate customers and administrators.                             |
| Authorization          | Restrict access based on user roles.                                   |
| Transaction Management | Ensure order placement and stock reservation succeed or fail together. |
| Event Handling         | Publish and handle order placement events.                             |
| Audit Logging          | Record successful order placement operations.                          |

---

## Architecture

The application follows a modular architecture using Spring Modulith.

Each module represents a distinct business capability.

```text
IntegrationApplication
        |
        +----------------------+
        |                      |
        ▼                      ▼
   Catalog Module         Inventory Module
        |                      |
        |                      |
        ▼                      ▼
 ProductRepository       InventoryRepository
        |                      |
        ▼                      ▼
   Product Entity         InventoryItem Entity


        Ordering Module
              |
              +----------------------+
              |                      |
              ▼                      ▼
        ProductService         InventoryService
              |                      |
              +----------+-----------+
                         |
                         ▼
                   OrderService
                         |
              +----------+-----------+
              |                      |
              ▼                      ▼
        OrderRepository       ApplicationEventPublisher
              |                      |
              ▼                      ▼
          Order Entity         OrderPlacedEvent
                                     |
                                     ▼
                                Audit Module
                                     |
                                     ▼
                              OrderAuditListener
                                     |
                                     ▼
                               AuditRepository
                                     |
                                     ▼
                                 AuditEntry
```

The diagram illustrates the main dependencies and interactions between the modules.

The modules are:

- `catalog`: manages product information.
- `inventory`: manages available stock.
- `ordering`: manages customer orders and publishes order placement events.
- `audit`: records audit entries in response to order placement events.

The `config` package contains application-level configuration, security configuration, data initialization, and exception handling.

---

## Spring Boot Application

Spring Boot provides the foundation for the application.

The main application class bootstraps the application, enables component scanning, and activates the Spring Boot auto-configuration mechanism.

```java
@SpringBootApplication
@Modulithic
public class IntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationApplication.class, args);
    }
}
```

### `@SpringBootApplication`

`@SpringBootApplication` combines three annotations:

- `@SpringBootConfiguration`
- `@EnableAutoConfiguration`
- `@ComponentScan`

This allows Spring Boot to configure the application and discover its components.

### `@Modulithic`

`@Modulithic` enables Spring Modulith's application module model.

Spring Modulith uses the application's package structure to identify modules and their relationships.

In this example, the top-level business packages represent application modules.

---

## Modular Architecture with Spring Modulith

Spring Modulith helps structure a Spring Boot application around business capabilities.

Instead of organizing the application entirely around technical layers, functionality is grouped into modules that represent distinct areas of the business.


### Declaring Application Modules

Each module declares its identity using `@ApplicationModule`.

For example:

```java
@ApplicationModule(displayName = "Catalog")
package com.springbyexample.v2.integrationbetweenspringtechnologies.catalog;

import org.springframework.modulith.ApplicationModule;
```

The same approach is used for the Inventory, Ordering, and Audit modules.

### Module Responsibilities

| Module    | Responsibility                               |
|-----------|----------------------------------------------|
| Catalog   | Product information and product retrieval.   |
| Inventory | Stock availability and stock reservation.    |
| Ordering  | Order creation, retrieval, and order events. |
| Audit     | Recording application audit entries.         |

### Module Interactions

The Ordering module coordinates the order placement process.

It depends on the public services exposed by the Catalog and Inventory modules.

The Audit module listens for an event published by the Ordering module.

This creates a separation between the business operation and its audit handling.

```text
Ordering
   |
   +----> Catalog
   |
   +----> Inventory
   |
   +----> Publishes OrderPlacedEvent
                       |
                       ▼
                     Audit
```

This demonstrates how modules can collaborate without exposing their internal implementation details.

---

## Product Catalog

The Catalog module manages product information.

Products are persisted using Spring Data JPA.

### Product Entity

The `Product` entity represents a product available for purchase.

```java
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal price;

    private boolean active;
}
```

The product contains:

- `id`: unique product identifier.
- `name`: product name.
- `price`: product price.
- `active`: indicates whether the product is available for purchase.

The application uses `BigDecimal` for monetary values to avoid the precision problems associated with floating-point arithmetic.

### Product Repository

The repository provides database access through Spring Data JPA.

```java
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByActiveTrue();

    Optional<Product> findByIdAndActiveTrue(Long id);
}
```

Spring Data JPA derives the required queries from the repository method names.

There is no need to implement the basic persistence operations manually.

### Product Service

The service exposes product retrieval operations to other application components.

```java
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProducts() {
        return productRepository.findAllByActiveTrue();
    }

    public Product getProduct(Long productId) {
        return productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
```

The service provides a public interface through which other modules can retrieve product information.

The read-only transaction indicates that these operations are not intended to modify persistent data.

---

## Inventory Management

The Inventory module manages the available quantity of each product.

It is responsible for checking stock availability and reserving stock during order placement.

### Inventory Entity

```java
@Entity
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long productId;

    private int availableQuantity;

    @Version
    private Long version;
}
```

The inventory entity contains:

- `id`: unique inventory record identifier.
- `productId`: identifies the associated product.
- `availableQuantity`: current available stock.
- `version`: version used for optimistic locking.

The `@Version` annotation enables optimistic locking through JPA.

The repository also provides a pessimistic locking query for stock reservation.

### Inventory Repository

```java
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select inventory
            from InventoryItem inventory
            where inventory.productId = :productId
            """)
    Optional<InventoryItem> findByProductIdForUpdate(Long productId);
}
```

The pessimistic write lock prevents concurrent transactions from modifying the same inventory record simultaneously while a stock reservation is being performed.

This helps protect stock quantities when multiple customers attempt to order the same product.

### Reserving Stock

The Inventory service provides the stock reservation operation.

```java
@Transactional
public void reserveStock(Long productId, int quantity) {
    InventoryItem inventory = inventoryRepository.findByProductIdForUpdate(productId)
            .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product: " + productId));

    inventory.reserve(quantity);
}
```

The entity performs the actual stock validation and quantity update.

If the requested quantity exceeds the available stock, an `InsufficientStockException` is thrown.

Because stock reservation participates in the order placement transaction, the update can be rolled back if a later operation fails.

---

## Order Management

The Ordering module coordinates the order placement process.

It interacts with the Catalog and Inventory modules, persists order information, and publishes an event after the order has been created.

### Order Entity

The `Order` entity represents a customer's order.

It contains:

- `id`: unique order identifier.
- `customerUsername`: username of the customer placing the order.
- `items`: products included in the order.
- `totalAmount`: total order amount.
- `status`: current order status.
- `createdAt`: order creation timestamp.

The order items are stored using JPA's `@ElementCollection` mapping.

### Order Item

An order item stores a snapshot of the product information at the time of ordering.

```java
@Embeddable
public class OrderItem {

    private Long productId;

    private String productName;

    private int quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;
}
```

Storing the product name and unit price in the order item preserves the information associated with the order even if the product's details change later.

### Order Status

The example defines an order status:

```java
public enum OrderStatus {
    PLACED
}
```

The status indicates that the order has been successfully placed.

The example intentionally keeps the order lifecycle small rather than introducing fulfillment, shipment, payment, and cancellation workflows.

### Order Repository

```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByCustomerUsernameOrderByCreatedAtDesc(String customerUsername);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findById(Long orderId);
}
```

The entity graph ensures that the order items are fetched when orders are retrieved.

This is important because the application disables Open EntityManager in View.

Without fetching the lazy collection within the repository query, serialization of an order response could fail after the persistence session has closed.

---

## Placing an Order

The central business operation is implemented by `OrderService`.

The service coordinates product retrieval, inventory reservation, order persistence, and event publication.

A simplified version of the operation is:

```java
@Transactional
public Order placeOrder(String customerUsername, Long productId, int quantity) {

    if (quantity <= 0) {
        throw new IllegalArgumentException("Quantity must be greater than zero");
    }

    Product product = productService.getProduct(productId);

    inventoryService.reserveStock(productId, quantity);

    BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));

    OrderItem item = new OrderItem(product.getId(), product.getName(), quantity, product.getPrice(), subtotal);

    Order order = new Order(customerUsername, List.of(item), subtotal, OrderStatus.PLACED);

    Order savedOrder = orderRepository.save(order);

    eventPublisher.publishEvent(new OrderPlacedEvent(savedOrder.getId(), customerUsername, savedOrder.getTotalAmount(),
            savedOrder.getCreatedAt()));

    return savedOrder;
}
```

This method demonstrates how multiple Spring-managed components can participate in one business operation.

### Order Placement Flow

```text
Customer Request
       |
       ▼
OrderController
       |
       ▼
OrderService
       |
       +----------------------+
       |                      |
       ▼                      ▼
 ProductService        InventoryService
       |                      |
       ▼                      ▼
 Retrieve Product      Lock Inventory
       |                      |
       |                      ▼
       |                Check Available Stock
       |                      |
       |                      ▼
       |                Reserve Stock
       |                      |
       +----------+-----------+
                  |
                  ▼
             Create Order
                  |
                  ▼
          Save Order to Database
                  |
                  ▼
         Publish OrderPlacedEvent
                  |
                  ▼
          Commit Transaction
```

The important point is that order creation and stock reservation participate in the same transaction.

---

## Transaction Management

Transaction management is essential when a business operation involves multiple database changes.

In this example, placing an order requires:

1. Retrieving the product.
2. Reserving the required inventory.
3. Creating the order.
4. Persisting the order.
5. Publishing an application event.

The `@Transactional` annotation on `OrderService.placeOrder()` establishes the transaction boundary.

```java
@Transactional
public Order placeOrder(...) {
    // Retrieve product.
    // Reserve inventory.
    // Create and save order.
    // Publish event.
}
```

### Successful Transaction

When all operations succeed, the transaction commits.

```text
Begin Transaction
       |
       ▼
Retrieve Product
       |
       ▼
Reserve Stock
       |
       ▼
Save Order
       |
       ▼
Publish Event
       |
       ▼
Commit Transaction
```

The order is persisted and the inventory quantity is reduced.

### Failed Transaction

If an operation fails, the transaction is rolled back.

For example, if a customer requests more stock than is available:

```text
Begin Transaction
       |
       ▼
Retrieve Product
       |
       ▼
Reserve Stock
       |
       ▼
InsufficientStockException
       |
       ▼
Rollback Transaction
       |
       ▼
No Order Created
Stock Unchanged
```

This prevents the application from creating an order without successfully reserving the required inventory.

The example demonstrates transaction coordination across services belonging to different application modules.

---

## Application Events

Spring's application event mechanism allows components to communicate through events rather than direct method calls.

The Ordering module publishes an `OrderPlacedEvent` after saving an order.

### OrderPlacedEvent

```java
public record OrderPlacedEvent(Long orderId, String customerUsername, BigDecimal totalAmount, Instant occurredAt) {
}
```

The event contains the information needed by interested components.

The Ordering module does not need to know how the Audit module processes the event.

### Publishing the Event

The event is published using Spring's `ApplicationEventPublisher`.

```java
eventPublisher.publishEvent(new OrderPlacedEvent(savedOrder.getId(),customerUsername,savedOrder.getTotalAmount(),
        savedOrder.getCreatedAt()));
```

Publishing an event allows other components to react to the operation without requiring the Ordering module to invoke them directly.

---

## Handling Events After Transaction Commit

The Audit module listens for order placement events.

It uses `@TransactionalEventListener` with `TransactionPhase.AFTER_COMMIT`.

```java
@Component
public class OrderAuditListener {

    private final AuditRepository auditRepository;

    public OrderAuditListener(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(OrderPlacedEvent event) {

        AuditEntry entry = new AuditEntry("ORDER_PLACED", event.orderId(), "Order placed by " + event.customerUsername()
                + " with total amount " + event.totalAmount(), event.occurredAt());

        auditRepository.save(entry);
    }
}
```

### Why `AFTER_COMMIT`?

The `AFTER_COMMIT` phase ensures that the listener runs after the original order transaction has successfully committed.

This means the audit entry is not created when the order transaction rolls back.

Conceptually:

```text
Order Transaction
       |
       ▼
Create Order
       |
       ▼
Reserve Inventory
       |
       ▼
Publish OrderPlacedEvent
       |
       ▼
Commit Transaction
       |
       ▼
AFTER_COMMIT Listener
       |
       ▼
Save Audit Entry
```

### Why `REQUIRES_NEW`?

The original order transaction has already committed when the event listener executes.

The listener therefore uses `Propagation.REQUIRES_NEW` to execute the audit persistence operation in a separate transaction.

This allows the audit entry to be persisted independently of the completed order transaction.

The example demonstrates transaction-bound event handling, but it does not implement a durable event publication mechanism or guarantee recovery if audit processing fails.

---

## Audit Module

The Audit module records important business operations.

For this example, it records successful order placement events.

### Audit Entity

```java
@Entity
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType;

    private Long aggregateId;

    private String description;

    private Instant occurredAt;
}
```

The audit entry stores:

- `eventType`: type of operation.
- `aggregateId`: identifier of the affected order.
- `description`: human-readable description.
- `occurredAt`: time the event occurred.

The Audit module listens to events and persists audit entries without being directly invoked by the OrderService.

---

## REST API

Spring Web exposes the application's functionality through REST endpoints.

The application provides endpoints for products, inventory, and orders.

### Product Endpoints

| Method | Endpoint                    | Description                  | Authentication |
|--------|-----------------------------|------------------------------|----------------|
| GET    | `/api/products`             | Retrieve available products. | Public         |
| GET    | `/api/products/{productId}` | Retrieve a product.          | Public         |

### Inventory Endpoints

| Method | Endpoint                     | Description               | Authorization |
|--------|------------------------------|---------------------------|---------------|
| GET    | `/api/inventory/{productId}` | Retrieve available stock. | ADMIN         |

### Order Endpoints

| Method | Endpoint                | Description                                                | Authorization |
|--------|-------------------------|------------------------------------------------------------|---------------|
| POST   | `/api/orders`           | Place an order.                                            | CUSTOMER      |
| GET    | `/api/orders`           | Retrieve the authenticated customer's orders.              | CUSTOMER      |
| GET    | `/api/orders/{orderId}` | Retrieve an order belonging to the authenticated customer. | CUSTOMER      |

### Placing an Order

A customer submits a request containing a product identifier and quantity.

Example:

```http
POST /api/orders
Content-Type: application/json
Authorization: Basic <credentials>
```

Request body:

```json
{
  "productId": 1,
  "quantity": 2
}
```

The application validates the request, retrieves the product, reserves inventory, and creates the order.

### PlaceOrderRequest

```java
public record PlaceOrderRequest(@NotNull Long productId, @Positive int quantity) {
}
```

The validation annotations enforce basic request constraints.

`@NotNull` ensures that a product identifier is supplied, while `@Positive` ensures that the requested quantity is greater than zero.

The controller uses `@Valid` to trigger request validation.

---

## Spring Security

Spring Security protects the application's endpoints using HTTP Basic authentication.

The example defines two users:

| Username | Password | Role     |
|----------|----------|----------|
| customer | password | CUSTOMER |
| admin    | password | ADMIN    |

These are demonstration credentials configured using an in-memory user store.

### Security Configuration

The security configuration defines authentication and authorization rules.

Conceptually:

```java
http
    .authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/h2-console/**").permitAll()
        .requestMatchers("/api/products/**").permitAll()
        .requestMatchers("/api/orders/**").hasRole("CUSTOMER")
        .requestMatchers("/api/inventory/**").hasRole("ADMIN")
        .anyRequest().authenticated()
    )
    .httpBasic(Customizer.withDefaults());
```

The complete configuration also disables CSRF for this API-focused example and adjusts frame options for the H2 console.

### Authorization Rules

The application applies different access rules depending on the requested resource.

```text
                    HTTP Request
                         |
                         ▼
                  Spring Security
                         |
             +-----------+-----------+
             |           |           |
             ▼           ▼           ▼
          Products     Orders     Inventory
             |           |           |
             ▼           ▼           ▼
          Public     CUSTOMER      ADMIN
```

### Customer Order Ownership

Authorization is not limited to checking whether a user has the CUSTOMER role.

The OrderService also ensures that a customer can retrieve only their own orders.

```java
public Order getOrder(Long orderId, String customerUsername) {
    return orderRepository.findById(orderId).filter(order -> order.getCustomerUsername().equals(customerUsername))
            .orElseThrow(() -> new OrderNotFoundException(orderId));
}
```

This prevents one customer from retrieving another customer's order.

The example returns a not-found response when the requested order does not exist or does not belong to the authenticated customer.

---

## Error Handling

The application uses `@RestControllerAdvice` to handle application exceptions centrally.

The exception handler translates application errors into appropriate HTTP responses.

| Exception                    | HTTP Status     | Meaning                                                         |
|------------------------------|-----------------|-----------------------------------------------------------------|
| `ProductNotFoundException`   | 404 Not Found   | Requested product does not exist or is unavailable.             |
| `OrderNotFoundException`     | 404 Not Found   | Requested order does not exist or is not owned by the customer. |
| `InsufficientStockException` | 409 Conflict    | Requested quantity exceeds available inventory.                 |
| `IllegalArgumentException`   | 400 Bad Request | Request contains an invalid value.                              |

This keeps exception-to-HTTP response mapping separate from business logic.

---

## Database Configuration

The example uses H2 as an in-memory relational database.

The configuration is defined in `application.yml`.

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:ordersdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    open-in-view: false
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  h2:
    console:
      enabled: true
      path: /h2-console
```

### Why H2?

H2 makes the example easy to run without requiring a separately installed database server.

It is suitable for demonstrating:

- JPA entity mapping.
- Repository operations.
- Transaction management.
- Database persistence during application execution.
- Integration testing.

The `create-drop` setting creates the database schema when the application starts and drops it when the application shuts down.

This configuration is intended for an educational example, not production deployment.

### Open EntityManager in View

The example explicitly configures:

```yaml
open-in-view: false
```

This disables Open EntityManager in View.

Database access should therefore happen within appropriate service or repository transaction boundaries.

The OrderRepository uses `@EntityGraph` to fetch order items when retrieving orders, avoiding lazy-loading problems during REST response serialization.

---

## Initial Application Data

The application uses `DataInitializer` to insert demonstration products and their inventory records when the database is empty.

The initial data includes:

| Product  |   Price | Available Quantity |
|----------|--------:|-------------------:|
| Laptop   | 1200.00 |                 10 |
| Keyboard |   75.00 |                 25 |
| Mouse    |   25.00 |                 50 |

This allows the application to be tested immediately after startup without manually inserting records.

---

## Testing the Integration

The example uses Spring Boot Test, MockMvc, Spring Security Test, and Spring Modulith Test.

The tests focus on verifying that the different Spring technologies work together correctly.

### Application Context Test

The application context test verifies that Spring Boot can start the application and configure its components.

```java
@Test
void contextLoads() {
}
```

This is a basic application startup verification.

### Product API Test

The product endpoints are public and can be accessed without authentication.

The tests verify that product information can be retrieved through the REST API.

### Authentication Test

An unauthenticated request to the order endpoints is rejected.

The test verifies that Spring Security protects customer operations.

### Successful Order Placement

The successful order placement test verifies that:

- A customer can authenticate.
- The order endpoint accepts a valid request.
- The order is persisted.
- Inventory is reduced by the requested quantity.
- The order total is calculated correctly.
- The order status is `PLACED`.
- An audit entry is created after the transaction commits.

This test demonstrates the interaction between Spring Web, Spring Security, Spring Data JPA, transaction management, and application events.

### Insufficient Stock

The insufficient stock test verifies that an order cannot be placed when the requested quantity exceeds available inventory.

It checks that:

- The API returns HTTP 409.
- Inventory remains unchanged.
- No order is persisted.
- No audit entry is created.

### Transaction Rollback

A service-level test verifies transaction rollback when stock reservation fails.

This demonstrates that the inventory and order operations participate in the same transaction.

### Order Ownership

The tests verify that a customer can retrieve their own orders but cannot retrieve another customer's order.

### Inventory Authorization

The inventory endpoint is restricted to administrators.

The tests verify that:

- An administrator can retrieve inventory information.
- A customer receives HTTP 403 when attempting to access the same endpoint.

### Invalid Order Request

The request validation test verifies that an order with an invalid quantity is rejected.

It also checks that no inventory, order, or audit data is changed.

### Missing Product

The missing product test verifies that attempting to order a nonexistent product returns HTTP 404.

It also verifies that the operation does not create an order or audit entry and does not modify existing inventory.

### Module Verification

Spring Modulith provides module verification through `ApplicationModules`.

```java
@Test
void verifiesApplicationModuleStructure() {
    ApplicationModules.of(IntegrationApplication.class).verify();
}
```

This verifies that the application's module structure follows the declared module boundaries and dependency rules.

---

## Overall Order Placement Test Flow

The main integration test exercises the complete order placement workflow.

```text
Customer
   |
   ▼
HTTP POST /api/orders
   |
   ▼
Spring Security Authentication
   |
   ▼
OrderController
   |
   ▼
OrderService
   |
   +--------------------------+
   |                          |
   ▼                          ▼
ProductService          InventoryService
   |                          |
   ▼                          ▼
Find Product            Lock Inventory
   |                          |
   |                          ▼
   |                    Check Stock
   |                          |
   |                          ▼
   |                    Reserve Stock
   |                          |
   +-------------+------------+
                 |
                 ▼
            Save Order
                 |
                 ▼
         Publish Order Event
                 |
                 ▼
         Commit Transaction
                 |
                 ▼
         Audit Event Listener
                 |
                 ▼
          Save Audit Entry
                 |
                 ▼
           HTTP Response
```

This is the central integration scenario of the example.

---

## Running the Example

From the repository root:

```bash
mvn -pl 06-capstone-application/integration-between-spring-technologies spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

### H2 Console

The H2 console is available at:

```text
http://localhost:8080/h2-console
```

Use the following connection settings:

| Setting   | Value                  |
|-----------|------------------------|
| JDBC URL  | `jdbc:h2:mem:ordersdb` |
| User Name | `sa`                   |
| Password  | Leave empty            |

The console is enabled for local demonstration purposes.

### Demo Credentials

| Username | Password | Role     |
|----------|----------|----------|
| customer | password | CUSTOMER |
| admin    | password | ADMIN    |

These credentials are for demonstration only and should not be used in a production application.

### Example API Requests

Retrieve products:

```bash
curl http://localhost:8080/api/products
```

Retrieve inventory as an administrator:

```bash
curl -u admin:password \
  http://localhost:8080/api/inventory/1
```

Place an order as a customer:

```bash
curl -u customer:password \
  -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":2}'
```

Retrieve the authenticated customer's orders:

```bash
curl -u customer:password \
  http://localhost:8080/api/orders
```

Retrieve a specific order:

```bash
curl -u customer:password \
  http://localhost:8080/api/orders/1
```

---

## Running the Tests

Run all tests in this module:

```bash
mvn -pl 06-capstone-application/integration-between-spring-technologies test
```

Or navigate to the module directory and execute:

```bash
mvn test
```

The test suite verifies the application context, REST endpoints, security rules, persistence operations, transactional behavior, event handling, and module boundaries.

---

## Key Takeaways

- Spring Boot provides application bootstrapping, dependency injection, and auto-configuration.
- Spring Modulith helps organize an application around business capabilities and enforce module boundaries.
- Spring Data JPA simplifies database access through repositories.
- Spring Web exposes application functionality through REST endpoints.
- Spring Security provides authentication and role-based authorization.
- Authorization can also include business-level ownership checks.
- `@Transactional` coordinates multiple database operations within one transaction.
- Database locking helps protect inventory during concurrent stock reservations.
- Spring application events enable loosely coupled communication between modules.
- `@TransactionalEventListener` supports transaction-bound event handling.
- `AFTER_COMMIT` ensures the audit listener runs after a successful order transaction.
- `REQUIRES_NEW` allows the audit operation to execute in its own transaction.
- `@RestControllerAdvice` centralizes REST exception handling.
- Spring Boot Test and MockMvc verify interactions across the application.
- Spring Modulith module verification helps enforce architectural boundaries.

---

## Next Example

The next example will explore **End-to-End Application Verification**, demonstrating how to verify the complete capstone application as a working system.

It will focus on validating end-to-end application workflows, ensuring that the integrated Spring technologies work together correctly, and verifying the application's behavior across its modules and REST APIs.
