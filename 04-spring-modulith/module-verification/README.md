# Module Verification

This example demonstrates **Module Verification** in Spring Modulith.

It focuses on how Spring Modulith discovers application modules, verifies architectural boundaries, and detects unwanted dependencies between modules.

## What This Example Demonstrates

- What application module verification means.
- How Spring Modulith discovers application modules.
- `ApplicationModules`.
- `ApplicationModules.of(...).verify()`.
- Declaring allowed module dependencies with `@ApplicationModule`.
- Public APIs and internal implementation packages.
- Detecting cyclic dependencies and architectural violations.
- Testing module architecture with JUnit.
- Complementing architecture tests with unit tests.

---

## What Is Module Verification?

Module verification is the process of checking whether an application's module structure follows its intended architecture.

As an application grows, classes can become tightly coupled across package boundaries. Modules that were intended to remain independent may start depending on each other's implementation details.

Spring Modulith provides architectural verification to help identify these problems automatically.

A simplified flow looks like:

```text
Spring Application
        │
        ▼
Discover Application Modules
        │
        ▼
Analyze Module Dependencies
        │
        ▼
Check Architectural Rules
        │
        ▼
Verification Result
        │
        ├── Valid Architecture
        │
        └── Architectural Violations
```

This allows architectural constraints to be tested rather than relying exclusively on developer conventions.

---

## Application Modules in This Example

This example contains two application modules:

- `catalog` — provides catalog-related functionality.
- `inventory` — consumes catalog functionality through its public API.

The simplified module structure looks like:

```text
com.springbyexample.v2.moduleverification
│
├── catalog
│   ├── CatalogItem
│   ├── CatalogService
│   └── internal
│       └── CatalogStore
│
└── inventory
    ├── InventoryService
    └── package-info.java
```

The catalog module contains a public service and an internal implementation class.

The inventory module depends on the catalog module through `CatalogService`.

Conceptually:

```text
Inventory Module
       │
       ▼
CatalogService
       │
       ▼
CatalogStore
   (internal)
```

The intended design is for inventory to use the catalog module's public API rather than accessing its internal implementation directly.

---

## The Catalog Module

The catalog module provides functionality for retrieving catalog items.

### `CatalogItem`

`CatalogItem` represents a catalog item:

```java
public record CatalogItem(String sku, String name) {}
```

It contains a stock-keeping unit (`sku`) and an item name.

### `CatalogService`

`CatalogService` provides the public service API:

```java
@Service
public class CatalogService {

    private final CatalogStore catalogStore;

    public CatalogService(CatalogStore catalogStore) {
        this.catalogStore = catalogStore;
    }

    public CatalogItem findBySku(String sku) {
        return new CatalogItem(sku, catalogStore.findNameBySku(sku));
    }
}
```

The service delegates the lookup to `CatalogStore` and returns a `CatalogItem`.

Other modules should use this service rather than depend directly on the store implementation.

### `CatalogStore`

`CatalogStore` is located in the catalog module's `internal` package:

```java
package com.springbyexample.v2.moduleverification.catalog.internal;

@Component
public class CatalogStore {

    public String findNameBySku(String sku) {
        return "Sample item";
    }
}
```

The implementation is intentionally simple. It returns a constant sample name rather than querying a database.

The important point is its intended architectural role: it represents an internal implementation detail of the catalog module.

---

## Public APIs and Internal Packages

Spring Modulith treats application modules as architectural units with defined boundaries.

By default, the module's base package is its API area, while subpackages can represent internal implementation details. Spring Modulith can detect access to another module's internal packages during verification.

For this example:

```text
catalog
│
├── CatalogItem.java
├── CatalogService.java
│
└── internal
    └── CatalogStore.java
```

The intended usage is:

```text
Inventory
    │
    ▼
CatalogService
    │
    ▼
CatalogStore
```

An undesirable dependency would look like:

```text
Inventory
    │
    ▼
CatalogStore
    (internal)
```

Direct access to internal classes creates tighter coupling between modules and makes implementation changes more difficult.

Keeping internal packages encapsulated helps preserve the module's public API as its implementation evolves.

---

## The Inventory Module

The inventory module consumes catalog functionality through `CatalogService`.

```java
@Service
public class InventoryService {

    private final CatalogService catalogService;

    public InventoryService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public CatalogItem findCatalogItem(String sku) {
        return catalogService.findBySku(sku);
    }
}
```

The service delegates the lookup to `CatalogService` and returns the resulting `CatalogItem`.

Inventory does not need to know how catalog items are stored or retrieved internally.

This illustrates an important modular design principle:

**A module should depend on another module's public API, not its internal implementation.**

---

## Declaring Allowed Module Dependencies

Spring Modulith allows an application to document and constrain dependencies between modules.

This example declares the inventory module's allowed dependency in `package-info.java`:

```java
@org.springframework.modulith.ApplicationModule(allowedDependencies = "catalog")
package com.springbyexample.v2.moduleverification.inventory;
```

The `@ApplicationModule` annotation identifies the package as an application module and declares its permitted dependencies.

In this example:

```text
inventory ──────► catalog
```

The inventory module is allowed to depend on the catalog module.

This declaration documents architectural intent and allows Spring Modulith to verify that the module's dependencies follow the declared rules.

The dependency declaration does not grant unrestricted access to every class in the catalog module. Internal package boundaries still apply.

---

## ApplicationModules

Spring Modulith provides the `ApplicationModules` API for inspecting and verifying an application's module structure.

The example uses:

```java
ApplicationModules.of(ModuleVerificationApplication.class)
```

This creates an application module model based on the application's main class.

Spring Modulith uses that entry point to discover the application's modules and analyze their relationships.

The resulting model can be used to inspect module information and verify architectural constraints.

---

## Verifying the Module Structure

The main architecture test is `ModuleVerificationTest`.

```java
@Test
void shouldVerifyApplicationModuleStructure() {
    ApplicationModules.of(ModuleVerificationApplication.class).verify();
}
```

The test performs two important operations.

### 1. Discover the Modules

```java
ApplicationModules.of(ModuleVerificationApplication.class)
```

This identifies the application's module structure based on the specified application class and its package hierarchy.

### 2. Verify the Architecture

```java
.verify();
```

This checks the discovered structure for architectural violations.

Depending on the module structure and configuration, verification can detect issues such as:

- Cyclic dependencies between modules.
- Dependencies on another module's internal packages.
- Dependencies that violate declared allowed-dependency rules.
- Other violations of Spring Modulith's module conventions.

If a violation is detected, the verification test fails and reports the architectural problem.

This makes architecture verification suitable for automated testing and continuous integration.

---

## Architectural Verification Flow

A simplified view of the verification process is:

```text
ModuleVerificationApplication
              │
              ▼
ApplicationModules.of(...)
              │
              ▼
Discover Modules
              │
              ▼
Analyze Dependencies
              │
              ▼
Check Module Boundaries
              │
              ▼
.verify()
              │
              ▼
Pass or Fail
```

The verification test does not need to start the application or execute service methods to analyze the module structure.

It focuses on the architecture itself.

---

## Detecting Architectural Violations

One of the benefits of module verification is that architectural mistakes become visible during testing.

For example, suppose `InventoryService` directly imports `CatalogStore`.

The dependency would look like:

```text
Inventory Module
       │
       ▼
CatalogStore
   (internal)
```

This bypasses the catalog module's public API.

Spring Modulith can detect such an internal package dependency during verification, causing the architecture test to fail.

Similarly, if two modules depend on each other in a way that creates a cycle, verification can identify the cyclic dependency.

These checks help prevent architectural drift as new features are introduced.

---

## Testing Module Verification

The example includes four tests with distinct responsibilities.

### `ModuleVerificationTest`

Verifies the application's module structure and architectural rules.

```java
@Test
void shouldVerifyApplicationModuleStructure() {
    ApplicationModules.of(ModuleVerificationApplication.class).verify();
}
```

### `CatalogStoreTest`

Tests the current sample implementation of `CatalogStore`.

The store returns the constant name `"Sample item"`.

Because this is a simple stub implementation, the test verifies only its current behavior.

### `CatalogServiceTest`

Uses a mocked `CatalogStore` to verify that `CatalogService` delegates the lookup and returns the expected catalog item.

The test checks the item's SKU and name, as well as the interaction with the store.

### `InventoryServiceTest`

Uses a mocked `CatalogService` to verify that `InventoryService` delegates the lookup and returns the expected catalog item.

These unit tests verify class behavior independently of the architecture test.

---

## Architecture Tests vs Unit Tests

Architecture tests and unit tests solve different problems.

### Unit Tests

Unit tests verify the behavior of individual classes.

For example:

```text
CatalogServiceTest
       │
       ▼
CatalogService
       │
       ▼
Mocked CatalogStore
```

The test verifies that the service returns the expected result and calls its dependency correctly.

### Architecture Tests

Architecture tests verify relationships and boundaries between application modules.

```text
Application
     │
     ▼
Application Modules
     │
     ▼
Module Dependencies
     │
     ▼
Architectural Rules
```

A unit test can pass even when modules have unwanted dependencies. An architecture test can pass even when a service contains a behavioral bug.

Both are useful, and they complement one another.

---

## Module Verification vs Application Startup

Module verification does not require a full application startup.

The test:

```java
ApplicationModules.of(ModuleVerificationApplication.class).verify();
```

analyzes the application's module structure directly.

By contrast, starting the Spring application exercises additional behavior, including bean creation, dependency injection, and application configuration.

These are related but distinct checks.

A project can use module verification alongside normal application-context tests and unit tests.

---

## Running the Example

From the repository root:

```bash
./mvnw -pl 04-spring-modulith/module-verification -am spring-boot:run
```

This starts the Spring Boot application after building the required Maven reactor dependencies.

---

## Running the Tests

Run the module's tests from the repository root:

```bash
./mvnw -pl 04-spring-modulith/module-verification -am test
```

To run the clean verification lifecycle:

```bash
./mvnw -pl 04-spring-modulith/module-verification -am clean verify
```

These commands assume that `module-verification` has been registered in the root Maven reactor.

---

## Experimenting with Module Verification

To understand how verification works, try deliberately introducing architectural violations.

### 1. Access an Internal Package

Modify `InventoryService` to depend directly on `CatalogStore`.

Run the architecture test and observe whether verification reports the internal package dependency.

### 2. Introduce a Cyclic Dependency

Create a dependency from the catalog module back to the inventory module while inventory already depends on catalog.

Run the verification test and inspect the reported cycle.

### 3. Add Another Module

Introduce a third module and declare its allowed dependencies using `@ApplicationModule`.

Observe how the declared rules affect verification.

### 4. Restore the Intended Architecture

Return to the design in which inventory depends on the catalog module's public API.

Rerun the tests and verify that the architecture test passes.

These experiments demonstrate how Spring Modulith can help enforce architectural intent automatically.

---

## Key Takeaways

- Spring Modulith can discover and verify application modules.
- `ApplicationModules.of(...).verify()` provides a straightforward way to test module architecture.
- `@ApplicationModule` can declare allowed dependencies between modules.
- Internal packages help encapsulate implementation details.
- Modules should generally interact through public APIs rather than internal implementation classes.
- Architectural verification can detect cyclic dependencies and other module boundary violations.
- Architecture tests and unit tests serve different purposes.
- Automated verification helps prevent architectural drift as an application grows.

---

## What This Example Does Not Cover

This example intentionally focuses on basic module verification.

It does not cover:

- Advanced module API declarations.
- Custom architectural rules.
- Module integration testing.
- Event-based communication between modules.
- Module documentation generation.
- Runtime deployment of independently deployable modules.
- A production persistence implementation for the catalog store.

These topics can be explored in separate examples.

---

## Next Example

The next example will explore **Documentation** in Spring Modulith, focusing on how to document application modules, visualize module dependencies, and generate documentation that makes a modular monolith’s architecture easier to understand and maintain.
