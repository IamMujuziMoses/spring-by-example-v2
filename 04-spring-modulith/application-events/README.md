# Application Events

This example demonstrates **application events** in Spring Modulith.

It focuses on how one application module can publish an event and another module can react to it without the publishing module directly depending on the event consumer.

## What This Example Demonstrates

- What application events mean in Spring.
- Publishing events with `ApplicationEventPublisher`.
- Defining event records to represent business occurrences.
- Handling events with `@EventListener`.
- Communicating between application modules through events.
- Reducing direct dependencies between modules.
- Testing event-driven interactions.
- Verifying the application module structure with `ApplicationModules.verify()`.

---

## What Are Application Events?

Application events allow components within the same application to communicate by publishing and handling events.

Instead of directly invoking another module's service, a module can publish an event describing something that has happened.

Interested components can then handle that event independently.

For example, when a product is registered in the catalog, the inventory module can react to the registration event.

A simplified flow looks like:

```text
Catalog Module
      │
      ▼
Product Registered
      │
      ▼
ApplicationEventPublisher
      │
      ▼
ProductRegisteredEvent
      │
      ▼
InventoryEventListener
      │
      ▼
Inventory Module
```

The catalog module does not need to know which components consume the event.

---

## Application Modules in This Example

The application contains two modules:

- **Catalog** — registers products and publishes product registration events.
- **Inventory** — listens for product registration events and records the registered product SKU.

The package structure follows this organization:

```text
com.springbyexample.applicationevents
├── ApplicationEventsApplication.java
├── catalog
│   ├── CatalogService.java
│   ├── Product.java
│   ├── ProductRegisteredEvent.java
│   └── internal
│       └── ProductRepository.java
└── inventory
    ├── InventoryEventListener.java
    └── InventoryService.java
```

The catalog module owns the product registration process and the event describing that process.

The inventory module owns the event listener and its reaction to the event.

---

## Defining an Application Event

The example defines a `ProductRegisteredEvent` record:

```java
public record ProductRegisteredEvent(String sku, String name) {
}
```

The event represents the fact that a product has been registered.

It contains the product SKU and name so that consumers can react without needing a direct reference to the catalog service or its repository.

An event should describe something that happened rather than require the consumer to perform a specific operation.

In this example, `ProductRegisteredEvent` describes a completed product registration.

---

## Publishing an Event

Spring provides `ApplicationEventPublisher` for publishing application events.

The `CatalogService` receives an `ApplicationEventPublisher` through constructor injection:

```java
public CatalogService(ProductRepository productRepository, ApplicationEventPublisher eventPublisher) {
    this.productRepository = productRepository;
    this.eventPublisher = eventPublisher;
}
```

When a product is registered, the service saves the product and publishes the event:

```java
public Product registerProduct(String sku, String name) {
    Product product = new Product(sku, name);

    productRepository.save(product);

    eventPublisher.publishEvent(new ProductRegisteredEvent(product.sku(), product.name()));

    return product;
}
```

The important operation is:

```java
eventPublisher.publishEvent(new ProductRegisteredEvent(product.sku(), product.name()));
```

This publishes the event to Spring's application event mechanism.

The catalog module does not invoke `InventoryService` or `InventoryEventListener` directly.

It simply announces that a product has been registered.

---

## Handling an Application Event

Spring provides `@EventListener` for declaring methods that should handle application events.

The inventory module defines an `InventoryEventListener`:

```java
@Component
public class InventoryEventListener {

    private final InventoryService inventoryService;

    public InventoryEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @EventListener
    public void on(ProductRegisteredEvent event) {
        inventoryService.recordRegisteredProduct(event.sku());
    }
}
```

When Spring publishes a `ProductRegisteredEvent`, the listener method receives it and records the product SKU through `InventoryService`.

The listener does not need to be called directly by the catalog module.

Spring discovers the event listener and dispatches matching events to it.

---

## The Role of InventoryService

The inventory service records the SKUs it has received through events.

```java
@Service
public class InventoryService {

    private final Set<String> registeredProductSkus = ConcurrentHashMap.newKeySet();

    public void recordRegisteredProduct(String sku) {
        registeredProductSkus.add(sku);
    }

    public boolean containsSku(String sku) {
        return registeredProductSkus.contains(sku);
    }
}
```

The set is an in-memory representation used for demonstration purposes.

It allows the integration test to verify that the inventory module reacted to the published event.

This is not intended to be a production inventory persistence implementation.

---

## How Event-Driven Communication Reduces Coupling

Consider direct service invocation between modules:

```text
CatalogService
      │
      ▼
InventoryService
```

With this arrangement, the catalog service needs a reference to the inventory service.

That creates a direct dependency between the two modules.

With event-driven communication:

```text
CatalogService
      │
      ▼
ApplicationEventPublisher
      │
      ▼
ProductRegisteredEvent
      │
      ▼
InventoryEventListener
      │
      ▼
InventoryService
```

The catalog publishes an event without knowing who consumes it.

Additional components can subscribe to the same event without requiring changes to the catalog service.

For example, a notification module could later listen for `ProductRegisteredEvent` without requiring the catalog module to depend on that notification module.

This is one of the main benefits of event-driven communication in a modular monolith.

---

## Application Events and Module Dependencies

Events reduce direct service dependencies, but they do not eliminate all module dependencies.

The inventory module still references `ProductRegisteredEvent`, which belongs to the catalog module.

Therefore, inventory has a dependency on the catalog module's event contract.

The dependency direction is:

```text
Inventory Module
       │
       │ Depends on event type
       ▼
Catalog Module
       │
       ▼
ProductRegisteredEvent
```

The important architectural difference is that the catalog module does not depend on the inventory module.

The publishing module does not need to know which modules consume its events.

---

## Synchronous Event Handling

This example uses Spring's regular `@EventListener`.

By default, event publication and listener execution are synchronous. The listener normally executes in the publishing thread before `publishEvent()` returns.

The flow is therefore:

```text
CatalogService
      │
      ▼
Publish Event
      │
      ▼
InventoryEventListener Executes
      │
      ▼
InventoryService Updated
      │
      ▼
Publishing Call Returns
```

This makes the behavior straightforward to demonstrate and test.

However, synchronous event handling does not automatically provide asynchronous processing, durable event storage, or independent transaction boundaries.

If a listener throws an exception, that exception can propagate back to the publisher and affect the calling operation.

---

## Application Events vs Distributed Messaging

Spring application events are designed for communication within a Spring application context.

They are not automatically distributed messages.

| Application Events                                          | Distributed Messaging                                              |
|-------------------------------------------------------------|--------------------------------------------------------------------|
| Communicate within the application context                  | Communicate across application processes                           |
| Use Spring's event publication mechanism                    | Commonly use a message broker                                      |
| Do not inherently require a broker                          | Typically require messaging infrastructure                         |
| Regular `@EventListener` handling is synchronous by default | Delivery behavior depends on the messaging system                  |
| Do not inherently guarantee durable delivery                | Durability depends on broker configuration and delivery guarantees |

Spring Modulith also provides additional event-related capabilities, including transactional event listeners and an event publication registry.

Those capabilities can be explored in separate examples.

---

## Running the Example

From the repository root:

```bash
./mvnw -pl 04-spring-modulith/application-events spring-boot:run
```

The application starts with the catalog and inventory services registered in the Spring application context.

---

## Running the Tests

Run the module's tests from the repository root:

```bash
./mvnw -pl 04-spring-modulith/application-events test
```

Alternatively, run the module and its required upstream Maven modules:

```bash
./mvnw -pl 04-spring-modulith/application-events -am test
```

The tests cover the event-driven interaction and module architecture verification.

---

## Key Takeaways

- Application events allow components to communicate without direct service invocation.
- `ApplicationEventPublisher` publishes events within the Spring application context.
- `@EventListener` identifies methods that consume matching events.
- Events should describe business occurrences and carry the information consumers need.
- The publishing module does not need to know which components consume its events.
- Event consumers still depend on the event contract.
- Regular `@EventListener` methods execute synchronously by default.
- Application events do not automatically provide durable delivery or independent transactions.
- Integration tests can verify that publishing an event produces the expected consumer behavior.
- Spring Modulith's architecture verification helps detect unwanted dependencies between modules.

---

## What This Example Does Not Cover

This example intentionally focuses on basic application event publication and consumption.

It does not cover:

- `@ApplicationModuleListener`.
- Transaction-bound event handling.
- Asynchronous event processing.
- The Spring Modulith Event Publication Registry.
- Failed event publication recovery.
- Event externalization.
- Publishing events to Kafka or RabbitMQ.
- Cross-process messaging.
- Distributed transactions.

These topics can be explored in separate examples.

---

## Next Example

The next example will explore **Event Externalization** in Spring Modulith, including how application events can be routed to external messaging infrastructure.
