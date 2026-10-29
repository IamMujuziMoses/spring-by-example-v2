
# Transaction Management

This example demonstrates **transaction management** in Spring using Spring Data JPA.

It focuses on how Spring manages database transactions, how `@Transactional` defines transaction boundaries, how failures trigger rollbacks, and how transaction propagation allows an audit record to be committed independently of a failed business operation.

The example uses a project-management scenario in which a project and its tasks must be created atomically. If creating any task fails, the project and all previously created tasks must be rolled back.

## What This Example Demonstrates

- What database transactions are and why they matter.
- Spring's declarative transaction management.
- The `@Transactional` annotation.
- Transaction boundaries in the service layer.
- Atomic operations involving multiple database records.
- Commit and rollback behavior.
- Default transaction propagation with `Propagation.REQUIRED`.
- Independent transactions with `Propagation.REQUIRES_NEW`.
- Separating business operations from transaction auditing.
- Spring Data JPA repositories and Hibernate.
- Testing transaction behavior with Spring Boot, JUnit, Mockito, and H2.
- Unit testing service behavior separately from transaction integration testing.

---

## What Is Transaction Management?

Transaction management controls how a group of database operations is executed as a single logical unit of work.

Consider a project-management application that creates a project and its tasks.

Creating a project might involve several database operations:

1. Insert the project.
2. Insert the first task.
3. Insert the second task.
4. Insert additional tasks, if any.

These operations are logically related. A project should not be left partially created if one of its tasks cannot be saved.

Without a transaction, an application might produce an inconsistent state:

````text
Create Project
      │
      ▼
Save Project
      │
      ▼
Save First Task
      │
      ▼
Save Second Task
      │
      ▼
Failure
      │
      ▼
Project and First Task Remain
````

With transaction management, the application can treat all the operations as one unit:

````text
Begin Transaction
       │
       ▼
   Save Project
       │
       ▼
   Save Tasks
       │
       ▼
  All Successful?
       │
   ┌───┴───┐
   │       │
  Yes      No
   │       │
   ▼       ▼
 Commit  Rollback
   │       │
   ▼       ▼
All Data  No Partial
Persists  Business Data
````

The goal is to ensure that either all the intended changes succeed or none of the business changes are committed.

---

## ACID Properties

Database transactions are commonly described using four ACID properties.

### Atomicity

Atomicity means that a transaction is treated as one indivisible operation.

For this example, creating a project and its tasks should either complete successfully or roll back together.

### Consistency

Consistency means that a transaction moves the database from one valid state to another, preserving the constraints enforced by the application and database.

For example, every persisted task must reference a valid project.

### Isolation

Isolation controls how concurrent transactions interact with one another.

The exact guarantees depend on the database and transaction isolation level.

### Durability

Durability means that committed changes persist according to the guarantees of the database and its configuration.

Together, these properties help applications maintain reliable data.

---

## The Application Scenario

This example models three entities:

- `Project`: represents a project.
- `Task`: represents a task belonging to a project.
- `TransactionAudit`: records project-creation failures.

The project and its tasks form the main business operation.

The audit record serves a different purpose: it records a failure even when the main business transaction is rolled back.

The conceptual relationship is:

````text
Project
   │
   │ 1
   │
   │ *
   ▼
 Task

TransactionAudit
   │
   └── Records transaction-related events
````

The audit record does not require a foreign key to the project. This allows it to remain in the database even when the corresponding project creation is rolled back.

---

## Project Entity

The `Project` entity represents the project being created.

````java
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    protected Project() {
    }

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
````

The entity uses JPA annotations to define its database mapping.

- `@Entity` marks the class as a persistent entity.
- `@Table` specifies the database table.
- `@Id` identifies the primary key.
- `@GeneratedValue` delegates identifier generation to the database.
- `@Column(nullable = false)` requires a project name at the database-schema level.

The entity does not manage transactions itself. Transaction coordination belongs in the service layer.

---

## Task Entity

The `Task` entity represents work associated with a project.

````java
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    protected Task() {
    }

    public Task(String title, Project project) {
        this.title = title;
        this.project = project;
    }
}
````

Each task references its project through a `ManyToOne` relationship.

The foreign key ensures that a task is associated with a project. The service explicitly saves each task, allowing the example to demonstrate transaction boundaries across multiple repository operations.

---

## TransactionAudit Entity

The `TransactionAudit` entity stores information about failed project-creation operations.

It includes:

- The event type.
- The project name.
- Details about the failure.
- The time the event occurred.

A typical audit record might contain:

````text
Event Type: PROJECT_CREATION_FAILED
Project Name: Example Project
Details: Task title must not be blank
Occurred At: 2026-10-29T08:00:00Z
````

The actual timestamp is generated when the audit record is created.

The audit entity is deliberately independent of the project entity. If the project transaction rolls back, the audit record must not depend on a project row that no longer exists.

---

## Spring Data JPA Repositories

The example uses three repositories:

````java
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
````

````java
public interface TaskRepository extends JpaRepository<Task, Long> {
}
````

````java
public interface TransactionAuditRepository extends JpaRepository<TransactionAudit, Long> {
}
````

Spring Data JPA provides implementations of these repository interfaces automatically.

They expose operations such as:

- `save()`
- `findById()`
- `findAll()`
- `count()`
- `deleteAll()`

The repositories handle persistence, while the service layer coordinates the business operation and its transaction boundaries.

---

## Declarative Transaction Management

Spring supports declarative transaction management through annotations and transaction infrastructure.

Instead of manually beginning and committing database transactions, application code declares where a transaction should apply.

For example:

````java
@Transactional
public Project createProjectWithTasks(String name, String description, List<String> taskTitles) {

    Project project = projectRepository.save(new Project(name, description));

    for (String title : taskTitles) {
        taskRepository.save(new Task(title, project));
    }

    return project;
}
````

The `@Transactional` annotation tells Spring to execute the method within a transaction when the method is invoked through the appropriate Spring-managed proxy.

If the method completes successfully, Spring commits the transaction.

If a qualifying exception escapes the method, Spring rolls back the transaction according to its rollback rules.

By default, Spring rolls back for unchecked exceptions, including `RuntimeException` and its subclasses, and for `Error`. Checked exceptions do not trigger rollback by default.

---

## The `@Transactional` Annotation

The `@Transactional` annotation is the central mechanism used by this example.

It can be used on classes or methods to define transaction behavior.

For example:

````java
@Transactional
public void updateProject() {
    // Database operations
}
````

Spring uses its transaction infrastructure to apply the declared behavior.

The annotation can configure several aspects of a transaction, including:

- Propagation.
- Isolation.
- Read-only behavior.
- Transaction timeout.
- Rollback rules.

This example concentrates on transaction boundaries, rollback behavior, and propagation.

---

## Why Transactions Belong in the Service Layer

A business operation can involve multiple repositories.

For example:

````text
ProjectTransactionService
          │
          ├── ProjectRepository
          │
          └── TaskRepository
````

If each repository operation were treated as an independent business transaction, the application could commit the project before discovering that a task cannot be created.

By placing `@Transactional` on the service method, the application groups the related persistence operations into one transaction.

The service layer therefore coordinates the business operation, while the repositories handle individual persistence operations.

---

## Creating a Project and Its Tasks

The main business method is `createProjectWithTasks()`.

Conceptually, its responsibilities are:

1. Validate the project name.
2. Save the project.
3. Validate each task title.
4. Save each task.
5. Return the created project.

The method is annotated with `@Transactional`.

````java
@Transactional
public Project createProjectWithTasks(String name, String description, List<String> taskTitles) {

    try {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name must not be blank");
        }

        Project project = projectRepository.save(new Project(name, description));

        for (String title : taskTitles) {
            if (title == null || title.isBlank()) {
                throw new IllegalArgumentException("Task title must not be blank");
            }

            taskRepository.save(new Task(title, project));
        }

        return project;
        
    } catch (RuntimeException exception) {
        
        auditService.recordProjectCreationFailure(name, exception.getMessage());

        throw exception;
    }
}
````

The implementation validates each task as it is processed. This allows the example to demonstrate rollback after the project and an earlier task have already been submitted for persistence.

The exception is rethrown after recording the failure so that the main transaction can roll back.

---

## Transaction Commit

A commit makes the transaction's changes permanent according to the database's transaction guarantees.

Suppose the application creates a project with two valid tasks.

The transaction flow is:

````text
Begin Transaction
       │
       ▼
Save Project
       │
       ▼
Save First Task
       │
       ▼
Save Second Task
       │
       ▼
Method Completes Successfully
       │
       ▼
Commit Transaction
       │
       ▼
Project and Both Tasks Persist
````

The project and tasks are committed together as part of the same transaction.

If the operation succeeds, no failure audit record is required.

---

## Transaction Rollback

Rollback discards the uncommitted database changes belonging to the transaction.

Suppose the application creates a project and processes two tasks:

- The first task has a valid title.
- The second task has a blank title.

The application has already submitted the project and first task for persistence when the validation exception occurs.

The transaction flow becomes:

````text
Begin Transaction
       │
       ▼
Save Project
       │
       ▼
Save First Task
       │
       ▼
Validate Second Task
       │
       ▼
IllegalArgumentException
       │
       ▼
Record Failure in New Transaction
       │
       ▼
Rethrow Exception
       │
       ▼
Rollback Main Transaction
       │
       ▼
Project and Tasks Are Not Committed
````

The database should contain neither the project nor its tasks after rollback.

The audit record is treated separately, as explained below.

---

## Transaction Propagation

Transaction propagation determines how a transactional method behaves when another transaction already exists.

Spring provides several propagation modes, including:

- `REQUIRED`
- `REQUIRES_NEW`
- `SUPPORTS`
- `NOT_SUPPORTED`
- `MANDATORY`
- `NEVER`
- `NESTED`

This example focuses on `REQUIRED` and `REQUIRES_NEW`.

---

## `Propagation.REQUIRED`

`REQUIRED` is the default propagation behavior for `@Transactional`.

````java
@Transactional
public Project createProjectWithTasks(...) {
    // Create project and tasks.
}
````

When no transaction exists, Spring starts one.

When a transaction already exists, the method normally participates in the existing transaction.

Conceptually:

````text
Caller
  │
  ▼
Transactional Service
  │
  ▼
Existing Transaction?
  │
  ├── No ──► Start Transaction
  │
  └── Yes ─► Join Transaction
````

The project and task operations in this example participate in the same main transaction.

This allows Spring to roll back their combined changes when a qualifying exception escapes the transactional method.

---

## `Propagation.REQUIRES_NEW`

The audit service uses `REQUIRES_NEW`.

````java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void recordProjectCreationFailure(String projectName, String details) {

    TransactionAudit audit = new TransactionAudit("PROJECT_CREATION_FAILED", projectName, details, Instant.now());

    auditRepository.save(audit);
}
````

`REQUIRES_NEW` always executes with a new transaction. If a transaction already exists, Spring suspends it for the duration of the new transaction.

This is useful when an audit record must survive a rollback of the main business operation.

The flow is:

````text
Main Transaction
       │
       ▼
Project Creation Fails
       │
       ▼
Suspend Main Transaction
       │
       ▼
Start New Audit Transaction
       │
       ▼
Save Audit Record
       │
       ▼
Commit Audit Transaction
       │
       ▼
Resume Main Transaction
       │
       ▼
Rethrow Original Exception
       │
       ▼
Rollback Main Transaction
````

The audit record can survive because it was committed independently.

This behavior assumes the audit transaction itself succeeds. If writing the audit record fails, the audit exception may affect which exception reaches the caller. The current implementation does not suppress audit failures.

---

## Why the Audit Service Is a Separate Bean

The audit operation is implemented in `TransactionAuditService`, separate from `ProjectTransactionService`.

This is important because Spring's declarative transaction management normally relies on proxies.

When one Spring-managed bean invokes a transactional method on another bean, the call can pass through the proxy and apply the method's transaction configuration.

By contrast, calling an annotated method directly from another method on the same object is known as self-invocation. That call can bypass the transactional proxy and prevent the expected propagation behavior from being applied.

Separating the services makes the intended `REQUIRES_NEW` boundary explicit.

---

## Transaction Management and Exception Handling

The service catches `RuntimeException` to record the failure and then rethrows the same exception.

````java
catch (RuntimeException exception) {
    auditService.recordProjectCreationFailure(name, exception.getMessage());

    throw exception;
}
````

Rethrowing the exception is important.

If the service swallowed the exception and returned normally, Spring could commit the main transaction unless it had otherwise been marked rollback-only.

Because the exception escapes the transactional method, Spring applies its default rollback rules for unchecked exceptions.

The catch block also means that invalid input is currently audited as a project-creation failure. This is an intentional behavior of the example's implementation.

---

## Testing Transaction Management

The example separates unit tests from integration tests.

### Unit Tests

The unit tests use JUnit and Mockito to test the service's business behavior without starting a Spring application context or using a real database.

They demonstrate:

- Rejecting a blank project name.
- Rejecting a blank task title.
- Auditing and rethrowing a persistence exception.

Mockito verifies which repository and audit-service methods are called.

These tests are useful for isolating business logic, but they do not prove that a real database transaction commits or rolls back.

### Integration Tests

The integration tests use `@SpringBootTest`, Spring Data JPA, and an H2 database.

They demonstrate:

- Successfully committing a project and its tasks.
- Creating a project with no tasks.
- Rolling back project and task changes after a failure.
- Persisting a failure audit record.
- Committing an audit record independently of an outer transaction rollback.

These tests exercise Spring's actual transaction infrastructure.

---

## H2 Database Configuration

The example uses an in-memory H2 database.

````properties
spring.application.name=transaction-management

spring.datasource.url=jdbc:h2:mem:transaction-management;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.open-in-view=false
````

H2 avoids the need to install or configure an external database server.

The `create-drop` setting allows Hibernate to create the schema for the example and drop it when the application context closes. This is convenient for a demonstration but is not a production database migration strategy.

---

## Running the Example

From the repository root:

````bash
mvn -pl 06-capstone-application/transaction-management spring-boot:run
````

This starts the Spring Boot application.

The example focuses on the service and persistence layers; it does not expose an HTTP API or user interface.

---

## Running the Tests

Run the module's tests from the repository root:

````bash
mvn -pl 06-capstone-application/transaction-management test
````

To build required modules in the Maven reactor as well:

````bash
mvn -pl 06-capstone-application/transaction-management -am test
````

The test suite includes both unit tests and integration tests.

---

## Important Testing Considerations

The integration tests intentionally do not place the test methods inside a test-managed `@Transactional` transaction.

This allows the service's transaction to complete before the test queries the database, making committed state and rollback results observable.

The unit tests use strict Mockito stubbing. Each test should define only the stubs it actually needs. Shared stubbing that is unused in a particular test can trigger `UnnecessaryStubbingException`.

Finally, a test that mocks a repository and makes it throw an exception is not, by itself, proof of database rollback. Rollback must be verified through a real transactional integration test.

---

## Key Takeaways

- A transaction groups database operations into one logical unit of work.
- Spring provides declarative transaction management through `@Transactional`.
- Transaction boundaries commonly belong in the service layer.
- `REQUIRED` joins an existing transaction or creates one when necessary.
- Unchecked exceptions trigger rollback by default when they escape a transactional method.
- A caught and swallowed exception does not automatically cause rollback.
- `REQUIRES_NEW` suspends an existing transaction and executes an independent transaction.
- Separating the audit service into its own Spring bean allows the propagation annotation to be applied through the proxy.
- A committed audit record can survive a rollback of the main business transaction.
- Unit tests and integration tests serve different purposes.
- Real transaction behavior should be verified with integration tests using a database.

---

## Next Example

The next example will demonstrate **Modular Architecture with Spring Modulith**, showing how to organize the capstone application into well-defined application modules, establish clear module boundaries, manage module dependencies, and verify the application's modular structure.
