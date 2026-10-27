# Application Design and Architecture

This example demonstrates **application design and architecture** in a Java application, using a small project and task management domain to explore separation of concerns, domain modeling, application services, repository abstractions, and unit testing.

It focuses on how to structure an application so that its business rules remain independent of application workflows and persistence implementations.

The example uses plain Java and JUnit 6 to demonstrate architectural principles without introducing a web framework, database, or unnecessary infrastructure.

## What This Example Demonstrates

- Separation of the domain and application layers.
- Organizing code by architectural responsibility.
- Modeling domain concepts with immutable Java records.
- Using value objects to represent project and task identifiers.
- Encapsulating business rules in the domain model.
- Representing task lifecycle states with enums.
- Enforcing valid task state transitions.
- Defining repository interfaces as persistence abstractions.
- Implementing application services for project and task use cases.
- Handling missing projects and tasks with meaningful exceptions.
- Testing domain rules independently of application services.
- Testing application services with in-memory repository implementations.
- Applying dependency inversion to keep business logic independent of persistence details.

---

## What Is Application Design and Architecture?

Application design and architecture describe how an application is organized, how its components interact, and where different responsibilities belong.

A well-structured application separates business rules from the workflows that use those rules and from the infrastructure that stores or transports data.

For example, a project management application might need to:

- Create projects.
- Create tasks associated with projects.
- Retrieve projects and tasks.
- List tasks belonging to a project.
- Move tasks through their lifecycle.

These operations involve different responsibilities. The domain model defines what a project or task is and which business rules apply. Application services coordinate operations, while repository interfaces define how the application accesses stored objects.

The central architectural idea is:

> Each component should have a clear responsibility and depend only on the abstractions it needs.

---

## Application Overview

This example models a simplified project and task management system.

A project represents a unit of work. A task belongs to a project and progresses through a predefined lifecycle.

The application supports the following use cases:

| Use case           | Responsibility                                           |
|--------------------|----------------------------------------------------------|
| Create a project   | Construct and save a valid project                       |
| Retrieve a project | Find a project or report that it does not exist          |
| Create a task      | Verify the parent project, construct a task, and save it |
| Retrieve a task    | Find a task or report that it does not exist             |
| List project tasks | Verify the project and retrieve its tasks                |
| Transition a task  | Apply domain rules and save the updated task             |

These use cases provide enough behavior to demonstrate meaningful architectural boundaries without introducing unrelated complexity.

---

## Architecture and Package Structure

The example uses two main layers: the **domain layer** and the **application layer**.

Each package has a specific responsibility.

### Domain Layer

The domain layer contains the application's business concepts and rules.

It defines projects, tasks, identifiers, task statuses, and the exception used when a task transition violates the domain rules.

The domain does not depend on application services or repository implementations.

### Application Layer

The application layer coordinates use cases.

Application services validate the inputs they need, retrieve domain objects through repository interfaces, invoke domain behavior, and save the results.

The application layer depends on the domain model and repository abstractions rather than a particular persistence technology.

### Why Separate These Layers?

Without clear boundaries, business rules can become scattered across controllers, database code, and service methods.

Separating the layers makes it easier to:

- Understand where a rule belongs.
- Test business behavior without starting an application context.
- Replace persistence implementations without rewriting domain logic.
- Extend use cases without placing every responsibility in one class.
- Maintain the application as its requirements evolve.

---

## Dependency Direction

The example follows a dependency direction in which application services use the domain model and repository interfaces.

```text
Application Service
       │
       ├──────────────► Domain Model
       │
       └──────────────► Repository Interface
                              ▲
                              │
                     Repository Implementation
                     (future infrastructure)
```

The repository implementation is not included in the production code in this example. The tests provide in-memory implementations to demonstrate the contracts.

The important point is that application services depend on `ProjectRepository` and `TaskRepository`, not on a concrete database implementation.

This is an application of **dependency inversion**: higher-level application behavior depends on abstractions rather than being coupled to specific infrastructure.

---

## Domain Modeling

Domain modeling represents the important concepts of a problem using types that express their meaning and enforce their rules.

This example models two primary concepts:

- `Project`
- `Task`

It also uses dedicated identifier types and an enum to make the model more expressive.

### Project

A project contains:

- A project identifier.
- A name.
- A description.

The project model enforces basic validity rules. Its identifier and name cannot be null, and the name cannot be blank.

A project is represented as a Java record, which provides a concise way to model immutable data.

Conceptually:

```text
Project
  ├── ProjectId
  ├── name
  └── description
```

The project model is responsible for maintaining its own basic invariants. It does not need to know how projects are stored or which application service created them.

### ProjectId

`ProjectId` represents the identity of a project.

Instead of passing an arbitrary string everywhere, the application uses a dedicated type.

```text
ProjectId
    │
    ▼
Project
```

The identifier validates that its value is not null or blank.

A dedicated identifier type improves readability and reduces accidental mixing of identifiers from different domain concepts.

### Task

A task contains:

- A task identifier.
- The identifier of its parent project.
- A title.
- A description.
- A status.

Conceptually:

```text
Task
  ├── TaskId
  ├── ProjectId
  ├── title
  ├── description
  └── TaskStatus
```

A task must have a valid identifier, parent project identifier, title, and status. The title cannot be blank.

The task also contains the rules that govern its lifecycle. Those rules belong in the domain model rather than being duplicated across application services.

### TaskId

`TaskId` represents the identity of a task.

Like `ProjectId`, it validates that the supplied identifier is not null or blank.

Although both identifiers use strings internally, they represent different concepts and have different Java types.

This makes the model more explicit and helps prevent accidental confusion between project IDs and task IDs.

---

## Task Lifecycle and Business Rules

Tasks move through a defined sequence of states.

The example uses the `TaskStatus` enum:

```java
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED
}
```

The lifecycle is intentionally simple.

```text
TODO
  │
  ▼
IN_PROGRESS
  │
  ▼
COMPLETED
```

The following transitions are permitted:

| Current status | Requested status | Result   |
|----------------|------------------|----------|
| `TODO`         | `IN_PROGRESS`    | Allowed  |
| `IN_PROGRESS`  | `COMPLETED`      | Allowed  |
| `TODO`         | `COMPLETED`      | Rejected |
| `COMPLETED`    | `TODO`           | Rejected |
| `COMPLETED`    | `IN_PROGRESS`    | Rejected |

The task's `transitionTo` method enforces these rules.

This is an important design decision: **the domain object protects its own business invariants**.

If transition rules were implemented only in an application service, another caller could potentially bypass them by interacting with the domain object through a different path.

Keeping the rules in the domain model gives the application one authoritative place to determine whether a transition is valid.

### Immutable State Transitions

The `Task` record is immutable. Rather than modifying the existing task, `transitionTo` returns a new task with the requested status.

Conceptually:

```text
Original Task
status = TODO
      │
      ▼
transitionTo(IN_PROGRESS)
      │
      ▼
New Task
status = IN_PROGRESS
```

The original task remains unchanged.

This makes state changes explicit, simplifies reasoning about object state, and makes the transition behavior straightforward to test.

### TaskTransitionException

When a transition violates the permitted lifecycle, the domain throws `TaskTransitionException`.

This communicates that the requested operation conflicts with a business rule.

The exception belongs to the domain layer because it describes an invalid domain operation rather than a failure to locate or persist an object.

---

## Repository Abstractions

Repositories define how application services access domain objects without specifying the underlying storage mechanism.

The example defines two interfaces.

### ProjectRepository

The project repository provides operations to save a project and find one by its identifier.

```java
Project save(Project project);

Optional<Project> findById(ProjectId id);
```

The `Optional<Project>` return type makes the possibility of a missing project explicit.

The interface does not prescribe whether projects are stored in a relational database, a document store, or another persistence mechanism.

### TaskRepository

The task repository provides operations to save a task, find a task by its identifier, and retrieve tasks belonging to a project.

```java
Task save(Task task);

Optional<Task> findById(TaskId id);

List<Task> findByProjectId(ProjectId projectId);
```

These operations represent the persistence needs of the application's task-related use cases.

The interface does not expose database-specific queries or implementation details.

### Why Use Repository Interfaces?

Repository abstractions help isolate application behavior from persistence concerns.

They make it possible to:

- Use in-memory implementations during unit testing.
- Introduce a database-backed implementation later.
- Change persistence technology without redesigning application services.
- Express the application's data-access needs through a small, focused contract.

The interfaces are intentionally small. They contain the operations required by the current use cases rather than attempting to anticipate every possible future query.

---

## Application Services

Application services coordinate domain objects and repositories to implement application use cases.

They do not replace the domain model. Instead, they orchestrate operations using the rules and behaviors that the domain model provides.

### ProjectApplicationService

`ProjectApplicationService` implements the project-related use cases.

Its responsibilities include:

- Creating and saving a project.
- Retrieving a project by its identifier.
- Reporting when a requested project does not exist.

When creating a project, the service constructs the domain object and passes it to `ProjectRepository`.

When retrieving a project, the service looks it up through the repository. If the repository returns an empty result, the service throws `ProjectNotFoundException`.

Conceptually:

```text
Create Project
      │
      ▼
ProjectApplicationService
      │
      ▼
Construct Project
      │
      ▼
ProjectRepository.save(...)
      │
      ▼
Saved Project
```

For retrieval:

```text
Get Project
      │
      ▼
ProjectApplicationService
      │
      ▼
ProjectRepository.findById(...)
      │
      ├── Found ─────► Return Project
      │
      └── Missing ───► ProjectNotFoundException
```

The service does not need to know how the repository stores or retrieves the project.

### TaskApplicationService

`TaskApplicationService` implements the task-related use cases.

Its responsibilities include:

- Creating a task for an existing project.
- Retrieving a task by its identifier.
- Listing tasks for a project.
- Transitioning a task to a new status.
- Saving the updated task after a valid transition.

#### Creating a Task

A task belongs to a project, so the service first verifies that the referenced project exists.

If the project does not exist, the operation fails with `ProjectNotFoundException`.

If it exists, the service constructs a task in the initial `TODO` state and saves it through `TaskRepository`.

```text
Create Task
     │
     ▼
TaskApplicationService
     │
     ▼
Verify Project Exists
     │
     ├── Missing ─────► ProjectNotFoundException
     │
     ▼
Create Task (TODO)
     │
     ▼
TaskRepository.save(...)
     │
     ▼
Saved Task
```

This use case demonstrates how an application service coordinates multiple dependencies while keeping domain validation inside the domain model.

#### Retrieving a Task

The service requests the task from `TaskRepository`.

If the task cannot be found, it throws `TaskNotFoundException`.

This makes the missing-task behavior explicit to callers.

#### Listing Tasks for a Project

Before retrieving tasks, the service verifies that the project exists.

It then asks the repository for the tasks associated with the project's identifier.

The service returns an immutable copy of the resulting list, preventing callers from modifying the repository's returned list through the service response.

#### Transitioning a Task

The service retrieves the existing task, delegates the transition decision to the domain object, and saves the resulting task.

```text
Transition Task
       │
       ▼
TaskApplicationService
       │
       ▼
Retrieve Task
       │
       ▼
Task.transitionTo(...)
       │
       ├── Invalid ─────► TaskTransitionException
       │
       ▼
TaskRepository.save(...)
       │
       ▼
Updated Task
```

The service coordinates the use case, while `Task` decides whether the transition is valid.

This separation keeps application workflow and domain behavior distinct.

---

## Exception Design

The example uses separate exceptions for different kinds of failure.

| Exception                  | Layer       | Meaning                                              |
|----------------------------|-------------|------------------------------------------------------|
| `ProjectNotFoundException` | Application | The requested project does not exist                 |
| `TaskNotFoundException`    | Application | The requested task does not exist                    |
| `TaskTransitionException`  | Domain      | A requested task transition violates a business rule |

This separation makes failures more descriptive.

A missing project is a problem encountered while executing an application use case. An invalid task transition is a violation of the domain's lifecycle rules.

The distinction also helps future adapters, such as a REST API, decide how to translate application and domain failures into appropriate responses.

This example does not implement such an adapter.

---

## Testing the Architecture

The example uses JUnit to test the domain model and application services independently.

The tests do not require a Spring application context or a running database.

This is intentional: the example demonstrates application design and architecture rather than Spring Boot configuration or persistence integration.

### Domain Tests

`ProjectDomainTest` verifies that:

- A valid project can be created.
- Project properties are retained correctly.
- A blank project name is rejected.
- A blank project identifier is rejected.

`TaskDomainTest` verifies that:

- A task begins in the `TODO` state.
- A valid transition returns a new task.
- The original task remains unchanged after a transition.
- A task can move from `IN_PROGRESS` to `COMPLETED`.
- Invalid transitions throw `TaskTransitionException`.

These tests focus on domain behavior without involving repositories or application services.

### Application Service Tests

`ProjectApplicationServiceTest` verifies that:

- A project can be created and retrieved.
- Retrieving a missing project throws `ProjectNotFoundException`.

`TaskApplicationServiceTest` verifies that:

- A task can be created for an existing project.
- Creating a task for a missing project fails.
- A task can be transitioned and the updated task saved.
- Tasks can be listed for a project.
- Retrieving a missing task throws `TaskNotFoundException`.

### In-Memory Repositories

The application service tests define small in-memory repository implementations using Java collections.

These implementations satisfy the repository interfaces without requiring a database.

Conceptually:

```text
Application Service Test
          │
          ▼
Application Service
          │
          ▼
Repository Interface
          │
          ▼
In-Memory Repository
          │
          ▼
Java Collection
```

This approach allows the tests to exercise application workflows while keeping them fast and independent of external infrastructure.

The in-memory implementations are test fixtures, not production persistence implementations.

---

## Why This Example Does Not Use Spring

Although this repository explores the Spring ecosystem, this example deliberately uses plain Java and JUnit.

The architectural boundaries can be understood before introducing dependency injection, application contexts, web controllers, or database integrations.

The services receive their dependencies through constructors. Tests instantiate the services directly and provide in-memory repositories.

Spring could later manage these services and their dependencies, but the domain and application design should not require a Spring application context to be understood or tested.

This keeps the example focused on architectural fundamentals.

---

## Running the Example

From the repository root, run the tests for this module:

```bash
mvn -pl 06-capstone-application/application-design-and-architecture test
```

To run verification, including the checks configured by the project:

```bash
mvn -pl 06-capstone-application/application-design-and-architecture verify
```

To clean and verify the module:

```bash
mvn -pl 06-capstone-application/application-design-and-architecture clean verify
```

These commands assume the module has been added to the root Maven project's `<modules>` section.

---

## Key Takeaways

- Good application architecture separates domain rules from application workflows and infrastructure.
- Domain models should enforce their own business invariants.
- Dedicated identifier types make domain concepts more explicit.
- Immutable objects make state changes easier to reason about.
- Application services coordinate use cases without owning every business rule.
- Repository interfaces decouple application behavior from persistence implementations.
- Application and domain exceptions should communicate different kinds of failure.
- In-memory repositories allow application services to be tested without a database.
- Plain Java and JUnit are sufficient to demonstrate these architectural principles before introducing Spring infrastructure.
- Clear boundaries make an application easier to test, maintain, and extend.

---

## What This Example Does Not Cover

This example intentionally focuses on application design and architecture.

It does not cover:

- Spring Boot application configuration.
- REST controllers or HTTP APIs.
- Database integration or ORM mapping.
- Concrete production repository implementations.
- Dependency injection through a Spring application context.
- Authentication and authorization.
- Transaction management.
- Distributed systems or messaging.
- Domain events or Spring Modulith.
- Deployment, observability, or production infrastructure.

These concerns can be introduced in later examples once the core architectural boundaries are established.

---

## Next Example

The next example will introduce **Spring Boot Application Setup**, establishing the foundation for the capstone application by configuring Spring Boot, creating the main application class, and setting up the application context.

This will prepare the application for the subsequent integration of Spring Data and persistence, REST APIs, Spring Security, transaction management, Spring Modulith, and application testing.
