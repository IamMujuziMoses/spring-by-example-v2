# Module Documentation

This example demonstrates **module documentation** in Spring Modulith.

It focuses on how Spring Modulith can generate architectural documentation from an application's module structure, including module diagrams, individual module views, and module canvases.

The goal is to make a modular monolith's architecture easier to understand, communicate, and maintain.

## What This Example Demonstrates

- What module documentation means in Spring Modulith.
- Discovering application modules with `ApplicationModules`.
- Declaring module metadata with `@ApplicationModule`.
- Giving modules human-readable names with `displayName`.
- Generating an overview diagram with `writeModulesAsPlantUml()`.
- Generating individual module diagrams with `writeIndividualModulesAsPlantUml()`.
- Generating module canvases with `writeModuleCanvases()`.
- Aggregating documentation with `writeAggregatingDocument()`.
- Inspecting the generated documentation artifacts.
- The relationship between module verification and documentation generation.

---

## What Is Module Documentation?

Module documentation describes the structure and relationships of an application's modules.

As a modular monolith grows, understanding its architecture can become difficult. Developers need to know which modules exist, what they contain, and how they depend on one another.

Spring Modulith can inspect an application's module structure and generate documentation artifacts that help answer these questions.

A simplified flow looks like:

```text
Spring Application
        │
        ▼
Discover Application Modules
        │
        ▼
Analyze Module Structure
        │
        ▼
Generate Documentation
        │
        ├── Module Overview Diagram
        │
        ├── Individual Module Diagrams
        │
        ├── Module Canvases
        │
        └── Aggregated AsciiDoc Document
```

This allows architectural documentation to be generated from the structure of the application rather than maintained entirely by hand.

---

## Application Structure

This example contains two application modules:

- **Catalog** — provides catalog-related functionality.
- **Inventory** — consumes catalog functionality through its public API.

The relevant package structure is:

```text
com.springbyexample.v2.moduledocumentation
│
├── ModuleDocumentationApplication.java
│
├── catalog
│   ├── package-info.java
│   ├── CatalogItem.java
│   ├── CatalogService.java
│   └── internal
│       └── CatalogStore.java
│
└── inventory
    ├── package-info.java
    └── InventoryService.java
```

The catalog module contains a public service and an internal implementation class.

The inventory module depends on the catalog module through `CatalogService`.

Conceptually:

```text
Inventory
    │
    ▼
CatalogService
    │
    ▼
CatalogStore (internal)
```

This structure gives Spring Modulith module relationships that can be represented in generated documentation.

---

## Declaring Module Metadata

Spring Modulith uses `@ApplicationModule` to declare module metadata and, optionally, dependency rules.

### Catalog Module

The catalog module declares its display name in `catalog/package-info.java`:

```java
@org.springframework.modulith.ApplicationModule(displayName = "Catalog")
package com.springbyexample.v2.moduledocumentation.catalog;
```

The `displayName` attribute provides a human-readable name for the module.

### Inventory Module

The inventory module declares its display name and its allowed dependency in `inventory/package-info.java`:

```java
@org.springframework.modulith.ApplicationModule(displayName = "Inventory", allowedDependencies = "catalog")
package com.springbyexample.v2.moduledocumentation.inventory;
```

This declares that inventory is allowed to depend on catalog.

The module metadata helps Spring Modulith identify and describe the modules and their relationships.

The display name improves readability, while the dependency declaration documents the intended architectural relationship.

---

## ApplicationModules

Spring Modulith provides `ApplicationModules` to discover and model an application's module structure.

The example starts with:

```java
var modules = ApplicationModules.of(ModuleDocumentationApplication.class).verify();
```

The call:

```java
ApplicationModules.of(ModuleDocumentationApplication.class)
```

creates a model of the application's modules based on the specified application class.

The subsequent call:

```java
.verify()
```

checks the discovered module structure for architectural violations.

This ensures that the example verifies the module architecture before generating its documentation.

Module verification is useful, but it serves a different purpose from documentation generation.

- Verification checks architectural constraints.
- Documentation generation produces artifacts that help developers understand the architecture.

---

## Generating Module Documentation

The main example is `ModuleDocumentationTest`.

```java
@Test
void shouldGenerateModuleDocumentation() {
    var modules = ApplicationModules.of(ModuleDocumentationApplication.class).verify();

    new Documenter(modules).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml()
            .writeModuleCanvases().writeAggregatingDocument();
}
```

The test uses Spring Modulith's `Documenter` API to generate documentation from the discovered module structure.

Each method generates a different type of artifact.

---

## Module Overview Diagram

The example calls:

```java
.writeModulesAsPlantUml()
```

This generates an overview diagram representing the application's modules and their dependencies.

For this example, the intended relationship is:

```text
+----------------+
|   Inventory    |
+----------------+
        |
        | depends on
        ▼
+----------------+
|     Catalog    |
+----------------+
```

The generated diagram makes the relationship between inventory and catalog easier to understand at a glance.

The diagram is generated from the application's discovered module structure. The illustration above is a simplified conceptual representation, not the generated PlantUML source.

---

## Individual Module Diagrams

The example calls:

```java
.writeIndividualModulesAsPlantUml()
```

This generates a separate diagram for each application module.

Individual diagrams help developers examine a module in more detail without needing to interpret the entire application's architecture at once.

For example, the catalog module contains:

```text
Catalog
   │
   ▼
CatalogService
   │
   ▼
CatalogStore
```

The generated diagram can help readers understand the module's components and their relationships.

The precise elements shown depend on the application's structure and Spring Modulith's generated diagram representation.

---

## Module Canvases

The example calls:

```java
.writeModuleCanvases()
```

Module canvases provide a structured overview of individual modules and relevant architectural information.

They complement diagrams by presenting module information in a form that is easier to scan and use as documentation.

For example, a developer examining the inventory module can use its documentation to understand that it consumes the catalog module through `CatalogService`.

Module canvases are especially useful when an application contains many modules and developers need a consistent way to explore them.

---

## Aggregating the Documentation

The example calls:

```java
.writeAggregatingDocument()
```

This generates an aggregate AsciiDoc document, typically named:

```text
all-docs.adoc
```

The document brings the generated module documentation together and references the relevant diagrams and module canvases.

Instead of manually collecting every generated artifact, developers have a single document through which to navigate the documentation.

The aggregate document can serve as an input to a larger documentation workflow.

Generating AsciiDoc does not automatically publish a website or guarantee that every diagram has been rendered as an image. Rendering and publishing can be handled separately.

---

## Understanding the Generated Artifacts

The example generates four main types of documentation artifacts.

| Artifact                   | Purpose                                                           |
|----------------------------|-------------------------------------------------------------------|
| Module overview diagram    | Shows the application's modules and their relationships.          |
| Individual module diagrams | Provides a more detailed view of each module.                     |
| Module canvases            | Summarizes relevant information about individual modules.         |
| `all-docs.adoc`            | Aggregates the generated documentation into a navigable document. |

The exact generated filenames and directory layout can vary with the Spring Modulith version.

The generated files, rather than the test method itself, are the main output of this example.

---

## Where Is the Documentation Generated?

By default, Spring Modulith writes generated documentation beneath the build output directory.

The output is typically located at:

```text
module-documentation/
└── target/
    └── spring-modulith-docs/
```

After running the tests, inspect this directory to find the generated diagrams, module canvases, and aggregate AsciiDoc document.

The directory is generated build output, not automatically committed source documentation.

If the team wants to publish these artifacts or include them in version control, that can be configured as a separate workflow.

---

## Testing Module Documentation

The example uses a JUnit test to generate the documentation.

```java
@Test
void shouldGenerateModuleDocumentation() {
    var modules = ApplicationModules.of(ModuleDocumentationApplication.class).verify();

    new Documenter(modules).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml().writeModuleCanvases()
            .writeAggregatingDocument();
}
```

This test has two responsibilities.

### 1. Verify the Module Structure

```java
ApplicationModules.of(ModuleDocumentationApplication.class).verify();
```

This checks the application's module architecture.

If an architectural violation is detected, the test fails before documentation generation proceeds.

### 2. Generate the Documentation

```java
new Documenter(modules)
        .writeModulesAsPlantUml()
        .writeIndividualModulesAsPlantUml()
        .writeModuleCanvases()
        .writeAggregatingDocument();
```

This invokes the documentation generator.

If generation fails with an exception, the test fails.

However, the test does not independently assert the content of every generated file. Its main purpose is to demonstrate and execute the documentation-generation workflow.

To validate generated content more rigorously, a separate test could inspect expected output files or compare selected generated content with known expectations.

---

## Module Documentation vs Module Verification

Module documentation and module verification are related, but they answer different questions.

### Module Verification

Verification asks:

> Does the application's module structure follow the architectural rules?

```text
Application Modules
        │
        ▼
Check Dependencies
        │
        ▼
Detect Violations
        │
        ▼
Pass or Fail
```

### Module Documentation

Documentation asks:

> How can developers understand and explore the application's module structure?

```text
Application Modules
        │
        ▼
Generate Diagrams
        │
        ▼
Generate Module Canvases
        │
        ▼
Aggregate Documentation
```

Verification helps protect the architecture.

Documentation helps developers understand it.

Using both makes a modular monolith easier to maintain as it grows.

---

## Running the Example

From the repository root:

```bash
./mvnw -pl 04-spring-modulith/module-documentation -am test
```

This runs the module's tests together with required Maven reactor dependencies.

After the build completes, inspect the generated documentation under:

```text
04-spring-modulith/module-documentation/target/spring-modulith-docs/
```

The directory should contain the artifacts generated by the documentation test.

---

## Running the Full Verification Lifecycle

To clean the build output and run the full Maven verification lifecycle:

```bash
./mvnw -pl 04-spring-modulith/module-documentation -am clean verify
```

This is useful for confirming that the example works from a clean build.

---

## What This Example Does Not Cover

This example intentionally focuses on generating module documentation.

It does not cover:

- Custom documentation templates.
- Publishing generated documentation as a website.
- Rendering PlantUML diagrams into image files.
- Maintaining generated documentation in version control.
- Custom architectural rules.
- Module integration testing.
- Event-based communication between modules.
- Production persistence for the catalog store.

These topics can be explored separately when needed.

---

## Key Takeaways

- Spring Modulith can generate documentation from an application's module structure.
- `ApplicationModules` discovers and models application modules.
- `@ApplicationModule` can provide module metadata and declare allowed dependencies.
- `displayName` provides a human-readable module name.
- `Documenter` generates module diagrams, module canvases, and aggregated documentation.
- PlantUML artifacts provide visual representations of the module architecture.
- The generated AsciiDoc document brings the documentation artifacts together.
- Module documentation helps developers understand architectural relationships.
- Module verification and documentation generation serve different purposes.
- Documentation generation can be automated as part of a Maven test workflow.

---

## Related Technologies

- [Spring Modulith](https://spring.io/projects/spring-modulith)
- [Spring Modulith Reference Documentation](https://docs.spring.io/spring-modulith/reference/)
- [Spring Modulith Module Documentation](https://docs.spring.io/spring-modulith/reference/documentation.html)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [PlantUML](https://plantuml.com/)

---

## Next Example

The next example will explore **Module Testing** in Spring Modulith, focusing on how to test individual application modules in isolation, verify module boundaries, and ensure that modules interact correctly through their public APIs.
