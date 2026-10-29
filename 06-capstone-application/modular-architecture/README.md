# Modular Architecture

This example demonstrates **modular application architecture with Spring Modulith**.

It focuses on how to organize a Spring Boot application into cohesive business modules, define module boundaries, control dependencies between modules, and use application events to communicate between modules without tightly coupling their implementations.

The example implements a small project and task management application with three business modules: **Project Management**, **Task Management**, and **Audit**.

## What This Example Demonstrates

- Organizing a Spring Boot application into business-focused modules.
- Defining application modules with `@ApplicationModule`.
- Using package structure to establish module boundaries.
- Separating public module APIs from internal implementation details.
- Managing dependencies between application modules.
- Using application events to communicate between modules.
- Publishing events with `ApplicationEventPublisher`.
- Handling application events with `@EventListener`.
- Implementing event-driven auditing.
- Exposing REST endpoints for projects, tasks, and audit entries.
- Testing individual services independently.
- Testing REST controllers with `MockMvc`.
- Testing interactions between modules with `@SpringBootTest`.
- Verifying application module structure with Spring Modulith.

---

## What Is Modular Architecture?

Modular architecture organizes an application into separate, cohesive units that represent distinct responsibilities or business capabilities.

Instead of placing all application logic into a single collection of controllers, services, and repositories, the application is divided into modules with explicit responsibilities and boundaries.

For example, a project management application can be organized into:

- **Project Management** — creates and retrieves projects.
- **Task Management** — creates and retrieves tasks associated with projects.
- **Audit** — records significant application events.

Each module owns its implementation details and exposes only the functionality other modules need to use.

Conceptually:

```text
Modular Application
        │
        ├── Project Management
        │
        ├── Task Management
        │
        └── Audit
```

The goal is not simply to divide code into packages. It is to establish meaningful boundaries that make the application easier to understand, maintain, test, and evolve.

---

## Why Modular Architecture?

As applications grow, business logic can become increasingly interconnected.

Without clear boundaries, a change to one feature can require changes throughout the application. Services may access one another's implementation details, creating dependencies that are difficult to understand and maintain.

A modular architecture helps address these problems by organizing related functionality together and controlling how modules interact.

For example:

```text
Without Clear Boundaries

Project Controller ───────► Task Service
       │                       │
       ▼                       ▼
  Audit Service ◄──────── Project Service
       │
       ▼
 Other Application Logic
```

When dependencies are not controlled, business logic can become difficult to isolate.

A modular design instead establishes explicit responsibilities:

```text
With Clear Module Boundaries

┌─────────────────────┐
│ Project Management  │
│                     │
│ Project API         │
│ Project Service     │
└──────────┬──────────┘
           │
           │ Public API
           ▼
┌─────────────────────┐
│ Task Management     │
│                     │
│ Task API            │
│ Task Service        │
└──────────┬──────────┘
           │
           │ TaskCreatedEvent
           ▼
┌─────────────────────┐
│ Audit               │
│                     │
│ Event Listener      │
│ Audit Service       │
└─────────────────────┘
```

The modules have distinct responsibilities, and their interactions are easier to reason about.

---

## Application Structure

The application has three main modules.

| Module    | Responsibility                                                  |
|-----------|-----------------------------------------------------------------|
| `project` | Creates and retrieves projects.                                 |
| `task`    | Creates and retrieves tasks and publishes task creation events. |
| `audit`   | Listens for task creation events and records audit entries.     |

The `internal` packages contain implementation details that are not intended to be used directly by other modules.

The project and task modules expose their service APIs and domain types through their main packages. The audit module keeps its implementation within its internal package because other modules do not need to depend on its implementation.

---

## The Application Entry Point

The application starts through `ModularArchitectureApplication`.

```java
@SpringBootApplication
@Modulithic
public class ModularArchitectureApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModularArchitectureApplication.class, args);
    }
}
```

`@SpringBootApplication` enables Spring Boot auto-configuration, component scanning, and configuration support.

`@Modulithic` identifies the application as a Spring Modulith application and enables Spring Modulith's application-module conventions and related infrastructure.

Spring Modulith can discover application modules from the application's package structure and analyze their dependencies.

This allows the module structure to become something that can be inspected and verified instead of relying exclusively on developer conventions.

---

## Defining Application Modules

Spring Modulith uses package structure and module metadata to identify application modules.

This example declares its modules using `@ApplicationModule` in each module's `package-info.java`.

### Project Management

```java
@ApplicationModule(displayName = "Project Management")
package com.springbyexample.v2.modulararchitecture.project;
```

### Task Management

```java
@ApplicationModule(displayName = "Task Management")
package com.springbyexample.v2.modulararchitecture.task;
```

### Audit

```java
@ApplicationModule(displayName = "Audit")
package com.springbyexample.v2.modulararchitecture.audit;
```

The `displayName` attribute provides a descriptive name for each module.

Using `package-info.java` keeps module metadata close to the package that defines the module boundary.

These declarations help Spring Modulith identify and document the application's business modules.

---

## Module Boundaries

A module boundary defines which parts of a module belong to its public interface and which parts are implementation details.

In this example, the `project` module exposes `ProjectService` and `Project` through its main package.

The `task` module can depend on these public types without depending on the project controller or other internal implementation details.

Conceptually:

```text
Project Management
│
├── Public API
│   ├── Project
│   ├── ProjectService
│   └── ProjectNotFoundException
│
└── Internal Implementation
    └── ProjectController
```

The same principle applies to Task Management.

```text
Task Management
│
├── Public API
│   ├── Task
│   ├── TaskService
│   ├── TaskCreatedEvent
│   └── TaskNotFoundException
│
└── Internal Implementation
    └── TaskController
```

The `internal` package communicates that these types are implementation details.

**Important:** A Java package named `internal` does not, by itself, enforce access restrictions. Java visibility rules and Spring Modulith's module verification are separate mechanisms that help enforce the intended boundaries.

A public type can still be part of an internal package, so package visibility and API design should be considered together.

---

## Project Management Module

The Project Management module is responsible for creating and retrieving projects.

A project contains:

```java
public record Project(Long id, String name, String description) {
}
```

The record represents a project using an identifier, name, and description.

The module's `ProjectService` provides operations for creating and retrieving projects.

Conceptually:

```text
Project Controller
        │
        ▼
Project Service
        │
        ├── Create Project
        │
        ├── Retrieve Project
        │
        └── List Projects
```

When a project is created, the service assigns an identifier and stores the project.

The service also validates the project name and trims surrounding whitespace.

If a requested project cannot be found, the service throws `ProjectNotFoundException`.

The controller exposes the service through REST endpoints.

### Project Endpoints

| Method | Endpoint                    | Purpose                   |
|--------|-----------------------------|---------------------------|
| `POST` | `/api/projects`             | Create a project.         |
| `GET`  | `/api/projects`             | Retrieve all projects.    |
| `GET`  | `/api/projects/{projectId}` | Retrieve a project by ID. |

The controller is an internal implementation detail of the Project Management module. Other modules should use the public service API rather than call the controller directly.

---

## Task Management Module

The Task Management module manages tasks associated with projects.

A task is represented by:

```java
public record Task(Long id, Long projectId, String title, boolean completed) {
}
```

Each task has an identifier, a project identifier, a title, and a completion status.

The `TaskService` provides operations for creating tasks and retrieving them by identifier or project.

### Creating a Task

When a task is created, the service:

1. Verifies that the associated project exists by calling `ProjectService`.
2. Validates the task title.
3. Creates a task with a unique identifier.
4. Stores the task.
5. Publishes a `TaskCreatedEvent`.
6. Returns the created task.

Conceptually:

```text
Create Task Request
        │
        ▼
Task Service
        │
        ▼
Validate Project
        │
        ▼
Validate Task Title
        │
        ▼
Create and Store Task
        │
        ▼
Publish TaskCreatedEvent
        │
        ▼
Return Task
```

The Task Management module depends on the public API of Project Management because a task must be associated with an existing project.

It does not need to depend on the project controller or project module's internal implementation.

### Task Endpoints

| Method | Endpoint                          | Purpose                                   |
|--------|-----------------------------------|-------------------------------------------|
| `POST` | `/api/tasks`                      | Create a task.                            |
| `GET`  | `/api/tasks`                      | Retrieve all tasks.                       |
| `GET`  | `/api/tasks/{taskId}`             | Retrieve a task by ID.                    |
| `GET`  | `/api/projects/{projectId}/tasks` | Retrieve tasks associated with a project. |

When a task references a project that does not exist, the service rejects the operation.

---

## Module Dependencies

Modules often need to collaborate, but their dependencies should remain intentional.

In this example, Task Management uses the Project Management service to validate project references.

```text
Task Management
       │
       │ Uses ProjectService
       ▼
Project Management
```

This is a direct dependency between two modules.

The dependency is appropriate because Task Management requires a project to exist before a task can be created.

The important distinction is that Task Management depends on the public API of Project Management rather than its internal implementation.

This helps keep the relationship understandable and reduces unnecessary coupling.

The Audit module, by contrast, does not need to call `TaskService` to discover when a task is created. It receives a published application event.

```text
Task Management
       │
       │ Publishes event
       ▼
TaskCreatedEvent
       │
       ▼
Audit
```

This event-based relationship reduces the need for the task creation workflow to know how auditing is implemented.

---

## Application Events

Application events allow one part of an application to announce that something has happened without directly invoking every component interested in that event.

In this example, Task Management publishes a `TaskCreatedEvent` after creating a task.

```java
public record TaskCreatedEvent(Long taskId, Long projectId, String title, Instant occurredAt) {
}
```

The event carries the information needed by interested components:

- The task identifier.
- The project identifier.
- The task title.
- The time the event occurred.

The event represents a business fact: a task has been created.

It does not contain the task controller or audit service, and it does not require the publisher to know which components will handle it.

---

## Publishing Application Events

The `TaskService` receives an `ApplicationEventPublisher` through constructor injection.

After creating and storing a task, it publishes the event:

```java
applicationEventPublisher.publishEvent(new TaskCreatedEvent(task.id(), task.projectId(), task.title(), Instant.now()));
```

The exact event construction should follow the implementation in `TaskService`.

The important part is that the service publishes an event describing a completed operation rather than directly calling the audit service.

Conceptually:

```text
TaskService
    │
    ├── Create Task
    │
    ├── Store Task
    │
    └── Publish TaskCreatedEvent
                 │
                 ▼
          Interested Listeners
```

This makes the task creation operation independent of the specific components that respond to the event.

Other components can subscribe to the same event in the future without requiring the task service to be modified for each new consumer.

---

## Handling Application Events

The Audit module listens for `TaskCreatedEvent` using Spring's `@EventListener`.

Conceptually:

```java
@Component
public class TaskCreatedEventListener {

    private final AuditService auditService;

    @EventListener
    public void onTaskCreated(TaskCreatedEvent event) {
        // Convert the event into an audit entry
        // and record it through AuditService.
    }
}
```

The listener receives the event and delegates audit entry creation to `AuditService`.

The resulting audit entry includes the event type, a description, the task ID, the project ID, and the event timestamp.

The listener belongs to the Audit module, so the task creation code does not need to know about the audit entry representation or audit storage.

### Event-Driven Communication

```text
┌────────────────────────┐
│ Task Management        │
│                        │
│ TaskService            │
└───────────┬────────────┘
            │
            │ publishEvent(...)
            ▼
┌────────────────────────┐
│ TaskCreatedEvent       │
└───────────┬────────────┘
            │
            │ @EventListener
            ▼
┌────────────────────────┐
│ Audit                  │
│                        │
│ TaskCreatedEventListener│
│ AuditService           │
└────────────────────────┘
```

The publisher and listener communicate through an event contract.

They do not need to reference one another's implementation classes.

---

## The Audit Module

The Audit module records task creation events.

An audit entry is represented by:

```java
public record AuditEntry(String eventType, String description, Long taskId, Long projectId, Instant occurredAt) {
}
```

When a task is created, the listener records an entry similar to:

```text
Event Type:  TASK_CREATED
Description: Task created: Verify audit event
Task ID:     1
Project ID:  1
Occurred At: <event timestamp>
```

The actual IDs and timestamp depend on the application state.

The `AuditService` maintains the recorded entries, and the audit controller exposes them through a REST endpoint.

### Audit Endpoint

| Method | Endpoint     | Purpose                 |
|--------|--------------|-------------------------|
| `GET`  | `/api/audit` | Retrieve audit entries. |

The Audit module depends on the public `TaskCreatedEvent` type, not on `TaskService` or the task controller.

This is a key example of reducing coupling through application events.

---

## Synchronous Event Handling

The example uses Spring's standard `ApplicationEventPublisher` and `@EventListener`.

With this configuration, the listener normally executes synchronously in the thread that publishes the event.

The simplified flow is:

```text
TaskService
    │
    ▼
Publish TaskCreatedEvent
    │
    ▼
TaskCreatedEventListener
    │
    ▼
AuditService
    │
    ▼
Audit Entry Recorded
    │
    ▼
Task Creation Call Returns
```

Because the listener is synchronous, audit processing normally occurs before the publishing call completes.

This example does not configure asynchronous event handling, durable messaging, or transactional event publication.

It therefore demonstrates in-process event-driven communication rather than a distributed messaging architecture.

For production systems, event delivery requirements, failure handling, transaction boundaries, and persistence should be considered explicitly.

---

## REST API Flow

The example exposes the modules through REST endpoints.

A typical task creation request follows this flow:

```text
HTTP POST /api/tasks
          │
          ▼
TaskController
          │
          ▼
TaskService
          │
          ├── Validate Project
          │
          ├── Create Task
          │
          └── Publish TaskCreatedEvent
                       │
                       ▼
                Audit Listener
                       │
                       ▼
                AuditService
          │
          ▼
HTTP 201 Created
```

The controller handles HTTP requests and responses. The service handles the task creation logic, and the event listener handles the audit side effect.

This separation keeps HTTP concerns, business operations, and auditing responsibilities distinct.

---

## Testing the Example

The example uses Spring Boot's test support, JUnit, Mockito, and Spring Modulith's testing support.

Different tests serve different purposes.

### Service Unit Tests

Service tests verify business logic independently of the web layer.

The Project Management tests cover project creation, name validation, trimming, retrieval, missing projects, and listing behavior.

The Task Management tests cover task creation, title validation, project validation, task retrieval, filtering by project, and event publication.

These tests help verify that each module's service behaves as expected without starting the entire application.

### Testing Event Publication

The Task Management service tests supply an `ApplicationEventPublisher` that captures published events.

Conceptually:

```text
TaskService Test
      │
      ▼
Create Task
      │
      ▼
Capture Published Event
      │
      ▼
Assert Event Contents
```

The test verifies that the event is published and contains the expected task ID, project ID, title, and timestamp.

It also verifies that invalid task creation does not publish a task-created event.

This tests the event publication contract without requiring the Audit module to be running.

### Audit Integration Tests

The audit integration tests use `@SpringBootTest` to load the application context and test the interaction between modules.

A representative flow is:

```text
Create Project
      │
      ▼
Create Task
      │
      ▼
TaskCreatedEvent Published
      │
      ▼
Audit Listener Invoked
      │
      ▼
Audit Entry Recorded
      │
      ▼
Verify Audit Entry
```

These tests verify that a task creation event results in an audit entry containing the expected information.

They also verify that attempting to create a task for a nonexistent project does not result in a new audit entry.

### MVC Tests

The controller tests use `@WebMvcTest` and `MockMvc` to test the REST API without starting a full application server.

For example, a project creation test can verify:

```java
mockMvc.perform(post("/api/projects")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
                {
                  "name": "Spring Modulith",
                  "description": "Learn modular architecture"
                }
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Spring Modulith"));
```

The service is replaced with a mock so the test can focus on HTTP behavior, request mapping, response status, and JSON output.

The controller tests cover successful requests, list endpoints, and error responses for missing projects or tasks.

### What the Different Tests Prove

| Test type                 | Main purpose                                                   |
|---------------------------|----------------------------------------------------------------|
| Service unit tests        | Verify business rules and service behavior.                    |
| Event publication tests   | Verify that task creation publishes the expected event.        |
| Audit integration tests   | Verify that events are handled and audit entries are recorded. |
| MVC tests                 | Verify HTTP endpoints, status codes, and JSON responses.       |
| Module verification tests | Verify the intended module structure and dependency rules.     |

Together, these tests cover individual components, communication between modules, and the application's external API.

---

## Spring Modulith Module Verification

Spring Modulith can inspect the application's module structure and identify dependencies that violate the intended architecture.

A typical verification test uses `ApplicationModules`:

```java
ApplicationModules modules = ApplicationModules.of(ModularArchitectureApplication.class);

modules.verify();
```

This test asks Spring Modulith to discover the application's modules and verify their architectural rules.

For example, it can help identify dependencies that cross module boundaries in ways that are not permitted by the module structure.

Module verification complements ordinary unit and integration tests: tests verify runtime behavior, while architectural verification checks structural constraints.

The exact verification rules depend on the module declarations and any additional rules configured in the application.

---

## In-Memory Storage

This example intentionally uses in-memory collections for project, task, and audit data.

That keeps the focus on module boundaries, dependencies, events, and testing rather than database configuration.

Consequently, the data is not durable. It is lost when the application context or application process is restarted, and this example does not demonstrate database transactions or persistence across restarts.

The in-memory implementation is suitable for illustrating the architectural concepts but should not be treated as a production persistence strategy.

---

## Running the Example

From the repository root, run:

```bash
mvn -pl 06-capstone-application/modular-architecture spring-boot:run
```

The application starts with the Project Management, Task Management, and Audit modules.

### Create a Project

```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Spring Modulith",
    "description": "Learn modular architecture"
  }'
```

### List Projects

```bash
curl http://localhost:8080/api/projects
```

### Retrieve a Project

```bash
curl http://localhost:8080/api/projects/1
```

### Create a Task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": 1,
    "title": "Explore application modules"
  }'
```

Creating a task publishes a `TaskCreatedEvent`. The Audit module handles the event and records an audit entry.

### List Tasks

```bash
curl http://localhost:8080/api/tasks
```

### Retrieve Tasks for a Project

```bash
curl http://localhost:8080/api/projects/1/tasks
```

### Retrieve Audit Entries

```bash
curl http://localhost:8080/api/audit
```

The audit endpoint returns the entries recorded by the event listener during the current application run.

---

## Running the Tests

Run the tests for this module from the repository root:

```bash
mvn -pl 06-capstone-application/modular-architecture test
```

To run the module's tests and the verification lifecycle, including compilation and other configured checks, use:

```bash
mvn -pl 06-capstone-application/modular-architecture -am clean verify
```

The `-am` option also builds required reactor projects, and `clean verify` runs the Maven lifecycle through the verification phase.

---

## Key Takeaways

- Modular architecture organizes an application around cohesive business responsibilities.
- Spring Modulith helps identify and verify application modules.
- `@Modulithic` marks the Spring Boot application for Spring Modulith support.
- `@ApplicationModule` declares module metadata.
- Package structure helps communicate module boundaries.
- Public APIs allow modules to collaborate without depending on internal implementation details.
- Task Management depends on Project Management to validate project references.
- `ApplicationEventPublisher` allows services to publish application events.
- `@EventListener` allows other modules to react to those events.
- Event-driven communication reduces direct coupling between the task creation workflow and the audit implementation.
- Unit tests, MVC tests, and integration tests verify different aspects of the application.
- Spring Modulith verification can help detect unwanted module dependencies.
- In-memory storage keeps the example focused but does not provide persistence or durable event delivery.

---

## Next Example

The next example can explore **application module testing and architectural verification in greater depth**, including module dependency rules, module integration testing, and techniques for preventing architectural erosion as an application grows.
