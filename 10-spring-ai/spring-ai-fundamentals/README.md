# Spring AI Fundamentals

This example demonstrates **Spring AI fundamentals** by integrating a Spring Boot application with an AI chat model using Spring AI and Ollama.

It focuses on how Spring AI simplifies interaction with AI models, how to use `ChatClient` to send prompts and retrieve generated responses, and how to expose AI-powered functionality through a REST API.

The example uses **Ollama with Llama 3.2** to run an AI model locally, allowing developers to experiment with Spring AI without requiring a cloud-based AI provider or API key.

## What This Example Demonstrates

- What Spring AI is and the problems it addresses.
- Integrating Spring AI into a Spring Boot application.
- Configuring an AI model using Ollama.
- Running a local AI model with Llama 3.2.
- Understanding the `ChatClient` API.
- Sending user messages to an AI model.
- Retrieving AI-generated responses.
- Building an AI-powered REST API.
- Validating incoming chat requests.
- Handling AI service failures.
- Testing Spring AI integrations without requiring a running AI model.

---

## What Is Spring AI?

Spring AI is a framework that simplifies the development of AI-powered applications using the Spring ecosystem.

It provides abstractions that allow developers to interact with different AI model providers without having to implement provider-specific integration logic throughout their applications.

Instead of directly managing HTTP requests, authentication, request formats, and response parsing for individual AI providers, developers can use Spring AI's consistent APIs.

Spring AI provides support for capabilities such as:

- Chat completion.
- Prompt engineering.
- Structured output.
- Embeddings.
- Vector databases.
- Retrieval-Augmented Generation (RAG).
- Tool calling.
- Model evaluation and testing.

The goal is to make AI integration feel natural within a Spring application.

---

## Spring AI Architecture

A simplified view of the example is:

```text
Client
  │
  │ HTTP POST
  ▼
ChatController
  │
  ▼
AiChatService
  │
  ▼
Spring AI ChatClient
  │
  ▼
Ollama Chat Model
  │
  ▼
Llama 3.2
  │
  ▼
Generated Response
  │
  ▼
ChatResponse
  │
  ▼
Client
```

Each component has a specific responsibility.

| Component        | Responsibility                                                    |
|------------------|-------------------------------------------------------------------|
| `ChatController` | Exposes the REST endpoint for chat requests.                      |
| `AiChatService`  | Contains the application logic for interacting with the AI model. |
| `ChatClient`     | Provides a fluent API for communicating with chat models.         |
| Ollama           | Runs and exposes the local AI model.                              |
| Llama 3.2        | Processes prompts and generates responses.                        |
| `ChatResponse`   | Represents the response returned to the client.                   |

---

## What Is a Chat Model?

A chat model is an AI model designed to process conversational input and generate responses.

Unlike traditional application services that execute predefined business logic, a chat model generates responses based on the supplied prompt and its learned capabilities.

For example, an application can send:

```text
Explain the purpose of Spring AI.
```

The model processes the prompt and generates a response.

The response might explain how Spring AI provides abstractions for integrating AI models into Spring applications.

The exact wording and content of generated responses can vary between requests.

---

## Ollama

This example uses [Ollama](https://ollama.com/) as its AI model provider.

Ollama allows developers to download and run AI models locally.

It exposes an API that applications can use to communicate with locally running models.

This means the Spring Boot application does not need to communicate with an external AI provider.

### Why Ollama?

Ollama is useful for learning and experimentation because:

- AI models can run locally.
- No cloud AI API key is required.
- Developers can experiment without paying per API request.
- Model execution can take place without sending prompts to an external AI provider.
- Different models can be downloaded and tested.

The performance of local models depends on the available hardware and the selected model.

---

## Llama 3.2

The example uses **Llama 3.2** as its chat model.

Llama 3.2 is a family of language models that can process prompts and generate natural-language responses.

Ollama provides a convenient way to download and run the model locally.

The model is configured through the application's YAML configuration.

---

## Installing Ollama

Download and install Ollama from:

https://ollama.com/

After installation, download the Llama 3.2 model:

```bash
ollama pull llama3.2
```

Verify that the model is available:

```bash
ollama list
```

You should see `llama3.2` in the list of downloaded models.

To run the model interactively:

```bash
ollama run llama3.2
```

You can now send prompts directly to the model from the terminal.

For example:

```text
Explain what Spring Boot is.
```

Ollama normally exposes its API at:

```text
http://localhost:11434
```

Spring AI uses this endpoint to communicate with the local model.

---

## Spring AI Dependencies

The example uses the Spring AI Ollama starter.

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-ollama</artifactId>
</dependency>
```

The Spring AI BOM manages the versions of Spring AI dependencies centrally.

The root Maven configuration imports the BOM:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-bom</artifactId>
    <version>${spring-ai.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

The project uses Spring AI 2.0.3 to remain aligned with its Spring Boot 4.1.x baseline.

Other dependencies used by the example include:

- Spring Boot Web MVC for REST endpoints.
- Spring Boot Validation for request validation.
- Spring Boot Test for application and controller testing.

---

## Configuring Spring AI

The application configures Spring AI through `application.yml`.

```yaml
spring:
  application:
    name: spring-ai-fundamentals

  ai:
    model:
      chat: ollama
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.2
        temperature: 0.7

server:
  port: 8080
```

### Application Name

```yaml
spring:
  application:
    name: spring-ai-fundamentals
```

Defines the Spring Boot application's name.

### Chat Model Provider

```yaml
spring:
  ai:
    model:
      chat: ollama
```

Specifies Ollama as the chat model provider.

### Ollama Base URL

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
```

Defines the address where the Ollama API is available.

### Chat Model

```yaml
spring:
  ai:
    ollama:
      chat:
        model: llama3.2
```

Specifies the model that Spring AI should use for chat requests.

### Temperature

```yaml
temperature: 0.7
```

Controls the randomness of generated responses.

A lower temperature generally produces more predictable responses, while a higher temperature allows more variation.

Temperature is a model-generation setting, not a guarantee of response quality or correctness.

---

## What Is ChatClient?

`ChatClient` is one of the main abstractions provided by Spring AI for interacting with chat models.

It provides a fluent API for constructing prompts, supplying user messages, invoking a model, and retrieving generated content.

Instead of directly interacting with the Ollama HTTP API, the application uses `ChatClient`.

This keeps the application code independent of the provider-specific communication details.

---

## Creating a ChatClient

The example creates a `ChatClient` using the auto-configured `ChatClient.Builder`.

```java
@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
}
```

Spring AI automatically configures the builder based on the available chat model configuration.

The application injects the builder through constructor injection and creates a `ChatClient` instance.

This demonstrates how Spring AI integrates with Spring's dependency injection mechanism.

---

## Sending a Message to the AI Model

The service exposes a `chat` method:

```java
public String chat(String message) {
    return chatClient.prompt().user(message).call().content();
}
```

The method uses the fluent `ChatClient` API.

### `prompt()`

```java
chatClient.prompt()
```

Begins constructing a prompt.

A prompt represents the input that will be sent to the chat model.

### `user(message)`

```java
.user(message)
```

Adds the user's message to the prompt.

The message contains the instruction or question that the AI model should process.

### `call()`

```java
.call()
```

Invokes the configured chat model.

Spring AI handles the communication with the underlying provider.

### `content()`

```java
.content();
```

Extracts the generated text from the model response.

The service returns this content to the calling component.

The complete interaction is:

```text
User Message
     │
     ▼
ChatClient.prompt()
     │
     ▼
.user(message)
     │
     ▼
.call()
     │
     ▼
AI Chat Model
     │
     ▼
Generated Response
     │
     ▼
.content()
```

---

## Exposing AI Functionality Through a REST API

The example exposes an HTTP endpoint that allows external clients to interact with the AI model.

The endpoint is:

```text
POST /api/ai/chat
```

The controller delegates the actual AI interaction to `AiChatService`.

This separation keeps HTTP-specific responsibilities out of the service layer.

### Chat Request

The application accepts a `ChatRequest`:

```java
public record ChatRequest(@NotBlank(message = "Message must not be blank") String message) {
}
```

The request contains one field:

| Field     | Description                          |
|-----------|--------------------------------------|
| `message` | The message to send to the AI model. |

The `@NotBlank` annotation ensures that the message is not null, empty, or composed entirely of whitespace.

### Chat Response

The application returns a `ChatResponse`:

```java
public record ChatResponse(String response) {
}
```

The response contains the text generated by the AI model.

### Chat Controller

The controller exposes the endpoint:

```java
@RestController
@RequestMapping("/api/ai/chat")
public class ChatController {

    private final AiChatService aiChatService;

    public ChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String response = aiChatService.chat(request.message());

        return ResponseEntity.ok(new ChatResponse(response));
    }
}
```

The controller performs the following operations:

1. Receives an HTTP POST request.
2. Deserializes the JSON request into `ChatRequest`.
3. Validates the incoming message.
4. Delegates the message to `AiChatService`.
5. Receives the generated response.
6. Wraps the response in `ChatResponse`.
7. Returns HTTP 200 OK.

The controller does not communicate directly with Ollama.

---

## Request Validation

The example uses Jakarta Bean Validation to validate incoming chat requests.

```java
@NotBlank(message = "Message must not be blank") String message
```

The controller uses `@Valid`:

```java
public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request)
```

This tells Spring MVC to validate the request before invoking the controller method.

Requests containing invalid messages are rejected with HTTP 400 Bad Request.

For example, the following requests are invalid:

### Blank Message

```json
{
  "message": ""
}
```

### Whitespace Message

```json
{
  "message": "   "
}
```

### Null Message

```json
{
  "message": null
}
```

### Missing Message

```json
{}
```

This ensures that the AI service receives meaningful input rather than invalid requests.

---

## Handling AI Service Failures

Communicating with an AI model introduces external dependencies.

For example:

- Ollama may not be running.
- The configured model may not be available.
- The AI provider may fail to process a request.
- Communication with the model may fail.

The example demonstrates basic exception handling using `@RestControllerAdvice`.

### AiChatExceptionHandler

```java
@RestControllerAdvice(assignableTypes = ChatController.class)
public class AiChatExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleAiServiceFailure() {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, "The AI service is temporarily unavailable.");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problemDetail);
    }
}
```

The exception handler catches `IllegalStateException` exceptions raised while processing requests handled by `ChatController`.

It converts the exception into an HTTP 503 Service Unavailable response.

### Why HTTP 503?

HTTP 503 indicates that the server is currently unable to handle the request.

It is appropriate for this example because the application depends on an AI service that may temporarily be unavailable.

### Example Error Response

```json
{
  "type": "about:blank",
  "title": "Service Unavailable",
  "status": 503,
  "detail": "The AI service is temporarily unavailable."
}
```

The application returns a controlled error response instead of exposing the underlying exception to the client.

This is a basic demonstration of error handling. A production application should consider using a dedicated AI service exception rather than catching the broad `IllegalStateException` type.

---

## Testing Spring AI

The example uses JUnit, Mockito, Spring Boot Test, and MockMvc.

The tests are designed to verify application behavior without requiring a running Ollama instance.

This is important because unit and controller tests should remain repeatable and independent of external AI infrastructure.

### Application Context Test

`SpringAiFundamentalsApplicationTest` verifies that the Spring application context loads successfully.

```java
@SpringBootTest
class SpringAiFundamentalsApplicationTest {

    @Test
    void contextLoads() {
    }
}
```

This verifies that the application can initialize its Spring configuration and required beans.

The test does not send a prompt to the AI model.

### Service Tests

`AiChatServiceTest` verifies the interaction between `AiChatService` and `ChatClient`.

Mockito is used to mock the `ChatClient` fluent API.

This allows the service to be tested without invoking Ollama.

The service tests cover:

| Test                      | Expected Behavior                                     |
|---------------------------|-------------------------------------------------------|
| Successful model response | Returns the generated response.                       |
| Null model response       | Returns null when the model response content is null. |
| Model invocation failure  | Propagates the exception raised by the chat client.   |

The tests focus on the service's responsibility: invoking the chat client and returning its response.

### Controller Tests

`ChatControllerTest` uses standalone MockMvc to test the REST endpoint.

The `AiChatService` is mocked so that the tests do not communicate with an actual AI model.

The controller tests cover:

| Scenario                | Expected HTTP Status    |
|-------------------------|-------------------------|
| Successful chat request | 200 OK                  |
| Blank message           | 400 Bad Request         |
| Missing message         | 400 Bad Request         |
| Null message            | 400 Bad Request         |
| Malformed JSON          | 400 Bad Request         |
| AI service failure      | 503 Service Unavailable |

These tests demonstrate both successful request processing and error handling.

### Testing AI Service Failures

The controller test simulates an AI service failure:

```java
when(aiChatService.chat("Explain Spring AI")).thenThrow(new IllegalStateException("AI model unavailable"));
```

The request is then submitted through MockMvc.

The test verifies that the exception handler returns HTTP 503:

```java
.andExpect(status().isServiceUnavailable())
.andExpect(jsonPath("$.detail").value("The AI service is temporarily unavailable."));
```

This demonstrates how application-level exception handling can translate an AI integration failure into a meaningful HTTP response.

---

## Why We Mock ChatClient Instead of Calling Ollama in Every Test

AI model responses are not always deterministic.

The same prompt can produce different responses depending on model behavior and generation settings.

Calling a real AI model during every test can also introduce:

- Longer test execution times.
- External infrastructure dependencies.
- Hardware requirements.
- Model availability issues.
- Unpredictable response content.

Mocking `ChatClient` allows the tests to focus on application behavior rather than the AI model's generated wording.

A separate integration test profile can be introduced later to verify communication with a running Ollama instance.

---

## Making a Chat Request

Once the application is running, send a request to the chat endpoint.

### Using cURL

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Explain the purpose of Spring AI"
  }'
```

### Example Response

```json
{
  "response": "Spring AI simplifies the integration of artificial intelligence models into Spring applications by providing consistent abstractions and APIs."
}
```

The actual response will be generated by Llama 3.2 and may differ from this example.

---

## Running the Example

### Prerequisites

Before starting the application, ensure that:

- Java 21 is installed.
- Maven is installed.
- Ollama is installed and running.
- The Llama 3.2 model has been downloaded.

Download the model if you have not already done so:

```bash
ollama pull llama3.2
```

Start Ollama if it is not already running.

From the repository root, start the application:

```bash
mvn -pl 10-spring-ai/spring-ai-fundamentals spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

---

## Running the Tests

The tests do not require Ollama to be running because the AI client and service dependencies are mocked where appropriate.

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 10-spring-ai/spring-ai-fundamentals clean verify
```

---

## Key Takeaways

- Spring AI simplifies AI integration within Spring applications.
- Spring AI provides abstractions for interacting with different AI model providers.
- Ollama allows developers to run AI models locally.
- Llama 3.2 can be used as a local chat model.
- `ChatClient` provides a fluent API for constructing prompts and invoking chat models.
- `ChatClient.Builder` can be injected through Spring dependency injection.
- `.prompt()` begins prompt construction.
- `.user()` supplies the user's message.
- `.call()` invokes the chat model.
- `.content()` retrieves the generated text.
- REST controllers can expose AI-powered functionality to external clients.
- Jakarta Bean Validation helps reject invalid chat requests.
- `@RestControllerAdvice` provides centralized exception handling.
- HTTP 503 can communicate temporary AI service unavailability.
- Mockito allows service and controller tests to run without a live AI model.
- AI-generated responses should not be assumed to be deterministic.

---

## Next Example

The next example will explore **Chat Models** in Spring AI, focusing on chat model abstractions, model configuration, provider integration, and generating responses using different chat model implementations.
