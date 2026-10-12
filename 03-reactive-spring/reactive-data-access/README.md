# Reactive Data Access

This example demonstrates **reactive database access** using Spring Data R2DBC, Project Reactor, and Spring WebFlux.

It focuses on how to interact with a relational database without blocking, how Spring Data R2DBC provides reactive repository operations, and how `Mono` and `Flux` can be used to compose database operations and expose them through reactive REST endpoints.

## What This Example Demonstrates

- What reactive data access means.
- The differences between JDBC and R2DBC.
- Spring Data R2DBC configuration.
- Reactive repositories using `ReactiveCrudRepository`.
- Reactive CRUD operations.
- Derived query methods.
- `Mono` and `Flux` in database access.
- Reactive service and controller layers.
- Database initialization with `schema.sql`.
- Testing reactive repositories with `StepVerifier`.
- Testing reactive controllers with Spring WebFlux test support.
- Managing test data isolation.

---

## What Is Reactive Data Access?

Reactive data access allows an application to interact with a database using a non-blocking programming model.

In traditional JDBC-based applications, a database operation typically occupies the executing thread while waiting for the database response.

With R2DBC, database operations can be composed as reactive publishers. The application can continue processing other work while waiting for asynchronous database operations to complete.

A simplified flow looks like:

```text
HTTP Request
     │
     ▼
Reactive Controller
     │
     ▼
Reactive Service
     │
     ▼
Reactive Repository
     │
     ▼
R2DBC Driver
     │
     ▼
Relational Database
```

The results flow back through the reactive pipeline to the HTTP response.

---

## JDBC vs R2DBC

JDBC and R2DBC provide different programming models for relational database access.

### JDBC

A traditional JDBC operation typically follows this pattern:

```text
Application Thread
       │
       ▼
Execute SQL
       │
       ▼
Wait for Database
       │
       ▼
Receive Result
       │
       ▼
Continue Processing
```

The executing thread is generally blocked while waiting for the database operation to finish.

### R2DBC

Reactive database access follows a non-blocking model:

```text
Application
     │
     ▼
Subscribe to Database Operation
     │
     ▼
Execute Asynchronously
     │
     ▼
Receive Database Result
     │
     ▼
Continue Reactive Pipeline
```

The application composes operations through reactive publishers rather than waiting synchronously for each result.

R2DBC does not automatically make every part of an application non-blocking. The database driver, surrounding operations, and application code must support the reactive execution model.

---

## Spring Data R2DBC

Spring Data R2DBC provides an abstraction for working with relational databases reactively.

It supports repository interfaces, entity mapping, derived query methods, and reactive CRUD operations.

In this example, Spring Data R2DBC manages persistence for a `Note` entity.

The repository extends:

```java
ReactiveCrudRepository<Note, Long>
```

This provides common operations such as:

- `save(...)`
- `findById(...)`
- `findAll()`
- `deleteById(...)`
- `existsById(...)`

These operations return reactive types rather than requiring the application to retrieve results through blocking calls.

---

## Domain Model

The example uses a `Note` record to represent a database record.

Conceptually:

```java
@Table("notes")
public record Note(@Id Long id, String title, String content
) {}
```

The annotations provide mapping information:

- `@Table("notes")` identifies the database table.
- `@Id` identifies the primary key.
- `id` stores the generated database identifier.
- `title` stores the note title.
- `content` stores the note content.

Spring Data R2DBC uses this mapping information when reading and writing records.

---

## Reactive Repository

The `NoteRepository` interface extends `ReactiveCrudRepository`.

```java
public interface NoteRepository extends ReactiveCrudRepository<Note, Long> {

    Flux<Note> findByTitleContainingIgnoreCase(String title);
}
```

The repository provides standard CRUD operations and a custom derived query method.

### Derived Query Methods

Spring Data derives the query from the repository method name:

```java
Flux<Note> findByTitleContainingIgnoreCase(String title);
```

This method searches for notes whose titles contain the supplied text, ignoring case.

For example, searching for `spring` can match titles such as:

- `Reactive Spring`
- `Spring Data R2DBC`
- `Learning SPRING`

The method returns a `Flux<Note>` because multiple notes may match the search criteria.

---

## Mono and Flux in Data Access

Project Reactor provides two core publisher types used throughout this example.

### `Mono<T>`

A `Mono` represents an asynchronous computation that emits zero or one item, or an error.

For example:

```java
Mono<Note> findById(Long id);
```

This is appropriate when retrieving a single note by its identifier.

A note may exist, or the publisher may complete without emitting a note.

### `Flux<T>`

A `Flux` represents an asynchronous sequence that can emit zero or many items, or an error.

For example:

```java
Flux<Note> findAll();
```

This is appropriate when retrieving multiple notes.

### Comparing Mono and Flux

| Type         | Expected Items | Example                              |
|--------------|----------------|--------------------------------------|
| `Mono<Note>` | Zero or one    | Find a note by ID                    |
| `Flux<Note>` | Zero or many   | Retrieve all notes                   |
| `Flux<Note>` | Zero or many   | Search notes by title                |
| `Mono<Void>` | No value       | Represent completion of an operation |

The important distinction is that `Mono` and `Flux` describe the reactive result, not whether a database record is guaranteed to exist.

---

## Reactive Service Layer

The service layer composes repository operations using `Mono` and `Flux`.

It provides operations to:

- Retrieve all notes.
- Retrieve a note by ID.
- Search notes by title.
- Create a note.
- Update an existing note.
- Delete a note.

### Retrieving Notes

A service method can delegate directly to the repository:

```java
public Flux<Note> findAll() {
    return noteRepository.findAll();
}
```

The method returns the publisher without blocking for the database results.

### Creating a Note

A creation operation can use the repository's `save` method:

```java
public Mono<Note> create(Note note) {
    return noteRepository.save(note);
}
```

The database operation is represented by the returned `Mono`.

### Updating a Note

An update operation can retrieve the existing note and then save the updated version:

```text
Find Existing Note
        │
        ▼
Note Found?
        │
        ▼
Apply Changes
        │
        ▼
Save Updated Note
```

Reactor operators such as `flatMap` allow the next asynchronous operation to be composed into the same pipeline.

### Deleting a Note

Deletion can check whether the requested identifier exists before performing the delete operation.

```text
Check Identifier
       │
       ▼
Does Note Exist?
    ┌──┴──┐
   Yes    No
    │      │
    ▼      ▼
 Delete   Return
 Note     Not Found
```

The exact HTTP response for a missing note is determined by the service and controller implementation.

---

## Reactive REST Controller

The `NoteController` exposes database operations through Spring WebFlux.

The endpoints are grouped under:

```text
/api/notes
```

The controller delegates to the service layer and returns reactive publishers.

### Available Endpoints

| Method | Endpoint                         | Description           |
|--------|----------------------------------|-----------------------|
| GET    | `/api/notes`                     | Retrieve all notes    |
| GET    | `/api/notes/{id}`                | Retrieve a note by ID |
| GET    | `/api/notes/search?title=spring` | Search notes by title |
| POST   | `/api/notes`                     | Create a note         |
| PUT    | `/api/notes/{id}`                | Update a note         |
| DELETE | `/api/notes/{id}`                | Delete a note         |

The controller uses `Flux` for collections and `Mono` for single results and asynchronous response handling.

Returning a reactive publisher allows Spring WebFlux to subscribe to and process the result as part of request handling.

---

## Database Configuration

This example uses an H2 in-memory database with an R2DBC driver.

The database schema is initialized through `schema.sql`, while `application.properties` contains the application and database initialization settings.

A simplified schema looks like:

```sql
CREATE TABLE IF NOT EXISTS "notes" (
    "ID" BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    "TITLE" VARCHAR(255) NOT NULL,
    "CONTENT" VARCHAR(2000) NOT NULL
);
```

The exact identifier casing must remain consistent with the entity mapping and the SQL generated by Spring Data R2DBC.

### Why Use an In-Memory Database?

H2 keeps the example self-contained.

It allows the application and its tests to demonstrate reactive database operations without requiring an external database server.

Because the database is in memory, its data is not intended to persist after the database instance is shut down.

---

## Example Requests

Start the application and use the following requests to explore the API.

### Create a Note

```bash
curl -X POST http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Reactive Spring",
    "content": "Learning reactive database access with R2DBC"
  }'
```

### Retrieve All Notes

```bash
curl http://localhost:8080/api/notes
```

### Retrieve a Note by ID

```bash
curl http://localhost:8080/api/notes/1
```

### Search Notes by Title

```bash
curl "http://localhost:8080/api/notes/search?title=spring"
```

### Update a Note

```bash
curl -X PUT http://localhost:8080/api/notes/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Reactive Spring",
    "content": "Exploring Spring Data R2DBC"
  }'
```

### Delete a Note

```bash
curl -X DELETE http://localhost:8080/api/notes/1
```

---

## Testing Reactive Data Access

The example uses repository tests and controller tests to demonstrate how reactive applications can be tested.

### Repository Tests

`NoteRepositoryTest` verifies reactive database operations, including saving and retrieving notes and searching by title.

The tests use `StepVerifier` to subscribe to reactive publishers and verify their emitted values and terminal signals.

For example:

```java
StepVerifier.create(noteRepository.findById(id))
        .assertNext(note ->assertThat(note.title()).isEqualTo("Reactive Spring")).verifyComplete();
```

This verifies that the publisher emits a note with the expected title and then completes.

### Understanding `verifyComplete()`

A `Flux` can emit multiple values before completing.

The following test:

```java
StepVerifier.create(notes)
        .assertNext(note ->assertThat(note.title()).isEqualTo("Reactive Spring")).verifyComplete();
```

expects exactly one item followed by completion.

If another matching note is emitted, the test fails because the publisher produced an unexpected additional value.

This is why repository tests should control their test data and verify the expected number of results.

### Test Data Isolation

Tests may share a database instance when they use the same application context.

If one test inserts a note and does not remove it, another test may encounter that record when executing a query.

A reliable repository test should establish a predictable database state before inserting its fixtures. When appropriate, it can clear existing records before executing the test.

Tests that share a database should also avoid running destructive cleanup concurrently against the same data.

### Controller Tests

`NoteControllerTest` tests the HTTP endpoints using Spring WebFlux's test support and a mocked `NoteService`.

This allows the controller's request handling and response behavior to be tested independently of actual database operations.

Repository tests and controller tests therefore cover different parts of the application.

---

## Running the Example

From the repository root, start the application with:

```bash
mvn -pl 03-reactive-spring/reactive-data-access spring-boot:run
```

The API will be available at:

```text
http://localhost:8080/api/notes
```

---

## Running the Tests

Run the tests for this module:

```bash
mvn -pl 03-reactive-spring/reactive-data-access test
```

Run the full verification lifecycle:

```bash
mvn -pl 03-reactive-spring/reactive-data-access clean verify
```

To run only the repository test class:

```bash
mvn -pl 03-reactive-spring/reactive-data-access -Dtest=NoteRepositoryTest test
```

---

## Reactive Data Access Flow

A simplified view of the complete example is:

```text
HTTP Request
     │
     ▼
NoteController
     │
     ▼
NoteService
     │
     ▼
NoteRepository
     │
     ▼
R2DBC Driver
     │
     ▼
Relational Database
     │
     ▼
Mono / Flux Result
     │
     ▼
Reactive HTTP Response
```

Each layer has a distinct responsibility:

- The controller handles HTTP requests and responses.
- The service composes application operations.
- The repository defines reactive persistence operations.
- The R2DBC driver communicates with the database.
- Reactor represents and composes asynchronous results.

---

## Reactive Data Access vs Blocking Data Access

The key difference is how database operations are represented and executed.

### Blocking Data Access

```text
Call Database
     │
     ▼
Wait for Result
     │
     ▼
Continue Execution
```

### Reactive Data Access

```text
Compose Database Operation
     │
     ▼
Subscribe to Publisher
     │
     ▼
Process Result When Available
     │
     ▼
Continue Reactive Pipeline
```

Reactive data access can improve resource utilization in applications with many concurrent operations, especially when the entire execution path supports non-blocking processing.

It does not guarantee faster execution for every query or workload.

---

## Important Concepts

### Non-Blocking Does Not Mean No Waiting

The application still depends on the database to complete its operations.

The difference is that the application does not need to block a thread while waiting for an asynchronous result.

### Reactive Types Are Not Database Transactions

Using `Mono` and `Flux` does not automatically define transaction boundaries.

Transactional behavior requires appropriate transaction configuration and management for the reactive execution context.

### Avoid Blocking Calls

Calls such as `block()` should generally be avoided in the reactive request-processing path.

Blocking a reactive pipeline can reduce concurrency and undermine the benefits of the non-blocking model.

### R2DBC Is Different from JDBC

R2DBC provides a reactive database connectivity API. It is not simply JDBC with a different return type, and JDBC drivers cannot be used as R2DBC drivers.

---

## Key Takeaways

- Reactive data access provides a non-blocking programming model for database operations.
- Spring Data R2DBC provides repository abstractions for reactive persistence.
- `ReactiveCrudRepository` exposes common CRUD operations through reactive publishers.
- `Mono` represents zero or one item, while `Flux` represents zero to many items.
- Derived query methods allow repositories to express common searches through method names.
- Spring WebFlux can expose reactive database operations through REST endpoints.
- R2DBC database configuration and schema initialization must be consistent with entity mappings.
- `StepVerifier` verifies emitted values and completion or error signals.
- Repository tests should control their test data to avoid order-dependent failures.
- Reactive data access works best when blocking operations are avoided throughout the execution path.

---

## What This Example Does Not Cover

This example intentionally focuses on basic reactive relational database access.

It does not cover:

- Reactive transaction management.
- Database migrations with Flyway or Liquibase.
- Database connection pool tuning.
- Production database configuration.
- Advanced query customization.
- Database-specific performance optimization.
- Reactive database access with PostgreSQL or MySQL.
- Integration with external services in a single reactive pipeline.

These topics can be explored in separate examples as the project progresses.

---

## Further Reading

- [Spring Data R2DBC Reference](https://docs.spring.io/spring-data/relational/reference/r2dbc.html)
- [Spring WebFlux Reference](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor Reference Guide](https://projectreactor.io/docs/core/release/reference/)
- [R2DBC Project](https://r2dbc.io/)

---

## Next Example

**Error handling**

The next example will continue exploring **Reactive Spring**, building on reactive database access to demonstrate how reactive components can be composed into more complete application workflows.
