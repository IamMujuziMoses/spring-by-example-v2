# Module Boundaries

This example demonstrates **module boundaries** in Spring Modulith.

It focuses on how application modules expose public APIs, keep implementation details internal, declare allowed module dependencies, and use architectural verification to protect the application's modular structure.

## What This Example Demonstrates

- What module boundaries mean in Spring Modulith.
- Public APIs and internal implementation packages.
- The `@NamedInterface` annotation.
- The `@ApplicationModule` annotation.
- The `allowedDependencies` attribute.
- Declaring dependencies on specific named interfaces.
- Constructor injection through a public module API.
- Verifying module boundaries with `ApplicationModules.verify()`.
- Testing a consumer module independently of an implementation module.

---

## What Are Module Boundaries?

Module boundaries define which parts of an application module other modules are allowed to access.

In a modular monolith, application modules belong to the same application, but they should not depend indiscriminately on each other's implementation details.

For example:

```text
Application
    │
    ├── Catalog
    │     ├── Public API
    │     └── Internal Implementation
    │
    └── Inventory
```

The inventory module may need product information from the catalog module.

However, it should not need to know how the catalog retrieves, stores, or manages product information internally.

A well-defined boundary allows the catalog module to change its implementation without unnecessarily affecting its consumers.

---

## Public APIs and Internal Implementations

A module's public API defines the functionality it makes available to other modules.

Its internal implementation contains the details used to provide that functionality.

In this example, the catalog module has two important areas:

```text
catalog/
    │
    ├── api/
    │     ├── Product.java
    │     └── ProductCatalog.java
    │
    └── internal/
          └── ProductCatalogImpl.java
```

### Public API

The `api` package contains the `Product` record and `ProductCatalog` interface.

These types define the contract that consumers can use.

### Internal Implementation

The `internal` package contains `ProductCatalogImpl`, which implements the public interface.

Other modules should not depend directly on this implementation.

This separation makes it possible to change the implementation while keeping the public contract stable.

---

## The `ProductCatalog` Interface

The catalog exposes a simple interface for looking up products.

```java
public interface ProductCatalog {

    Optional<Product> findBySku(String sku);
}
```

The interface describes what consumers can do without specifying how the operation is implemented.

For example, the implementation could eventually retrieve products from a database, a remote service, or another storage mechanism.

Consumers would continue using the same interface as long as its contract remains unchanged.

---

## The `ProductCatalogImpl` Implementation

The catalog provides an implementation of the interface:

```java
@Service
public class ProductCatalogImpl implements ProductCatalog {

    private final Map<String, Product> products = Map.of(
            "SKU-001", new Product("SKU-001", "Laptop"),
            "SKU-002", new Product("SKU-002", "Keyboard"),
            "SKU-003", new Product("SKU-003", "Monitor")
    );

    @Override
    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(products.get(sku));
    }
}
```

The example uses an in-memory map to keep the implementation small.

The important point is that the implementation resides in a separate internal package.

The inventory module does not need to reference `ProductCatalogImpl` to retrieve product information.

---

## The `@NamedInterface` Annotation

Spring Modulith treats the module's root package as its default API package.

When a module needs to expose an additional package, it can declare a named interface using `@NamedInterface`.

This example declares `catalog.api` as a named interface.

The `catalog/api/package-info.java` file contains:

```java
@org.springframework.modulith.NamedInterface("api")
package com.springbyexample.moduleboundaries.catalog.api;
```

The name `api` identifies the named interface.

Other modules can explicitly declare a dependency on this interface.

Conceptually:

```text
Catalog Module
      │
      ├── api
      │     ├── Product
      │     └── ProductCatalog
      │
      └── internal
            └── ProductCatalogImpl
```

The `api` package is exposed as a named interface, while the `internal` package remains an implementation detail.

A named interface is an architectural declaration. It does not, by itself, make Java classes private or inaccessible at the language level.

---

## The `@ApplicationModule` Annotation

Spring Modulith provides `@ApplicationModule` for declaring application module metadata, including allowed dependencies.

In this example, the inventory module declares:

```java
@org.springframework.modulith.ApplicationModule(allowedDependencies = "catalog :: api")
package com.springbyexample.moduleboundaries.inventory;
```

The `allowedDependencies` attribute specifies which other module interfaces the inventory module is allowed to depend on.

The expression:

```text
catalog :: api
```

means:

- `catalog` identifies the target application module.
- `api` identifies the named interface exposed by that module.

This declaration makes the intended dependency explicit.

Instead of allowing inventory to depend on arbitrary catalog internals, the architecture specifies that it may depend on the catalog's `api` interface.

---

## Understanding `allowedDependencies`

The `allowedDependencies` attribute can be used to restrict a module's dependencies.

For example:

```java
@ApplicationModule(allowedDependencies = "catalog :: api")
package com.example.inventory;
```

This allows dependencies on the `api` named interface of the `catalog` module.

The exact dependency declaration matters because module verification checks whether the actual dependencies comply with the declared architecture.

This is particularly useful as an application grows and developers add new classes or introduce new dependencies.

---

## Using the Public API from Inventory

The inventory service receives the `ProductCatalog` interface through constructor injection:

```java
@Service
public class InventoryService {

    private final ProductCatalog productCatalog;

    public InventoryService(ProductCatalog productCatalog) {
        this.productCatalog = productCatalog;
    }

    public Optional<Product> findProduct(String sku) {
        return productCatalog.findBySku(sku);
    }
}
```

Notice that the service depends on:

```java
ProductCatalog
```

It does not depend on:

```java
ProductCatalogImpl
```

Spring resolves the implementation at runtime and injects it through the constructor.

The dependency therefore follows the public contract rather than the implementation.

Conceptually:

```text
InventoryService
       │
       ▼
ProductCatalog
   (Interface)
       ▲
       │ implements
       │
ProductCatalogImpl
   (Implementation)
```

This makes the relationship between the two modules easier to maintain and test.

---

## Why Keep Implementation Details Internal?

Suppose the catalog module later replaces its in-memory map with a database-backed implementation.

The internal implementation might change from:

```text
ProductCatalogImpl
        │
        ▼
In-Memory Map
```

to:

```text
ProductCatalogImpl
        │
        ▼
Product Repository
        │
        ▼
Database
```

If the public `ProductCatalog` interface remains compatible, the inventory module does not need to change merely because the catalog's storage implementation changed.

This is one of the main benefits of module boundaries: changes can remain localized to the module that owns the affected functionality.

---

## Verifying Module Boundaries

Spring Modulith provides `ApplicationModules` to discover application modules and verify their architectural relationships.

The example includes a test that invokes:

```java
ApplicationModules.of(ModuleBoundariesApplication.class).verify();
```

This verifies the discovered application structure against Spring Modulith's applicable architectural rules.

The verification checks include restrictions on dependencies between modules and access to types considered internal.

The test turns the intended architecture into an automated check.

Conceptually:

```text
Application Code
       │
       ▼
Module Discovery
       │
       ▼
Architecture Verification
       │
       ▼
Pass or Fail
```

If a developer introduces an illegal dependency, verification can detect the violation and fail the test.

---

## Testing the Inventory Service

The example also includes unit tests for `InventoryService`.

The tests supply a simple `ProductCatalog` implementation directly:

```java
ProductCatalog catalog = sku -> "SKU-001".equals(sku) ? Optional.of(product) : Optional.empty();
```

The service is then constructed with that interface:

```java
InventoryService inventoryService = new InventoryService(catalog);
```

This demonstrates that the inventory service can be tested without creating the catalog's real implementation or loading the entire Spring application context.

The tests cover:

- Finding a product through the catalog interface.
- Returning an empty result when a product does not exist.

This separation makes the service easier to test and reinforces the value of programming against a public contract.

---

## Module Boundaries vs Java Visibility

Java visibility and architectural boundaries are related, but they are not the same thing.

Java's access modifiers control access at the language level.

Spring Modulith adds architectural verification that understands application modules and their exposed interfaces.

For example, a class may be declared `public` in Java while still being considered internal to its Spring Modulith module.

That means another module might technically be able to reference the class in source code, but Spring Modulith's verification can reject the dependency.

This allows a project to enforce architectural rules even when Java visibility alone is insufficient.

---

## Running the Example

From the repository root:

```bash
mvn -pl 04-spring-modulith/module-boundaries spring-boot:run
```

The application starts as a Spring Boot application.

This example focuses on module architecture and service interaction rather than exposing HTTP endpoints.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or run the module's verification from the repository root:

```bash
mvn -pl 04-spring-modulith/module-boundaries clean verify
```

The tests cover:

- Module boundary verification.
- Product lookup through the public catalog interface.
- Handling products that do not exist.
- Testing the inventory service independently of the catalog implementation.

---

## Key Takeaways

- Module boundaries define which parts of an application module other modules may access.
- A module's public API separates its contract from its implementation.
- `@NamedInterface` exposes an additional package as a named interface.
- `@ApplicationModule` can declare allowed module dependencies.
- `allowedDependencies = "catalog :: api"` restricts the declared dependency to the catalog's `api` interface.
- Constructor injection allows consumers to depend on interfaces rather than concrete implementations.
- Spring Modulith verification helps detect unintended dependencies and access to internal types.
- Architectural tests help keep a modular monolith maintainable as it grows.
- Java visibility alone does not enforce all Spring Modulith architectural boundaries.

---

## What This Example Does Not Cover

This example intentionally focuses on defining and verifying module boundaries.

It does not cover:

- Module dependency graphs across a large application.
- Application events and inter-module event communication.
- Event externalization.
- Module integration testing with `@ApplicationModuleTest`.
- Generating module documentation.
- Extracting modules into independent microservices.
- Database ownership and transaction boundaries across modules.

These topics can be explored in subsequent examples.

---

## Next Example

The next example will explore **Module Dependencies** in Spring Modulith, including how to declare allowed dependencies, avoid cyclic dependencies, and verify the dependency structure of an application.
