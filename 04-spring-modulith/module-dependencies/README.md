# Module Dependencies

This example demonstrates **module dependencies** in Spring Modulith.

It focuses on how application modules declare allowed dependencies, how one module accesses another module through its public API, and how Spring Modulith verifies that the application's architecture follows the declared dependency rules.

## What This Example Demonstrates

- What module dependencies mean in Spring Modulith.
- Application modules and their relationships.
- Declaring allowed dependencies using `@ApplicationModule`.
- Using a module's public API from another module.
- Encapsulating internal implementation details.
- Restricting dependencies between application modules.
- Verifying module dependencies with `ApplicationModules.verify()`.
- Testing service interactions across module boundaries.
- Detecting architectural violations through automated tests.

---

## What Are Module Dependencies?

Module dependencies describe how application modules rely on one another to provide functionality.

In a modular application, each module should have a clear responsibility and expose an intentional API for other modules to use.

For example, an inventory module may need to retrieve product information from a catalog module.

Instead of accessing the catalog's internal repository directly, the inventory module can depend on the catalog's public service API.

A simplified relationship looks like:

```text
Inventory Module
       │
       ▼
Catalog Public API
       │
       ▼
Catalog Internal Implementation
```

This approach reduces coupling between modules and keeps implementation details encapsulated.

---

## Application Modules

Spring Modulith identifies application modules based on the application's package structure.

In this example, the application contains two modules:

- **Catalog** — provides product lookup functionality.
- **Inventory** — retrieves products through the catalog module.

The package structure follows this organization:

```text
com.springbyexample.moduledependencies
├── ModuleDependenciesApplication.java
├── catalog
│   ├── CatalogService.java
│   ├── Product.java
│   └── internal
│       └── ProductRepository.java
└── inventory
    ├── package-info.java
    └── InventoryService.java
```

The main application package acts as the root of the application's module structure.

The `catalog` and `inventory` packages represent separate application modules.

---

## Catalog Module

The catalog module provides functionality for retrieving products by their SKU.

The `Product` record represents a product:

```java
public record Product(String sku, String name) {
}
```

The `ProductRepository` contains the example product data and performs lookups:

```java
@Repository
public class ProductRepository {

    private final Map<String, Product> products = Map.of(
            "SKU-001", new Product("SKU-001", "Laptop"),
            "SKU-002", new Product("SKU-002", "Keyboard"),
            "SKU-003", new Product("SKU-003", "Monitor")
    );

    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(products.get(sku));
    }
}
```

The repository belongs to the catalog module's internal package:

```text
catalog.internal
```

The `CatalogService` provides the functionality that other modules should use:

```java
@Service
public class CatalogService {

    private final ProductRepository productRepository;

    public CatalogService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }
}
```

The service acts as the entry point through which the inventory module accesses catalog functionality.

---

## Declaring Allowed Dependencies

Spring Modulith provides `@ApplicationModule` to configure application module metadata, including which other modules a module may depend on.

This example declares the inventory module's dependency in `inventory/package-info.java`:

```java
@org.springframework.modulith.ApplicationModule(allowedDependencies = "catalog")
package com.springbyexample.v2.moduledependencies.inventory;
```

The `allowedDependencies` attribute specifies that the inventory module may depend on the catalog module.

This makes the intended dependency explicit rather than leaving the architecture entirely implicit in the Java code.

### Why Use `package-info.java`?

`package-info.java` allows annotations and documentation to be associated with a Java package.

Spring Modulith uses package-level metadata to configure and describe application modules.

In this example, the annotation declares the dependency rule for the inventory module as a whole.

---

## Understanding `allowedDependencies`

Consider this declaration:

```java
@ApplicationModule(allowedDependencies = "catalog")
```

It means that the inventory module is allowed to depend on the catalog module.

The declared rule does not mean that the inventory module automatically receives access to every implementation detail inside the catalog module.

Module dependencies and module API visibility are related but distinct concepts.

A dependency declaration defines which module may be referenced. The module's exposed API determines which parts of that module are intended to be accessed externally.

Spring Modulith can verify whether dependencies comply with its module rules.

---

## Using the Catalog Module from Inventory

The inventory module depends on `CatalogService` rather than accessing `ProductRepository` directly.

```java
@Service
public class InventoryService {

    private final CatalogService catalogService;

    public InventoryService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public Optional<Product> findProduct(String sku) {
        return catalogService.findBySku(sku);
    }
}
```

The service delegates product lookup to the catalog module.

The resulting call path is:

```text
InventoryService
       │
       ▼
CatalogService
       │
       ▼
ProductRepository
       │
       ▼
Product
```

The inventory module does not need to know how the catalog retrieves or stores its products.

It only needs to know about the catalog service's public contract.

This reduces coupling and makes it easier to change the catalog's internal implementation without requiring corresponding changes in inventory.

---

## Public API vs Internal Implementation

A key principle of modular design is separating the functionality exposed to other modules from the details used to implement that functionality.

In this example:

| Component           | Responsibility                        |
|---------------------|---------------------------------------|
| `CatalogService`    | Provides product lookup functionality |
| `Product`           | Represents product information        |
| `ProductRepository` | Implements internal product lookup    |

The inventory module should depend on the catalog's public API rather than directly accessing its internal repository.

Conceptually:

```text
Catalog Module
├── Public API
│   ├── CatalogService
│   └── Product
└── Internal Implementation
    └── ProductRepository
```

The internal package communicates that `ProductRepository` is an implementation detail.

Spring Modulith can detect certain violations of module encapsulation when the application modules are verified.

---

## Why Declare Module Dependencies Explicitly?

Without explicit dependency rules, modules can gradually become tightly coupled.

For example, inventory might begin accessing the catalog's internal repository directly. Other modules might start depending on inventory implementation classes, and circular dependencies might develop.

Explicit dependency rules make these relationships intentional.

```text
Without Clear Rules

Module A ─────► Module B
   ▲               │
   │               ▼
   └──────── Module C
```

An architecture with unclear dependency rules can become difficult to understand and maintain.

With explicit rules:

```text
Inventory Module
       │
       │ Declared Dependency
       ▼
Catalog Module
```

The application can verify whether its implementation follows the intended architecture.

---

## Module Dependencies vs Module Boundaries

Module boundaries and module dependencies are closely related, but they focus on different aspects of modularity.

### Module Boundaries

Module boundaries define the separation between application modules and identify which parts of a module are exposed to other modules.

### Module Dependencies

Module dependencies define which other modules a module is permitted to depend on.

The distinction can be summarized as:

| Concept                   | Main Question                                      |
|---------------------------|----------------------------------------------------|
| Module boundaries         | Where does one module end and another begin?       |
| Module API                | Which parts of a module can other modules access?  |
| Module dependencies       | Which modules may depend on one another?           |
| Architecture verification | Does the implementation follow the declared rules? |

Together, these concepts help maintain a modular application structure.

---

## Running the Example

From the repository root, run:

```bash
./mvnw -pl 04-spring-modulith/module-dependencies spring-boot:run
```

The application starts with the catalog and inventory services registered in the Spring application context.

---

## Running the Tests

Run the module's tests from the repository root:

```bash
./mvnw -pl 04-spring-modulith/module-dependencies test
```

Alternatively, run the module and its required upstream Maven modules:

```bash
./mvnw -pl 04-spring-modulith/module-dependencies -am test
```

The tests cover product lookup, missing products, service delegation, and module dependency verification.

---

## Key Takeaways

- Module dependencies describe relationships between application modules.
- Spring Modulith identifies application modules from the application's package structure.
- `@ApplicationModule` can declare which modules a module is allowed to depend on.
- `CatalogService` provides the catalog functionality used by the inventory module.
- Internal implementation classes should not be accessed directly from other modules.
- `ApplicationModules.verify()` checks the application's modular architecture.
- Unit tests verify service behavior, while architecture tests verify structural rules.
- Explicit dependency rules help prevent unwanted coupling and architectural regressions.
- Module boundaries, public APIs, and allowed dependencies work together to support modular application design.

---

## What This Example Does Not Cover

This example intentionally focuses on declaring and verifying module dependencies.

It does not cover:

- Named interfaces for exposing multiple APIs from a module.
- Application events and event-driven communication.
- Event externalization.
- Module integration testing.
- Module documentation generation.
- Runtime module isolation.
- Microservice communication.
- Distributed transactions between services.

These topics can be explored in separate examples.

---

## Next Example

The next example will explore **Application Events** in Spring Modulith, including publishing events between application modules and using event-driven communication to reduce direct coupling.
