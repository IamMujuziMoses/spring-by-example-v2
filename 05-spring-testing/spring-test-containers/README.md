# Testcontainers

This example demonstrates **Testcontainers integration testing** with Spring Boot and PostgreSQL.

It focuses on how Testcontainers manages a real PostgreSQL database in a Docker container, how Spring Boot automatically configures the datasource using `@ServiceConnection`, and how integration tests can verify application behavior against a real database.

## What This Example Demonstrates

- What Testcontainers is and why it is useful for integration testing.
- Running PostgreSQL in a Docker container.
- Managing container lifecycle with `@Testcontainers` and `@Container`.
- Integrating Testcontainers with Spring Boot using `@ServiceConnection`.
- Configuring a datasource using container connection details.
- Testing REST endpoints with `MockMvc`.
- Testing persistence using Spring Data JPA and PostgreSQL.
- Testing successful message creation and retrieval.
- Testing behavior when a requested message does not exist.
- Isolating test data between test methods.
- Understanding why Docker must be running when executing Testcontainers tests.

---

## What Is Testcontainers?

Testcontainers is a testing library that provides disposable instances of infrastructure dependencies through containers.

Instead of requiring developers to install and configure a database specifically for testing, Testcontainers starts a container when the tests run and manages its lifecycle.

This example uses PostgreSQL as the database.

A simplified flow looks like:

```text
Run Integration Tests
         │
         ▼
Testcontainers
         │
         ▼
Start PostgreSQL Container
         │
         ▼
Spring Boot Datasource
         │
         ▼
Spring Data JPA
         │
         ▼
Application Components
         │
         ▼
Integration Tests
```

The tests exercise the application using a real PostgreSQL database rather than an in-memory database.

---

## Why Use Testcontainers?

Integration tests often depend on infrastructure such as databases, message brokers, or other external services.

Using an in-memory database can simplify testing, but its behavior may differ from the database used in production.

Testcontainers allows tests to run against a real database engine without requiring a separately maintained test database installation.

The main benefits include:

- **Realistic database behavior:** Tests run against PostgreSQL rather than a substitute.
- **Reproducibility:** The database image and configuration are declared in the test code.
- **Isolation:** Tests can use a disposable database environment.
- **Reduced manual setup:** Developers do not need to configure a persistent PostgreSQL server for this example.
- **Production-like testing:** Database-specific behavior can be exercised during automated tests.

Testcontainers does not eliminate the need to understand the test environment. Docker or a compatible container runtime must be available when the tests execute.

---

## PostgreSQL Container

The integration test declares a PostgreSQL container:

```java
@Container
@ServiceConnection
static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("messages")
                .withUsername("test")
                .withPassword("test");
```

The container uses the `postgres:16-alpine` image.

The configuration specifies:

- **Database name:** `messages`
- **Username:** `test`
- **Password:** `test`
- **Image:** PostgreSQL 16 based on Alpine Linux

These credentials are intended for the isolated test database, not for production use.

The container field does not need to be referenced directly by the test methods. Testcontainers and Spring Boot discover it through the annotations.

---

## `@Testcontainers`

The test class uses:

```java
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class MessageIntegrationTest {
    // Integration tests
}
```

`@Testcontainers` integrates the Testcontainers JUnit Jupiter extension with the test class.

It allows the extension to discover container fields annotated with `@Container` and manage their lifecycle.

The container is started as part of the test lifecycle and stopped when the lifecycle finishes.

This means the example does not require a developer to start PostgreSQL manually.

However, the Docker engine itself must already be running.

---

## `@Container`

The PostgreSQL container is declared with:

```java
@Container
static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");
```

`@Container` identifies the container that Testcontainers should manage.

The field is `static`, so the container is shared across test methods in the test class rather than being started separately for every test method.

This reduces the overhead of repeatedly starting PostgreSQL while allowing the tests to prepare their own data.

---

## Why Is the Container Field Not Referenced?

The container field may appear unused because the test methods do not access `postgres` directly.

This is expected.

The field is discovered through annotations and accessed by the testing frameworks.

```text
Container Field
      │
      ▼
@Testcontainers Extension
      │
      ▼
@Container Discovery
      │
      ▼
PostgreSQL Container Lifecycle
```

The field is still important even though application code does not call its methods directly.

Using the explicit `PostgreSQLContainer<?>` type also makes the declaration clearer and avoids a raw generic type.

---

## `@ServiceConnection`

The container is also annotated with:

```java
@ServiceConnection
```

Spring Boot's `@ServiceConnection` support detects supported container types and derives connection details for the corresponding application service.

For this example, Spring Boot uses the PostgreSQL container's connection details to configure the datasource.

Conceptually:

```text
PostgreSQL Container
         │
         ▼
@ServiceConnection
         │
         ▼
Database Connection Details
         │
         ▼
Spring Boot Datasource
         │
         ▼
Spring Data JPA
```

Without this integration, the test would need to configure the datasource connection details manually.

`@ServiceConnection` removes that boilerplate for supported services.

---

## Application Architecture

The example implements a simple message API backed by PostgreSQL.

The main components are:

```text
MessageIntegrationTest
         │
         ▼
      MockMvc
         │
         ▼
 MessageController
         │
         ▼
   MessageService
         │
         ▼
 MessageRepository
         │
         ▼
   PostgreSQL
```

### Message Entity

The `Message` entity represents a message stored in the database.

It contains the information required to identify and retrieve a message.

### Message Repository

The `MessageRepository` uses Spring Data JPA to provide database access.

It supports the persistence and retrieval operations used by the application.

### Message Service

The `MessageService` contains the message-related application logic and delegates persistence operations to the repository.

### Message Controller

The `MessageController` exposes REST endpoints for creating and retrieving messages.

### Integration Testing Application

The `IntegrationTestingApplication` class bootstraps the Spring Boot application context used by the integration tests.

---

## Application Properties

The example uses the following configuration:

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

This configures Hibernate to create the database schema for the application context and drop it when the context is closed.

Because PostgreSQL runs in a disposable container, the database is intended for testing rather than persistent application data.

The container's connection details are supplied through `@ServiceConnection`, so a fixed local PostgreSQL URL is not required.

---

## Integration Testing with `MockMvc`

The test class uses:

```java
@SpringBootTest
@AutoConfigureMockMvc
```

`@SpringBootTest` loads the Spring Boot application context.

`@AutoConfigureMockMvc` configures `MockMvc`, allowing tests to exercise the application's MVC request-handling pipeline without starting a separate HTTP server.

The requests flow through the controller, service, repository, and PostgreSQL database.

```text
MockMvc Request
      │
      ▼
Spring MVC
      │
      ▼
Controller
      │
      ▼
Service
      │
      ▼
Repository
      │
      ▼
PostgreSQL Container
```

This is an integration test because it verifies interactions between multiple application components and a real database.

---

## Testing Message Creation

The tests send a request to:

```text
POST /messages
```

The request creates a message through the application's REST API.

The test verifies the response and confirms that the message has been persisted.

The expected flow is:

```text
POST /messages
       │
       ▼
MessageController
       │
       ▼
MessageService
       │
       ▼
MessageRepository
       │
       ▼
PostgreSQL
       │
       ▼
HTTP Response
```

This verifies more than the controller response alone. It also exercises the persistence layer against PostgreSQL.

---

## Testing Message Retrieval

The tests retrieve an existing message using:

```text
GET /messages/{id}
```

The application looks up the message through the repository and returns it in the HTTP response.

The test verifies that the returned message matches the expected content.

This demonstrates that data written to PostgreSQL can be retrieved through the application.

---

## Testing a Missing Message

The tests also request a message that does not exist.

The application responds with the appropriate not-found result.

This scenario verifies how the application handles missing data when the request passes through the controller, service, and persistence layers.

Integration tests should cover both successful operations and relevant failure scenarios.

---

## Isolating Test Data

The test class clears existing messages before each test:

```java
@BeforeEach
void setUp() {
    messageRepository.deleteAll();
}
```

This helps prevent data created by one test from affecting another test.

The PostgreSQL container is shared across test methods, so the database itself is not necessarily recreated for every test.

Instead, the setup method prepares the repository state before each test runs.

This is a simple way to keep test behavior predictable while avoiding the cost of starting a new database container for every method.

---

## Testcontainers vs. H2

This project includes a separate Integration Testing example that uses H2.

Both approaches are useful, but they serve slightly different purposes.

| Aspect                       | H2                            | Testcontainers with PostgreSQL  |
|------------------------------|-------------------------------|---------------------------------|
| Database                     | In-memory H2                  | Real PostgreSQL                 |
| Docker required              | No                            | Yes                             |
| Startup overhead             | Usually lower                 | Includes container startup      |
| PostgreSQL-specific behavior | Not fully reproduced          | Tested against PostgreSQL       |
| Manual database installation | Not required                  | Not required                    |
| Typical use                  | Lightweight integration tests | Real database integration tests |

H2 is convenient for fast, lightweight testing.

Testcontainers is useful when the behavior of the actual database matters, including database-specific SQL, types, constraints, and other PostgreSQL features.

Neither approach replaces every kind of testing. The appropriate choice depends on the behavior being verified.

---

## Docker Requirement

Docker must be installed and running before the Testcontainers tests execute.

Testcontainers manages containers, but it does not start the Docker engine.

Verify that Docker is available:

```bash
docker --version
docker info
```

You can also check that Docker can run a container:

```bash
docker run --rm hello-world
```

If Docker is unavailable, the tests may fail with an error similar to:

```text
java.lang.IllegalStateException:
Could not find a valid Docker environment.
```

This error indicates that Testcontainers cannot find a usable Docker environment. It does not, by itself, indicate a problem with the test's PostgreSQL container declaration.

### Does Docker Need to Run All the Time?

No. Docker only needs to be running when executing this example's Testcontainers tests.

You can stop Docker after finishing your work to free system resources.

Other examples in the Spring Testing module that do not use containers do not require Docker merely because they belong to the same module.

In a CI/CD environment, the test runner must also have access to a compatible container runtime.

---

## Running the Example

From the repository root, run:

```bash
mvn -pl 05-spring-testing/testcontainers spring-boot:run
```

This starts the application, but the application also needs a configured PostgreSQL datasource. The Testcontainers configuration in this example is intended for the test lifecycle, so running the application independently requires an appropriate database configuration.

For the normal integration testing workflow, run the tests instead.

---

## Running the Tests

This example uses `Testcontainers` to start a `PostgreSQL` container for integration testing.

Docker must be installed and running for the integration tests to execute.

### Running Without Docker

The `Testcontainers` tests are skipped by default so that the repository's Maven build can run without requiring Docker.

From the repository root:

```bash
mvn clean verify
```

### Running With Docker

Start Docker, then execute:

```bash
mvn clean verify -DskipTestcontainersTests=false
```

or running only the `Testcontainers` module tests

```bash
mvn -pl 05-spring-testing/testcontainers -am clean verify -DskipTestcontainersTests=false
```

The `skipTestcontainersTests` property controls whether this module's tests are executed. Setting it to `false` enables the tests.

During execution, Testcontainers starts PostgreSQL, Spring Boot configures the datasource using `@ServiceConnection`, and the integration tests run against the containerized database.

The container lifecycle is managed by Testcontainers and the JUnit integration.

> **Note:** Skipping these tests does not verify the PostgreSQL integration. Run them with Docker before submitting changes that affect this example.

The expected lifecycle is:

```text
Run Tests
    │
    ▼
Start PostgreSQL Container
    │
    ▼
Initialize Spring Boot Context
    │
    ▼
Execute Integration Tests
    │
    ▼
Stop PostgreSQL Container
    │
    ▼
Test Execution Complete
```

---

## Key Takeaways

- Testcontainers allows integration tests to use real infrastructure dependencies.
- PostgreSQL can run in a disposable Docker container for testing.
- `@Testcontainers` integrates container management with JUnit Jupiter.
- `@Container` identifies the container managed by the extension.
- A static container field is shared across test methods in the test class.
- `@ServiceConnection` lets Spring Boot derive datasource connection details from the container.
- `@SpringBootTest` loads the application context for integration testing.
- `@AutoConfigureMockMvc` supports MVC testing without starting a separate HTTP server.
- Clearing repository data before each test helps isolate test scenarios.
- Docker must be running when Testcontainers tests execute, but it does not need to run continuously when you are not using them.
- H2 remains useful for lightweight tests, while Testcontainers provides stronger confidence in behavior against PostgreSQL.

---

## What This Example Does Not Cover

This example intentionally focuses on basic PostgreSQL integration testing with Testcontainers.

It does not cover:

- Running multiple containers in a single test.
- Custom Docker networks.
- Database migration testing with Flyway or Liquibase.
- Advanced container reuse strategies.
- Container startup optimization for large test suites.
- Testing against multiple PostgreSQL versions.
- CI/CD-specific container configuration.
- Running the application independently against an externally managed PostgreSQL database.

These topics can be explored separately when needed.

---

## Next Example

The next example will explore **Application Context Testing**, including how to verify Spring application context initialization, bean registration, and configuration behavior.
