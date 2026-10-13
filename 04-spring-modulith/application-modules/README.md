# Application Modules

This example demonstrates **application modules** in Spring Modulith.

It focuses on how Spring Modulith discovers application modules from a Spring Boot application's package structure, how business capabilities can be organized into separate modules, and how to verify the application's modular structure.

## What This Example Demonstrates

- What application modules are in Spring Modulith.
- The concept of a modular monolith.
- Organizing application code around business capabilities.
- Package-based application module discovery.
- Using `ApplicationModules` to inspect application modules.
- Verifying the application module structure with `ApplicationModules.verify()`.
- Testing application module discovery.
- Writing unit tests for services within application modules.

---

## What Are Application Modules?

Application modules are logical units within an application that group related functionality around a particular business capability.

For example, an e-commerce application might contain modules for:

- Catalog
- Inventory
- Orders
- Payments
- Customers

Each module represents a distinct area of responsibility within the application.

Spring Modulith helps developers structure Spring Boot applications around these modules and provides tools for inspecting and verifying their architecture.

A simplified structure looks like:

```text
Spring Boot Application
        │
        ├── Catalog
        │
        ├── Inventory
        │
        ├── Orders
        │
        └── Payments
```

These modules belong to the same application and can be deployed together, while their logical separation makes the codebase easier to understand and maintain.

---

## What Is a Modular Monolith?

A modular monolith is an application that runs as a single deployable unit but is internally organized into well-defined modules.

Unlike a collection of microservices, a modular monolith does not require each module to run as a separate process or be deployed independently.

Conceptually:

```text
        Modular Monolith
              │
     ┌────────┼────────┐
     │        │        │
     ▼        ▼        ▼
  Catalog  Inventory  Orders
     │        │        │
     └────────┼────────┘
              │
        Single Application
```

The goal is to combine the operational simplicity of a single application with the maintainability of a modular architecture.

Spring Modulith supports this approach by helping developers identify modules and verify architectural rules.

---

## The Main Application

The application entry point is a standard Spring Boot application:

```java
@SpringBootApplication
public class ApplicationModulesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationModulesApplication.class, args);
    }
}
```

The main application class establishes the root package from which Spring Modulith discovers application modules.

In this example, the relevant package structure is:

```text
com.springbyexample.applicationmodules
        │
        ├── catalog
        │
        └── inventory
```

Each direct subpackage represents a candidate application module.

Spring Modulith inspects the application structure to identify these modules.

---

## The Catalog Module

The `catalog` module represents the product catalog capability.

Its service provides a simple operation for retrieving featured products:

```java
public class CatalogService {

    public List<String> findFeaturedProducts() {
        return List.of("Laptop", "Keyboard", "Monitor");
    }
}
```

The service is intentionally simple because the focus of this example is module organization rather than implementing a complete product catalog.

The important point is that catalog-related functionality is grouped under the `catalog` package.

Conceptually:

```text
catalog/
    │
    └── CatalogService
```

As the application grows, the module could contain additional catalog-related components, such as product queries, validation, and domain logic.

---

## The Inventory Module

The `inventory` module represents the inventory capability.

Its service provides a simple operation for retrieving the available quantity for a product SKU:

```java
public class InventoryService {

    public int getAvailableQuantity(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank");
        }

        return 10;
    }
}
```

The service validates its input and returns a fixed quantity for demonstration purposes.

The inventory-related functionality is grouped under the `inventory` package:

```text
inventory/
    │
    └── InventoryService
```

The example does not implement persistent inventory storage or stock reservation. Those concerns are outside the scope of this first module-discovery example.

---

## Application Module Discovery

Spring Modulith provides the `ApplicationModules` API for discovering and inspecting an application's modules.

The example uses:

```java
ApplicationModules modules = ApplicationModules.of(ApplicationModulesApplication.class);
```

The `ApplicationModules.of(...)` method analyzes the application based on its main application class.

Spring Modulith then discovers the modules associated with the application's package structure.

For this example, the expected modules are:

```text
ApplicationModulesApplication
        │
        ├── catalog
        │
        └── inventory
```

The discovery test checks that both modules are present:

```java
assertThat(modules.getModuleByName("catalog")).isPresent();
assertThat(modules.getModuleByName("inventory")).isPresent();
```

This demonstrates how application modules can be inspected programmatically rather than relying only on developers to recognize package boundaries by convention.

---

## Understanding `ApplicationModules`

`ApplicationModules` represents the collection of application modules discovered by Spring Modulith.

It can be used to inspect the application's modular structure and verify architectural constraints.

In this example:

```java
ApplicationModules modules = ApplicationModules.of(ApplicationModulesApplication.class);
```

The application class provides the starting point for module discovery.

The resulting `ApplicationModules` instance can then be used to retrieve individual modules and verify the overall structure.

Conceptually:

```text
Main Application Class
          │
          ▼
ApplicationModules.of(...)
          │
          ▼
Module Discovery
          │
          ▼
Discovered Modules
          │
          ├── catalog
          │
          └── inventory
```

This API is useful for making the architecture visible and testable.

---

## Verifying Application Modules

Discovering modules is only part of the process.

Spring Modulith also provides a way to verify whether the application's modular structure satisfies its architectural rules.

The example invokes:

```java
modules.verify();
```

This verifies the discovered modules against Spring Modulith's applicable architectural rules.

For example, as module dependencies become more complex, verification can help detect dependencies that violate the intended modular architecture.

The discovery test therefore performs two related checks:

```text
Discover Modules
       │
       ▼
Confirm Expected Modules
       │
       ▼
Verify Architecture
```

This is an important step toward preventing an application's modular structure from deteriorating as new features are introduced.

---

## Module Discovery vs Module Boundaries

Discovering an application module does not automatically mean that its boundaries are well designed.

For example, Spring Modulith may discover both `catalog` and `inventory`, but developers must still decide which types each module exposes and which dependencies are allowed between modules.

Consider this structure:

```text
Application
    │
    ├── Catalog
    │
    └── Inventory
```

At this stage, the example demonstrates that these modules can be discovered and inspected.

It does not yet establish a complete set of rules governing how they can depend on one another.

Explicit module boundaries and dependency constraints will be explored in subsequent examples.

---

## Running the Example

From the repository root, run:

```bash
mvn -pl 04-spring-modulith/application-modules spring-boot:run
```

The application starts as a Spring Boot application.

This example focuses on application structure and module discovery rather than exposing HTTP endpoints.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or run the module's verification from the repository root:

```bash
mvn -pl 04-spring-modulith/application-modules clean verify
```

The tests cover:

- Application module discovery.
- Application module verification.
- Catalog service behavior.
- Inventory service behavior.

---

## Key Takeaways

- Application modules group related functionality around business capabilities.
- A modular monolith runs as one deployable application while maintaining logical module separation.
- Spring Modulith discovers application modules from the application's package structure.
- `ApplicationModules` provides an API for inspecting discovered modules.
- `ApplicationModules.verify()` checks the modular structure against applicable architectural rules.
- Architecture tests help prevent unintended structural changes.
- Module discovery is different from defining explicit module boundaries.
- Module-level architecture tests complement unit tests for individual services.
- Starting with small, focused modules makes it easier to evolve a modular application.

---

## What This Example Does Not Cover

This example intentionally focuses on basic application module discovery and verification.

It does not cover:

- Explicitly defining module boundaries.
- Named interfaces and exposed module APIs.
- Allowed and disallowed module dependencies.
- Application events and inter-module communication.
- Event externalization.
- Module documentation generation.
- Advanced module integration testing.
- Splitting a modular monolith into microservices.

These topics can be explored in separate examples as the Spring Modulith module develops.

---

## Next Example

The next example will explore **Module Boundaries** in Spring Modulith, including how to define clear boundaries between application modules and control which parts of a module are exposed to other modules.
