# REST APIs

This example demonstrates **building REST APIs with Spring Boot** by exposing application functionality through HTTP endpoints.

It builds on the Spring Data and Persistence example, reusing its JPA-based persistence implementation and the application services defined in the Application Design and Architecture module.

The example demonstrates how to expose project and task operations through REST endpoints, validate incoming requests, represent application data using DTOs, handle errors consistently, and test REST APIs using Spring's MVC testing support.

## What This Example Demonstrates

- Designing RESTful HTTP endpoints with Spring MVC.
- Creating and retrieving projects through a REST API.
- Creating, retrieving, and listing tasks associated with projects.
- Updating task status through a dedicated endpoint.
- Using HTTP methods and status codes appropriately.
- Separating request and response DTOs from domain models.
- Validating incoming requests using Jakarta Bean Validation.
- Handling application exceptions with `@RestControllerAdvice`.
- Returning consistent error responses.
- Generating resource URLs using the `Location` header.
- Reusing application services and persistence adapters from existing modules.
- Configuring entity scanning and Spring Data JPA repositories across modules.
- Testing controller behavior using `MockMvc`.
- Testing end-to-end REST workflows against the persistence layer.

---

## What Is a REST API?

A REST API exposes application resources through HTTP endpoints.

Clients interact with these resources by sending HTTP requests and receiving HTTP responses. Each request identifies a resource, specifies an operation, and may contain data required to perform that operation.

For example, a project management application might expose projects and tasks as resources.

```text
Client
  │
  │ HTTP Request
  ▼
REST Controller
  │
  ▼
Application Service
  │
  ▼
Repository Interface
  │
  ▼
Persistence Adapter
  │
  ▼
JPA Repository
  │
  ▼
Database
```

The response travels back through the application layers and is serialized into an HTTP response.

A REST API allows different clients, such as web applications, mobile applications, command-line tools, or other services, to interact with the same backend.

---

## Example Domain

This example builds on the capstone application's project and task domain.

A project represents a unit of work, while a task represents an individual activity belonging to a project.

Conceptually:

```text
Project
  │
  ├── Task 1
  │     └── TODO
  │
  ├── Task 2
  │     └── IN_PROGRESS
  │
  └── Task 3
        └── COMPLETED
```

The domain model already defines the business concepts and task status transitions. The REST API exposes these capabilities over HTTP without placing business rules directly in the controllers.

### Project Resource

A project contains:

- An identifier.
- A name.
- A description.

### Task Resource

A task contains:

- An identifier.
- The identifier of its parent project.
- A title.
- A description.
- A status.

The available task statuses are:

```text
TODO
  │
  ▼
IN_PROGRESS
  │
  ▼
COMPLETED
```

The domain model controls which transitions are valid. The REST API translates requests into application-service calls and returns the resulting task representation.

---

## Application Architecture

The REST API is part of a larger, multi-module Spring application.

It reuses the Application Design and Architecture and Spring Data and Persistence modules instead of implementing domain logic or database access a second time.

```text
                 REST API Client
                        │
                        ▼
              ┌───────────────────┐
              │   REST API Module │
              │                   │
              │ Controllers       │
              │ Request DTOs      │
              │ Response DTOs     │
              │ Exception Handler │
              └─────────┬─────────┘
                        │
                        ▼
              ┌───────────────────┐
              │ Persistence       │
              │ Services          │
              └─────────┬─────────┘
                        │
                        ▼
              ┌───────────────────┐
              │ Application       │
              │ Services          │
              └─────────┬─────────┘
                        │
                        ▼
              ┌───────────────────┐
              │ Repository        │
              │ Interfaces         │
              └─────────┬─────────┘
                        │
                        ▼
              ┌───────────────────┐
              │ Persistence       │
              │ Adapters and JPA  │
              └─────────┬─────────┘
                        │
                        ▼
                     Database
```

The responsibilities are separated as follows:

| Component             | Responsibility                                                          |
|-----------------------|-------------------------------------------------------------------------|
| REST controllers      | Map HTTP requests to application operations and produce HTTP responses. |
| Request DTOs          | Define and validate the data clients can submit.                        |
| Response DTOs         | Define the representation returned to clients.                          |
| Exception handler     | Translate application and request errors into HTTP error responses.     |
| Persistence services  | Coordinate persistence operations and transaction boundaries.           |
| Application services  | Execute application use cases.                                          |
| Repository interfaces | Define persistence-independent data access contracts.                   |
| Persistence adapters  | Implement repository contracts using Spring Data JPA.                   |
| JPA repositories      | Perform database operations.                                            |

This separation keeps HTTP-specific concerns out of the domain and application layers.

---

## Maven Module Structure

The example is located at:

```text
06-capstone-application/
├── application-design-and-architecture/
├── spring-data-and-persistence/
└── rest-apis/
```

The `rest-apis` module depends on `spring-data-and-persistence`, which in turn depends on `application-design-and-architecture`.

```text
rest-apis
    │
    ▼
spring-data-and-persistence
    │
    ▼
application-design-and-architecture
```

This dependency structure allows the REST module to use the existing application and persistence functionality.

It also avoids duplicating domain models, application services, and persistence implementations.

---

## REST API Endpoints

The example exposes endpoints for managing projects and tasks.

### Project Endpoints

| HTTP Method | Endpoint                    | Description                | Success Status |
|-------------|-----------------------------|----------------------------|----------------|
| `POST`      | `/api/projects`             | Creates a project.         | `201 Created`  |
| `GET`       | `/api/projects/{projectId}` | Retrieves a project by ID. | `200 OK`       |

### Task Endpoints

| HTTP Method | Endpoint                          | Description                         | Success Status |
|-------------|-----------------------------------|-------------------------------------|----------------|
| `POST`      | `/api/projects/{projectId}/tasks` | Creates a task within a project.    | `201 Created`  |
| `GET`       | `/api/tasks/{taskId}`             | Retrieves a task by ID.             | `200 OK`       |
| `GET`       | `/api/projects/{projectId}/tasks` | Lists tasks belonging to a project. | `200 OK`       |
| `PATCH`     | `/api/tasks/{taskId}/status`      | Changes a task's status.            | `200 OK`       |

The API uses nested URLs when the operation concerns a task within a particular project. Individual tasks can also be retrieved and updated through their own resource URLs.

---

## Creating a Project

The client sends a `POST` request to `/api/projects` with a JSON request body.

```http
POST /api/projects
Content-Type: application/json
```

Example request:

```json
{
  "id": "project-1",
  "name": "Capstone Application",
  "description": "A Spring Boot project management application"
}
```

The controller accepts the request, delegates project creation to the persistence service, and returns the created project.

Example response:

```http
HTTP/1.1 201 Created
Location: /api/projects/project-1
Content-Type: application/json
```

```json
{
  "id": "project-1",
  "name": "Capstone Application",
  "description": "A Spring Boot project management application"
}
```

The `201 Created` status indicates that a resource was created successfully.

The `Location` header identifies the URL where the new resource can be retrieved.

### Why Use `201 Created`?

A successful resource-creation request should communicate more than simply that the request was accepted.

`201 Created` explicitly indicates that the server created a resource as a result of the request.

Returning the resource representation in the response also allows the client to immediately access its identifier and other properties.

---

## Request DTOs

Request DTOs define the structure of incoming JSON payloads.

For example, `CreateProjectRequest` represents the data needed to create a project.

```java
public record CreateProjectRequest(
    @NotBlank String id,
    @NotBlank @Size(max = 255) String name,
    @Size(max = 2000) String description
) {
}
```

The record defines three fields:

- `id` must not be blank.
- `name` must not be blank and cannot exceed 255 characters.
- `description` cannot exceed 2,000 characters.

The constraints are provided by Jakarta Bean Validation.

### Why Use DTOs?

Using dedicated request DTOs prevents the API from accepting domain objects directly.

This is useful because the external API contract and the internal domain model have different responsibilities.

```text
Incoming JSON
     │
     ▼
Request DTO
     │
     ▼
Validation
     │
     ▼
Controller
     │
     ▼
Application or Persistence Service
     │
     ▼
Domain Model
```

The client supplies only the fields supported by the request contract. The application constructs the appropriate domain objects from those values.

This approach also allows the API to evolve without requiring the domain model to mirror every HTTP payload.

---

## Request Validation

The controllers use `@Valid` to trigger validation of incoming request DTOs.

For example:

```java
@PostMapping
public ResponseEntity<ProjectResponse> createProject(
        @Valid @RequestBody CreateProjectRequest request) {
    // Delegate to the application-facing service.
}
```

The annotations have distinct responsibilities:

- `@RequestBody` binds the JSON request body to the Java object.
- `@Valid` triggers Bean Validation.
- `@NotBlank` rejects null, empty, or whitespace-only strings.
- `@Size` restricts the length of a value.

If the request fails validation, the controller does not proceed with the creation operation. Spring raises a validation exception, which the API exception handler converts into a `400 Bad Request` response.

For example, this request is invalid because the project name is blank:

```json
{
  "id": "project-1",
  "name": " ",
  "description": "An invalid project"
}
```

The example returns a structured error response rather than allowing the invalid request to reach the application service.

Validation at the API boundary provides useful feedback to clients. Domain invariants should still be enforced in the domain layer where appropriate, because application rules must remain valid regardless of how the application is accessed.

---

## Response DTOs

Response DTOs define the JSON representation returned to clients.

For example, `ProjectResponse` converts a domain `Project` into an API response:

```java
public record ProjectResponse(String id, String name, String description) {
    
    public static ProjectResponse from(Project project) {
        
        return new ProjectResponse(project.id().value(), project.name(), project.description());
    }
}
```

The conversion method explicitly maps the domain object to the API representation.

The same approach is used for tasks.

```java
public record TaskResponse(String id, String projectId, String title, String description, String status) {
    
    public static TaskResponse from(Task task) {
        
        return new TaskResponse(task.id().value(), task.projectId().value(), task.title(), task.description(), 
                task.status().name());
    }
}
```

The response exposes identifiers as strings and task status as its name.

### Why Not Return Domain Objects Directly?

Dedicated response DTOs:

- Define a stable external representation.
- Prevent internal domain structures from becoming part of the API contract accidentally.
- Allow response fields to evolve independently of domain implementation details.
- Make it explicit which information is exposed to API clients.

---

## Creating a Task

Tasks belong to projects, so creation uses a nested project URL.

```http
POST /api/projects/project-1/tasks
Content-Type: application/json
```

Example request:

```json
{
  "id": "task-1",
  "title": "Implement REST APIs",
  "description": "Expose project and task operations"
}
```

The project identifier is taken from the URL, while the request body contains the task-specific information.

The controller delegates creation to `TaskPersistenceService`, which uses the existing application and persistence layers.

Example response:

```http
HTTP/1.1 201 Created
Location: /api/tasks/task-1
Content-Type: application/json
```

```json
{
  "id": "task-1",
  "projectId": "project-1",
  "title": "Implement REST APIs",
  "description": "Expose project and task operations",
  "status": "TODO"
}
```

The initial task status is `TODO`, as defined by the domain model.

The controller does not independently implement task creation rules or manage database entities.

---

## Retrieving Projects and Tasks

A client retrieves an individual project using:

```http
GET /api/projects/project-1
```

A task can be retrieved using:

```http
GET /api/tasks/task-1
```

A successful retrieval returns `200 OK` and a JSON response representing the requested resource.

If the requested resource does not exist, the corresponding application exception is handled by the API exception handler and translated into `404 Not Found`.

This gives clients a consistent way to distinguish successful retrieval from a missing resource.

---

## Listing Tasks for a Project

The API lists tasks associated with a project using:

```http
GET /api/projects/project-1/tasks
```

Example response:

```json
[
  {
    "id": "task-1",
    "projectId": "project-1",
    "title": "Implement REST APIs",
    "description": "Expose project and task operations",
    "status": "TODO"
  },
  {
    "id": "task-2",
    "projectId": "project-1",
    "title": "Write tests",
    "description": "Test the REST endpoints",
    "status": "IN_PROGRESS"
  }
]
```

If the project exists but has no tasks, the endpoint returns an empty JSON array:

```json
[]
```

The controller first checks that the project exists. This prevents a request for a nonexistent project from being confused with a valid project that simply has no tasks.

---

## Updating Task Status

The API exposes a dedicated endpoint for changing task status:

```http
PATCH /api/tasks/task-1/status
Content-Type: application/json
```

Example request:

```json
{
  "status": "IN_PROGRESS"
}
```

Example response:

```json
{
  "id": "task-1",
  "projectId": "project-1",
  "title": "Implement REST APIs",
  "description": "Expose project and task operations",
  "status": "IN_PROGRESS"
}
```

The request uses the domain `TaskStatus` enum, whose values are:

```java
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED
}
```

The controller delegates the transition to the existing task service. The domain model determines whether the requested transition is valid.

For example:

```text
TODO → IN_PROGRESS       Valid
IN_PROGRESS → COMPLETED  Valid
TODO → COMPLETED         Invalid
```

An invalid transition is translated into `400 Bad Request` by the API exception handler.

A status-specific endpoint makes the intention of the operation clear and avoids exposing arbitrary internal task fields for modification.

---

## HTTP Status Codes

The API uses HTTP status codes to communicate the outcome of a request.

| Status            | Meaning                                | Example                                                    |
|-------------------|----------------------------------------|------------------------------------------------------------|
| `200 OK`          | The request completed successfully.    | Retrieving a project or updating task status.              |
| `201 Created`     | A resource was created.                | Creating a project or task.                                |
| `400 Bad Request` | The request is invalid.                | Failed validation, malformed JSON, or invalid task status. |
| `404 Not Found`   | The requested resource does not exist. | Retrieving an unknown task.                                |

A consistent status-code strategy allows clients to handle responses without relying exclusively on message text.

---

## Centralized Exception Handling

The example uses `@RestControllerAdvice` to centralize exception handling.

```java
@RestControllerAdvice
public class ApiExceptionHandler {
    // Exception handlers
}
```

Without centralized handling, controllers can become cluttered with repeated `try`/`catch` blocks and error-response construction.

Instead, the exception handler translates application and request exceptions into HTTP responses.

The example handles three main categories.

### Missing Resources

`ProjectNotFoundException` and `TaskNotFoundException` are mapped to `404 Not Found`.

### Invalid Task Transitions

`TaskTransitionException` is mapped to `400 Bad Request`.

### Invalid Request Bodies

Validation failures and malformed JSON are mapped to `400 Bad Request`.

The response uses an `ApiError` structure:

```json
{
  "message": "Request validation failed",
  "details": [
    "name: must not be blank"
  ]
}
```

The `message` provides a high-level description of the error. The `details` list can provide more specific information, such as which field failed validation.

Centralized exception handling makes error responses more consistent and reduces duplication across controllers.

---

## Spring Boot Application Configuration

The REST module has its own application entry point:

```java
@SpringBootApplication(scanBasePackages = {
    "com.springbyexample.v2.restapis",
    "com.springbyexample.v2.springdataandpersistence"
})
public class RestApisApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestApisApplication.class, args);
    }
}
```

The component scan includes both the REST API module and the Spring Data and Persistence module.

This allows Spring to discover REST controllers, exception handlers, persistence services, and other components required by the application.

### Persistence Configuration

The REST module reuses JPA entities and Spring Data repository interfaces from the persistence module. Its persistence configuration explicitly identifies the entity and repository packages.

```java
@Configuration
@EntityScan(basePackageClasses = {ProjectEntity.class, TaskEntity.class})
@EnableJpaRepositories(
    basePackages = "com.springbyexample.v2.springdataandpersistence.persistence.repository")
public class RestApisPersistenceConfiguration {
}
```

`@EntityScan` identifies the packages containing JPA entities.

`@EnableJpaRepositories` identifies the packages containing Spring Data JPA repository interfaces.

Keeping these annotations in a dedicated configuration class makes the persistence setup explicit and allows MVC slice tests to exclude that configuration when they don't need a database.

The production application still discovers this configuration through component scanning.

---

## Database Configuration

The example uses an in-memory H2 database for local execution and testing.

```properties
spring.application.name=rest-apis
spring.datasource.url=jdbc:h2:mem:capstone-rest;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.open-in-view=false
spring.jpa.show-sql=true
```

The important settings are:

- `jdbc:h2:mem:capstone-rest` creates an in-memory database.
- `DB_CLOSE_DELAY=-1` keeps the database alive while the JVM remains active, even if connections are temporarily closed.
- `spring.jpa.hibernate.ddl-auto=create-drop` creates the schema for the application lifecycle and drops it when the persistence context shuts down.
- `spring.jpa.open-in-view=false` disables Open EntityManager in View.
- `spring.jpa.show-sql=true` prints SQL statements to the console.

An in-memory database makes the example easy to run without installing or configuring an external database.

The schema-generation setting is suitable for this educational example; production applications should use an appropriate database and a deliberate schema migration strategy.

---

## Testing the REST API

The example uses Spring's testing support and `MockMvc` to verify HTTP behavior.

`MockMvc` allows tests to execute MVC requests without starting a real HTTP server.

This makes it possible to test routing, request binding, validation, exception handling, status codes, headers, and response bodies.

### Controller Tests

Controller tests use mocked persistence services to focus on the REST layer.

They cover behavior such as:

- Creating a project and checking the response.
- Verifying the `Location` header.
- Retrieving a project.
- Rejecting blank fields and overlong names.
- Rejecting malformed JSON.
- Creating and retrieving tasks.
- Listing tasks for a project.
- Returning an empty array when a project has no tasks.
- Updating task status.
- Rejecting invalid status values.

The mocked services isolate controller behavior from database implementation details.

### Integration Tests

The integration test loads the application and exercises the REST endpoints using the real application and persistence components.

A typical workflow is:

```text
Create Project
      │
      ▼
Retrieve Project
      │
      ▼
Create Task
      │
      ▼
List Project Tasks
      │
      ▼
Update Task Status
      │
      ▼
Retrieve Updated Task
```

The integration tests also verify error scenarios, including missing resources and invalid domain transitions.

Using both controller and integration tests provides complementary coverage:

| Test Type         | Main Focus                                                                         |
|-------------------|------------------------------------------------------------------------------------|
| Controller tests  | HTTP contract, request validation, headers, response bodies, and error mapping.    |
| Integration tests | Interaction between controllers, services, persistence adapters, and the database. |

---

## Why Use `@WebMvcTest`?

`@WebMvcTest` loads a focused MVC test slice rather than the entire application.

It is appropriate when a test needs the web layer but does not need a database.

For example:

```java
@WebMvcTest(ProjectController.class)
@Import(ApiExceptionHandler.class)
class ProjectControllerTest {
    // MockMvc and mocked service dependencies
}
```

The persistence service is supplied as a mock, allowing the test to verify controller behavior without creating a JPA `EntityManagerFactory`.

Because the production application has explicit JPA repository configuration, that persistence configuration should be excluded from MVC slice tests. Otherwise, the test can attempt to create repository infrastructure even though the MVC slice does not provide the necessary JPA beans.

This distinction is important when designing tests for applications that combine web and persistence components.

---

## Why Separate Request and Response Models?

The REST API acts as a boundary between external clients and the internal application.

Request DTOs describe what clients may submit. Response DTOs describe what clients receive. Domain models express application concepts and enforce business rules.

```text
External Client
      │
      ▼
Request DTO
      │
      ▼
REST Controller
      │
      ▼
Application Services
      │
      ▼
Domain Model
      │
      ▼
Persistence
```

The response follows the reverse direction, with the domain result mapped to a response DTO.

This separation makes the API easier to maintain and allows internal implementation details to change without automatically changing the external contract.

---

## Running the Example

From the repository root:

```bash
mvn -pl 06-capstone-application/rest-apis -am spring-boot:run
```

The `-am` option builds the required modules in the reactor before running the requested module.

The application starts with the REST API endpoints available at:

```text
http://localhost:8080/api/projects
```

For example, create a project with `curl`:

```bash
curl -i -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{
    "id": "project-1",
    "name": "Capstone Application",
    "description": "A Spring Boot project management application"
  }'
```

The response should include `201 Created`, a `Location` header, and the created project representation.

Retrieve the project with:

```bash
curl -i http://localhost:8080/api/projects/project-1
```

Create a task with:

```bash
curl -i -X POST http://localhost:8080/api/projects/project-1/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "id": "task-1",
    "title": "Implement REST APIs",
    "description": "Expose project and task operations"
  }'
```

List the project's tasks with:

```bash
curl -i http://localhost:8080/api/projects/project-1/tasks
```

---

## Running the Tests

Run the REST API module tests and build its required modules from the repository root:

```bash
mvn -pl 06-capstone-application/rest-apis -am test
```

To run tests from the module directory:

```bash
mvn test
```

The tests verify the REST API contract, request validation, exception handling, and interactions with the application and persistence layers.

---

## Key Takeaways

- Spring MVC controllers expose application functionality through HTTP endpoints.
- REST APIs use HTTP methods and status codes to communicate resource operations and outcomes.
- Request DTOs define and validate incoming payloads.
- Response DTOs provide a stable representation of application data.
- `@Valid` integrates Jakarta Bean Validation into request processing.
- `@RestControllerAdvice` centralizes HTTP exception handling.
- `201 Created` and the `Location` header communicate successful resource creation.
- Application services and domain models should own business behavior rather than REST controllers.
- Existing persistence functionality can be reused across Maven modules.
- Entity scanning and repository discovery must account for package boundaries.
- `@WebMvcTest` tests the web layer without requiring a database.
- Integration tests verify that the web, application, and persistence layers work together.

---

## Next Example

The next example will extend the capstone application with **Spring Security**, demonstrating how to secure REST APIs using authentication and authorization while integrating security into the existing application architecture.
