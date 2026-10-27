# Spring Data and Persistence

This example demonstrates how to integrate **Spring Data JPA and relational database persistence** into a Spring application.

It builds on the `application-design-and-architecture` example by connecting the existing domain model and application services to a relational database. It demonstrates how to persist projects and tasks using Spring Data JPA repositories, JPA entities, persistence mappers, repository adapters, and transactional service methods.

The example uses an in-memory H2 database to keep the application easy to run and test without requiring an external database server.

## What This Example Demonstrates

- Integrating Spring Data JPA into a Spring Boot application.
- Configuring an in-memory H2 relational database.
- Mapping domain objects to JPA entities and back.
- Defining JPA entities for projects and tasks.
- Using `JpaRepository` to perform database operations.
- Implementing domain repository interfaces with JPA-backed adapters.
- Separating domain models from persistence entities.
- Representing relationships between projects and tasks.
- Persisting task statuses using enums.
- Using Spring's `@Transactional` annotation.
- Reading and writing data through application services.
- Testing persistence adapters with `@DataJpaTest`.
- Testing transactional service operations with `@SpringBootTest`.
- Persisting task status transitions and retrieving the updated state.

---

## What Is Spring Data JPA?

Spring Data JPA simplifies data access in applications that use the Java Persistence API (JPA).

Instead of implementing common database operations manually, an application can extend `JpaRepository` to obtain operations such as:

- Saving an entity.
- Finding an entity by its identifier.
- Retrieving all entities.
- Deleting entities.
- Counting stored entities.

For example:

```java
public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, String> {
}
```

Spring Data JPA creates an implementation of this interface at runtime.

The application can then save and retrieve projects without writing SQL for basic CRUD operations.

Spring Data JPA builds on JPA, while the JPA provider, Hibernate by default in this configuration, handles the object-relational mapping and database interactions.

---

## Why Persistence Matters

The application design and architecture example defines domain objects and application services without depending on a particular database.

However, domain objects stored only in memory do not automatically survive application restarts.

Persistence introduces a mechanism for storing application data in a database and reconstructing domain objects when that data is needed again.

The relationship between the application architecture and persistence can be represented as:

```text
Application Service
        │
        ▼
Domain Repository Interface
        │
        ▼
Repository Adapter
        │
        ▼
Spring Data JPA Repository
        │
        ▼
JPA Entity
        │
        ▼
Relational Database
```

The repository interface belongs to the application architecture. The JPA repository, entity, and adapter belong to the persistence implementation.

This separation allows the domain model and application services to remain independent of database-specific implementation details.

---

## Architecture of This Example

This example extends the capstone application's existing architecture instead of replacing it.

```text
┌───────────────────────────────────────────┐
│             Application Layer             │
│                                           │
│  ProjectApplicationService                │
│  TaskApplicationService                   │
│                                           │
│  ProjectRepository / TaskRepository       │
└────────────────────┬──────────────────────┘
                     │
                     ▼
┌───────────────────────────────────────────┐
│              Persistence Layer            │
│                                           │
│  ProjectRepositoryAdapter                 │
│  TaskRepositoryAdapter                    │
│                                           │
│  ProjectPersistenceMapper                 │
│  TaskPersistenceMapper                    │
│                                           │
│  ProjectJpaRepository                     │
│  TaskJpaRepository                        │
│                                           │
│  ProjectEntity / TaskEntity               │
└────────────────────┬──────────────────────┘
                     │
                     ▼
┌───────────────────────────────────────────┐
│              H2 Database                  │
│                                           │
│  projects                                 │
│  tasks                                    │
└───────────────────────────────────────────┘
```

Each layer has a distinct responsibility.

### Application Layer

The application layer defines the operations the application supports, such as creating projects, creating tasks, retrieving tasks, and transitioning task statuses.

It depends on repository interfaces rather than JPA repositories or database entities.

### Persistence Layer

The persistence layer implements those repository interfaces.

It translates between domain objects and JPA entities, delegates database operations to Spring Data JPA, and retrieves the data needed by the application layer.

### Database

The database stores the projects and tasks as relational records.

H2 is used here because it provides a lightweight database suitable for demonstrations and automated tests.

---

## Domain Models vs JPA Entities

An important design decision in this example is keeping domain models separate from JPA entities.

### Domain Model

The domain model represents the application's business concepts and rules.

For example, the existing `Project` domain record represents a project using its identifier, name, and description:

```java
Project project = new Project(
    new ProjectId("project-1"),
    "Capstone Application",
    "A project for demonstrating Spring technologies"
);
```

The domain model does not need JPA annotations to represent a project.

Similarly, the `Task` domain model represents a task and its relationship to a project using domain identifiers.

### JPA Entity

A JPA entity represents data that can be persisted through JPA.

The `ProjectEntity` maps to the `projects` database table:

```java
@Entity
@Table(name = "projects")
public class ProjectEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    private String name;

    @Column(length = 2000)
    private String description;

    protected ProjectEntity() {
    }

    public ProjectEntity(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
```

The entity uses JPA annotations to describe how its fields map to database columns.

The protected no-argument constructor allows the JPA provider to instantiate the entity.

The distinction is intentional:

| Domain model                   | JPA entity                            |
|--------------------------------|---------------------------------------|
| Represents business concepts   | Represents persisted data             |
| Uses domain identifiers        | Uses persistence identifiers          |
| Contains business rules        | Contains persistence mapping metadata |
| Independent of JPA annotations | Uses JPA annotations                  |
| Used by application services   | Used by persistence infrastructure    |

This separation reduces coupling between business logic and database implementation details.

---

## Mapping Domain Objects to JPA Entities

Because the domain model and JPA entities serve different purposes, the persistence layer needs a way to translate between them.

This example uses dedicated persistence mappers.

### ProjectPersistenceMapper

The `ProjectPersistenceMapper` converts a domain `Project` into a `ProjectEntity` and converts a persisted entity back into a domain object.

```java
@Component
public class ProjectPersistenceMapper {

    public ProjectEntity toEntity(Project project) {
        return new ProjectEntity(project.id().value(), project.name(), project.description());
    }

    public Project toDomain(ProjectEntity entity) {
        return new Project(new ProjectId(entity.getId()), entity.getName(), entity.getDescription());
    }
}
```

The `toEntity` method extracts values from the domain object to construct a JPA entity.

The `toDomain` method reconstructs the domain object from the persisted entity.

The application layer therefore does not need to know how JPA entities are represented.

### TaskPersistenceMapper

The `TaskPersistenceMapper` performs the equivalent conversion for tasks.

A task also has a relationship to a project, so the mapper receives the corresponding `ProjectEntity` when constructing a `TaskEntity`.

```java
@Component
public class TaskPersistenceMapper {

    public TaskEntity toEntity(Task task, ProjectEntity project) {
        return new TaskEntity(task.id().value(), project, task.title(), task.description(), task.status());
    }

    public Task toDomain(TaskEntity entity) {
        return new Task(new TaskId(entity.getId()), 
                new ProjectId(entity.getProject().getId()), entity.getTitle(), entity.getDescription(), entity.getStatus()
        );
    }
}
```

The task mapper uses the domain `TaskStatus` enum directly. The entity and domain model therefore share the same task status values without requiring a second, duplicate enum.

The mapper also converts the entity's project relationship back into a domain `ProjectId`.

---

## Defining JPA Repositories

Spring Data JPA provides repository implementations through interfaces.

### ProjectJpaRepository

```java
public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, String> {
}
```

The first generic parameter is the entity type, and the second is the identifier type.

This repository provides common operations such as:

```java
repository.save(projectEntity);
repository.findById("project-1");
repository.findAll();
repository.count();
```

There is no need to implement these standard operations manually.

### TaskJpaRepository

```java
public interface TaskJpaRepository extends JpaRepository<TaskEntity, String> {

    List<TaskEntity> findAllByProject_Id(String projectId);
}
```

The `findAllByProject_Id` method is a derived query.

Spring Data JPA interprets the method name and generates a query that finds tasks associated with the specified project's identifier.

The underscore explicitly separates the `project` property from its nested `id` property.

Conceptually, this query retrieves tasks matching the relationship:

```text
Project ID: project-1
        │
        ▼
Tasks belonging to project-1
        │
        ├── Task A
        ├── Task B
        └── Task C
```

This demonstrates how Spring Data JPA can derive queries from repository method names without requiring a manually written query for this operation.

---

## Modeling the Project–Task Relationship

A project can have multiple tasks. Each task belongs to one project.

The relationship is represented in the persistence model with a many-to-one association:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "project_id", nullable = false)
private ProjectEntity project;
```

The `@ManyToOne` annotation declares that multiple tasks can reference the same project.

The `@JoinColumn` annotation specifies the foreign-key column in the `tasks` table.

The `optional = false` and `nullable = false` settings express that a task must have a project.

### Why `FetchType.LAZY`?

Lazy fetching means the associated project is not necessarily loaded immediately when a task entity is retrieved.

Instead, the persistence provider can load the project when the relationship is accessed.

This can reduce unnecessary database work when the application does not need the associated project.

However, accessing lazy relationships requires appropriate persistence-context management. This example uses transactional operations and maps the required project identifier while the persistence context is available.

### Relational Representation

```text
projects
┌────────────┬───────────────────────┐
│ id         │ name                  │
├────────────┼───────────────────────┤
│ project-1  │ Capstone Application  │
└────────────┴───────────────────────┘
              │
              │ project_id
              ▼
tasks
┌──────────┬────────────┬───────────────┐
│ id       │ project_id │ title         │
├──────────┼────────────┼───────────────┤
│ task-1   │ project-1  │ Design model  │
│ task-2   │ project-1  │ Add tests     │
└──────────┴────────────┴───────────────┘
```

The `project_id` foreign key associates each task with its project.

---

## Implementing the Repository Adapters

The domain repository interfaces define the operations required by the application. The adapters implement those operations using Spring Data JPA.

This follows the dependency direction established in the application design example: the application layer depends on abstractions, while the persistence layer supplies their implementations.

### ProjectRepositoryAdapter

The project adapter implements the existing `ProjectRepository` interface.

```java
@Repository
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final ProjectJpaRepository repository;
    
    private final ProjectPersistenceMapper mapper;

    public ProjectRepositoryAdapter(ProjectJpaRepository repository, ProjectPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Project save(Project project) {
        return mapper.toDomain(
            repository.save(mapper.toEntity(project))
        );
    }

    @Override
    public Optional<Project> findById(ProjectId id) {
        return repository.findById(id.value())
            .map(mapper::toDomain);
    }
}
```

When the application saves a project, the adapter:

1. Receives a domain `Project`.
2. Converts it to a `ProjectEntity`.
3. Delegates persistence to `ProjectJpaRepository`.
4. Converts the saved entity back to a domain `Project`.
5. Returns the domain object to the caller.

The adapter also converts the domain identifier to its underlying value when looking up a project.

### TaskRepositoryAdapter

The task adapter implements `TaskRepository`.

Its responsibilities include saving tasks, retrieving individual tasks, and finding tasks belonging to a project.

Saving a task requires resolving the associated project entity before constructing the `TaskEntity`.

Conceptually:

```text
Domain Task
    │
    ▼
Resolve ProjectEntity
    │
    ▼
TaskPersistenceMapper
    │
    ▼
TaskEntity
    │
    ▼
TaskJpaRepository
    │
    ▼
Saved TaskEntity
    │
    ▼
Domain Task
```

This ensures that the task's project relationship is represented by the corresponding JPA entity.

If the referenced project cannot be found, the adapter fails instead of persisting a task with a nonexistent project.

The adapter also implements `findByProjectId` by delegating to the derived query on `TaskJpaRepository` and mapping the results back to domain tasks.

---

## Connecting Application Services to Persistence

The original application services use repository interfaces and implement application-level operations.

For example, `ProjectApplicationService` creates and retrieves projects, while `TaskApplicationService` creates tasks, retrieves them, and transitions their statuses.

The persistence module provides Spring configuration to register these application services as beans and inject the JPA-backed repository adapters.

Conceptually:

```text
ProjectApplicationService
            │
            ▼
    ProjectRepository
            ▲
            │ implements
ProjectRepositoryAdapter
            │
            ▼
   ProjectJpaRepository
```

The same arrangement applies to tasks.

This design allows the application services to use the same repository contracts without depending directly on Spring Data JPA.

The `ApplicationServiceConfiguration` class provides the application service beans, while component scanning discovers the persistence adapters, mappers, and JPA repositories.

---

## Transaction Management

Database operations often involve multiple related actions that should succeed or fail together.

Spring's `@Transactional` annotation defines transaction boundaries around methods.

This example uses transactional service methods to demonstrate how application operations can interact with persistence within a transaction.

### Creating a Project

```java
@Transactional
public Project createProject(ProjectId id, String name, String description) {
    return projectService.createProject(id, name, description);
}
```

The method executes within a transaction managed by Spring.

### Reading a Project

```java
@Transactional(readOnly = true)
public Project getProject(ProjectId id) {
    return projectService.getProject(id);
}
```

The `readOnly = true` attribute indicates that the operation is intended for reading data. Depending on the transaction manager and persistence provider, it can enable optimizations, but it should not be treated as a universal guarantee that writes are impossible.

### Updating a Task's Status

```java
@Transactional
public Task transitionTask(TaskId id, TaskStatus newStatus) {
    return taskService.transitionTask(id, newStatus);
}
```

The application service retrieves the task, applies the domain transition rules, and saves the updated task through its repository.

The transaction provides a boundary for the persistence work involved in that operation.

### Transaction Flow

```text
Service Method
      │
      ▼
Begin Transaction
      │
      ▼
Application Service
      │
      ▼
Repository Adapter
      │
      ▼
Database Operation
      │
      ▼
Commit or Rollback
```

Spring's transaction infrastructure manages the transaction around the proxied service method.

Transactional behavior depends on calls passing through the Spring-managed proxy. Self-invocation within the same bean does not ordinarily trigger a new proxy-based transaction interception.

---

## Persisting Task Status Transitions

The existing domain model defines three task statuses:

- `TODO`
- `IN_PROGRESS`
- `COMPLETED`

The domain model controls which transitions are allowed. Persistence stores the resulting status.

The task entity uses:

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private TaskStatus status;
```

The `EnumType.STRING` setting stores the enum's name instead of its ordinal position.

For example, a task's status is stored as `TODO`, `IN_PROGRESS`, or `COMPLETED`.

This is generally safer than ordinal persistence because reordering enum constants does not silently change the meaning of existing numeric values.

The intended transition flow is:

```text
TODO
  │
  ▼
IN_PROGRESS
  │
  ▼
COMPLETED
```

The domain model remains responsible for validating transitions. The persistence layer records the valid state produced by the application service.

---

## Database Configuration

The application uses an in-memory H2 database configured in `application.properties`.

```properties
spring.application.name=spring-data-and-persistence

spring.datasource.url=jdbc:h2:mem:capstone;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.open-in-view=false
spring.jpa.show-sql=true
```

### H2 Database

The JDBC URL:

```properties
spring.datasource.url=jdbc:h2:mem:capstone;DB_CLOSE_DELAY=-1
```

configures an in-memory database named `capstone`.

This means the example does not require a separately installed database server.

The database is useful for local development and automated tests, but it is not intended to provide durable storage across normal application restarts.

### Schema Generation

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

instructs Hibernate to create the schema when the persistence context starts and drop it when the application context shuts down.

This simplifies a self-contained example by avoiding a separate database migration step.

Production applications typically need a deliberate schema migration strategy, for example using Flyway or Liquibase.

### Open EntityManager in View

```properties
spring.jpa.open-in-view=false
```

disables Open EntityManager in View.

This encourages database access and mapping to occur within explicit service transaction boundaries rather than relying on an extended persistence context during web response rendering.

### SQL Logging

```properties
spring.jpa.show-sql=true
```

enables SQL statement logging so that database operations can be observed while exploring the example.

This is useful for learning, although production logging should be configured with care.

---

## Testing Persistence

The example includes tests for both repository adapters and transactional services.

The two test approaches cover different responsibilities.

### Testing Repository Adapters with `@DataJpaTest`

`@DataJpaTest` is designed for JPA-focused tests.

It configures the relevant persistence infrastructure and typically uses an embedded database when one is available.

The project and task adapter tests import the adapter and mapper components they need, then exercise persistence operations against the test database.

For example:

```java
@DataJpaTest
@Import({ProjectRepositoryAdapter.class, ProjectPersistenceMapper.class})
class ProjectRepositoryAdapterTest {
    // Persistence tests
}
```

The project adapter tests verify that:

- A project can be saved and retrieved.
- The saved domain object matches the original project.
- Looking up a nonexistent project returns an empty `Optional`.

The task adapter tests verify that:

- A task can be saved and retrieved.
- Tasks can be found by project identifier.
- Looking up a nonexistent task returns an empty `Optional`.

These tests exercise the actual JPA repository and adapter mapping rather than mocking database operations.

### Testing Services with `@SpringBootTest`

`@SpringBootTest` loads the Spring Boot application context and tests the integrated application components.

For example:

```java
@SpringBootTest
class ProjectPersistenceServiceTest {
    // Service integration tests
}
```

The project service test creates a project through the persistence service and retrieves it to verify that the data is available through the application layer.

The task service test creates a project and a task, transitions the task through its allowed statuses, and verifies that the completed status can be retrieved.

This checks the collaboration between:

```text
Persistence Service
        │
        ▼
Application Service
        │
        ▼
Repository Adapter
        │
        ▼
Spring Data JPA
        │
        ▼
H2 Database
```

### Why Use Both Test Annotations?

| Test approach     | Main purpose                                                           |
|-------------------|------------------------------------------------------------------------|
| `@DataJpaTest`    | Focuses on JPA repositories, entity mappings, and persistence adapters |
| `@SpringBootTest` | Tests the integrated Spring application and service wiring             |

The focused tests are generally faster and help isolate persistence problems. The full-context tests provide confidence that the components work together.

Tests using `@DataJpaTest` commonly run within a test-managed transaction that is rolled back after each test. Full-context tests do not automatically receive the same rollback behavior merely because they use `@SpringBootTest`; transaction behavior depends on the test configuration and annotations.

---

## Running the Example

From the repository root, run:

```bash
mvn -pl 06-capstone-application/spring-data-and-persistence -am spring-boot:run
```

The `-pl` option selects the Spring Data and Persistence module, while `-am` also builds its required reactor modules.

The application starts using the configured in-memory H2 database.

Because this is a persistence-focused example without a REST API, it does not expose project or task operations through HTTP endpoints. Its main purpose is to demonstrate how the application services interact with a database through Spring Data JPA.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or run them from the repository root:

```bash
mvn -pl 06-capstone-application/spring-data-and-persistence -am test
```

The tests verify entity persistence, domain-to-entity mapping, repository adapter behavior, project-task relationships, and task status persistence through the service layer.

---

## Key Takeaways

- Spring Data JPA reduces boilerplate for common database operations.
- `JpaRepository` provides standard CRUD operations and supports derived queries.
- Domain models and JPA entities have different responsibilities.
- Persistence mappers translate between domain objects and database entities.
- Repository adapters implement application-defined repository interfaces.
- A many-to-one relationship models tasks belonging to projects.
- `FetchType.LAZY` allows related entities to be loaded when needed.
- `EnumType.STRING` stores task statuses by name.
- `@Transactional` defines transaction boundaries for service operations.
- `@DataJpaTest` is useful for focused persistence tests.
- `@SpringBootTest` verifies integration across the application context.
- H2 provides a convenient database for a small, runnable example.
- Keeping persistence details behind repository interfaces helps preserve separation of concerns.

---

## Next Example

The next capstone example will explore **REST APIs**, building HTTP endpoints on top of the existing application services and persistence layer.

It will demonstrate how clients can interact with projects and tasks through HTTP while keeping web concerns separate from domain logic and database access.
