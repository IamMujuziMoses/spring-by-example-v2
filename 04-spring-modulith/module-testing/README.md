# Module Testing — Spring Modulith

This example demonstrates how to test application modules using **Spring Modulith**. It explores how to test a module in isolation, include its direct dependencies when needed, and verify that the application maintains its intended modular architecture.

The example is part of the [`spring-by-example-v2`](https://github.com/IamMujiMoses/spring-by-example-v2) project.

## Overview

As applications grow, testing the entire application for every change can become slow and unnecessarily complex. Modular applications benefit from tests that focus on individual modules and their interactions.

Spring Modulith provides `@ApplicationModuleTest`, which allows developers to bootstrap a Spring application context focused on a particular application module.

This example uses two application modules:

- **Catalog** — provides catalog item information.
- **Inventory** — retrieves catalog items through the public Catalog service API.

It demonstrates three complementary testing approaches:

1. Unit testing individual classes with JUnit 5, Mockito, and AssertJ.
2. Testing a module in isolation with `@ApplicationModuleTest`.
3. Testing a module together with its direct dependencies.

An additional architecture test verifies that the module structure respects the declared dependency rules.

---

## Learning Objectives

By completing this example, you will understand how to:

- Use `@ApplicationModuleTest` to test Spring Modulith application modules.
- Bootstrap a module independently of its dependencies.
- Mock dependencies outside the module under test.
- Include direct module dependencies in an integration test.
- Distinguish unit tests from module integration tests.
- Verify application module boundaries and dependency constraints.
- Choose an appropriate testing boundary based on the behavior being tested.

---

## Application Design

### Catalog Module

The Catalog module exposes `CatalogService`, which retrieves a catalog item's name through `CatalogStore` and returns a `CatalogItem`.

The `CatalogStore` class represents a simplified data-access component. For this example, it returns a fixed item name rather than querying a database.

### Inventory Module

The Inventory module exposes `InventoryService`, which delegates catalog item retrieval to `CatalogService`.

Inventory depends on the public Catalog service API rather than accessing the Catalog module's internal implementation directly.

The module dependency is declared in `inventory/package-info.java`:

```java
@org.springframework.modulith.ApplicationModule(displayName = "Inventory", allowedDependencies = "catalog")
package com.springbyexample.v2.moduletesting.inventory;
```

This declaration makes the intended dependency explicit and allows Spring Modulith to verify the architecture.

---

## Testing Approaches

### 1. Unit Testing Individual Classes

The example includes unit tests for `CatalogService`, `CatalogStore`, and `InventoryService`.

These tests use JUnit 5 and AssertJ for assertions, with Mockito for dependency isolation where appropriate.

| Test class             | Responsibility                                                                |
|------------------------|-------------------------------------------------------------------------------|
| `CatalogStoreTest`     | Verifies the current store implementation returns the expected item name.     |
| `CatalogServiceTest`   | Verifies that the service uses the store result to construct a `CatalogItem`. |
| `InventoryServiceTest` | Verifies that Inventory delegates to Catalog and returns its result.          |

Unit tests do not require a Spring application context. They are useful for checking individual class behavior quickly and independently.

### 2. Testing a Module in Isolation

`InventoryModuleIsolatedTest` uses `@ApplicationModuleTest` with its default `STANDALONE` bootstrap mode.

```java
@ApplicationModuleTest
class InventoryModuleIsolatedTest {
    // Test implementation
}
```

The test bootstraps the Inventory module and replaces its external `CatalogService` dependency with a Mockito mock using `@MockitoBean`.

This allows the test to focus on Inventory's behavior without requiring the real Catalog module implementation.

This approach is useful when:

- Testing a module's behavior independently.
- Controlling the responses returned by external modules.
- Avoiding unnecessary startup and setup for dependent modules.
- Keeping tests focused on the module's own responsibilities.

### 3. Testing with Direct Dependencies

`InventoryModuleWithDependenciesTest` uses the following configuration:

```java
@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
class InventoryModuleWithDependenciesTest {
    // Test implementation
}
```

With `DIRECT_DEPENDENCIES`, Spring Modulith includes Inventory's direct dependencies in the test context.

The test uses the real `CatalogService` and `CatalogStore`, allowing it to verify the interaction between the two modules.

This approach is useful when you want to test module integration without loading every module in the application.

### Bootstrap Modes

Spring Modulith provides three main bootstrap modes:

| Mode                  | Behavior                                                                                                                        |
|-----------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `STANDALONE`          | Loads the module under test without loading its dependent modules as real modules. External dependencies may need to be mocked. |
| `DIRECT_DEPENDENCIES` | Loads the module under test and its direct dependencies.                                                                        |
| `ALL_DEPENDENCIES`    | Loads the module under test and all transitive dependencies.                                                                    |

Choose the mode that matches the scope of the test. Loading more modules can provide broader integration coverage, but it can also increase test setup and execution time.

### 4. Verifying the Application Architecture

`ModuleTestingArchitectureTest` uses `ApplicationModules` to verify the application's modular structure.

```java
ApplicationModules.of(ModuleTestingApplication.class).verify();
```

This test checks architectural rules such as dependency cycles and unauthorized access to other modules' internal packages.

Architecture verification complements behavioral testing: a test can pass while the code violates the intended modular design, so both checks are valuable.

---

## Running the Example

Run the tests from the repository root.

### Run all tests for this example

```bash
./mvnw -pl 04-spring-modulith/module-testing -am test
```

### Run the complete Maven verification lifecycle

```bash
./mvnw -pl 04-spring-modulith/module-testing -am clean verify
```

The `-pl` option selects the module-testing example, while `-am` also builds the Maven reactor projects it depends on.

---

## Key Takeaways

- Unit tests validate individual classes without starting Spring.
- `@ApplicationModuleTest` supports integration tests focused on a particular application module.
- `STANDALONE` is useful for isolated tests with mocked external dependencies.
- `DIRECT_DEPENDENCIES` allows a module and its direct dependencies to be tested together.
- `ALL_DEPENDENCIES` supports tests that need the full transitive dependency chain.
- `ApplicationModules.verify()` checks architectural constraints separately from application behavior.
- The right testing boundary depends on whether you are validating a class, a module, or an interaction between modules.

The main lesson is that **Spring Modulith lets you test modular applications at different levels without making every test an application-wide integration test**.

---

## Next Example

The next example moves beyond modular application design and begins **Module 05 - Spring Testing**.

The focus will shift from structuring applications into well-defined modules to testing Spring applications effectively, exploring how Spring’s testing support helps verify application behavior, test application contexts, isolate dependencies, and build reliable, maintainable tests.
