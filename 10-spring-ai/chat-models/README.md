# Chat Models

This example demonstrates **chat models** in Spring AI.

It focuses on how Spring AI's `ChatModel` abstraction provides a common interface for interacting with AI chat models, how prompts are constructed and submitted to a model, how generated responses are retrieved, and how model-specific options can be configured for individual requests.

The example uses **Ollama with Llama 3.2** as the chat model provider.

## What This Example Demonstrates

- What chat models are in Spring AI.
- The `ChatModel` abstraction.
- The relationship between `ChatModel` and AI model providers.
- Constructing prompts using `Prompt`.
- Supplying user input using `UserMessage`.
- Submitting prompts using `ChatModel.call()`.
- Understanding `ChatResponse`, `Generation`, and `AssistantMessage`.
- Configuring chat model options.
- Using `OllamaChatOptions`.
- Configuring model temperature.
- Overriding temperature for individual requests.
- Exposing chat model functionality through a REST API.
- Validating chat requests.
- Handling incomplete model responses.
- Testing chat model interactions using Mockito and MockMvc.

---

## What Is a Chat Model?

A chat model is an AI model designed to process conversational input and generate responses.

Unlike traditional applications that rely entirely on predefined logic, chat models can interpret natural language and generate responses based on the input they receive.

For example, an application can submit:

```text
Explain what Spring Boot is.
```

The chat model processes the message and generates a response:

```text
Spring Boot is a framework built on top of Spring that simplifies
the development of production-ready Spring applications.
```

Spring AI provides abstractions that allow applications to interact with different chat model providers through a consistent programming model.

This means application code can work with common Spring AI interfaces instead of depending directly on a provider's native API.

---

## Spring AI ChatModel Abstraction

The `ChatModel` interface is one of the central abstractions in Spring AI.

It provides a common way to interact with chat models.

Conceptually:

```text
Application
    │
    ▼
ChatModel
    │
    ▼
Chat Model Provider
    │
    ▼
AI Model
    │
    ▼
Generated Response
```

Spring AI provides implementations for different model providers.

Examples include:

- Ollama
- OpenAI
- Anthropic
- Google Vertex AI
- Azure OpenAI

Each provider has its own configuration and capabilities, but Spring AI provides common abstractions for interacting with them.

This reduces the need for application code to depend directly on provider-specific APIs.

---

## ChatModel vs ChatClient

Spring AI provides both `ChatModel` and `ChatClient` for interacting with chat models.

They serve related but different purposes.

### ChatModel

`ChatModel` is a lower-level abstraction for interacting directly with a chat model.

It provides methods for submitting prompts and receiving model responses.

For example:

```java
var response = chatModel.call(new Prompt(new UserMessage(message)));
```

This gives the application direct access to the prompt and response interaction.

### ChatClient

`ChatClient` provides a higher-level, fluent API built around chat model interactions.

For example:

```java
var response = chatClient.prompt().user(message).call().content();
```

It simplifies common interactions and provides additional capabilities such as advisors, structured output, and fluent prompt construction.

### Comparison

| Feature             | ChatModel                                        | ChatClient                                  |
|---------------------|--------------------------------------------------|---------------------------------------------|
| Abstraction level   | Lower-level                                      | Higher-level                                |
| API style           | Direct method calls                              | Fluent API                                  |
| Prompt construction | Explicit                                         | Fluent                                      |
| Response handling   | Explicit                                         | Simplified                                  |
| Model interaction   | Direct                                           | Built on chat model abstractions            |
| Typical use         | Understanding and controlling model interactions | Building application-level AI conversations |

The Spring AI Fundamentals example demonstrates `ChatClient`.

This example focuses on `ChatModel` to understand the underlying model interaction more directly.

---

## Prompt

A `Prompt` represents the input submitted to a chat model.

It contains the instructions and optional model-specific configuration used during model interaction.

A prompt can contain one or more messages.

For example:

```java
var prompt = new Prompt(new UserMessage("Explain Spring AI"));
```

The prompt contains a user message that will be submitted to the chat model.

Conceptually:

```text
Prompt
  │
  ▼
Instructions
  │
  ▼
UserMessage
  │
  ▼
"Explain Spring AI"
```

Spring AI uses the prompt to represent the input sent to the model.

---

## UserMessage

`UserMessage` represents a message supplied by the user.

For example:

```java
var userMessage = new UserMessage("Explain Spring AI");
```

The message can then be included in a prompt:

```java
var prompt = new Prompt(userMessage);
```

This separates the message content from the actual model invocation.

The application constructs the message, places it inside a prompt, and submits the prompt to the model.

---

## Calling a Chat Model

The `ChatModel` interface provides the `call()` method for submitting a prompt.

For example:

```java
var response = chatModel.call(prompt);
```

The method sends the prompt to the configured chat model and returns a `ChatResponse`.

The interaction can be represented as:

```text
User Message
     │
     ▼
Prompt
     │
     ▼
ChatModel.call()
     │
     ▼
AI Model
     │
     ▼
ChatResponse
```

In this example, the application injects `ChatModel` into `ChatModelService`.

Spring AI automatically configures the appropriate implementation based on the configured model provider.

The application therefore does not need to instantiate an Ollama-specific chat model manually.

---

## ChatResponse

`ChatResponse` represents the response returned by a chat model.

It provides access to the generated results and associated response information.

A simplified response structure looks like:

```text
ChatResponse
     │
     ▼
Generation
     │
     ▼
AssistantMessage
     │
     ▼
Generated Text
```

The application accesses the generated text through the response object.

For example:

```java
var response = chatModel.call(prompt);

String content = response.getResult().getOutput().getText();
```

The individual components have different responsibilities.

### Generation

`Generation` represents a generated completion returned by the chat model.

A response can contain generation information and metadata associated with the model's output.

### AssistantMessage

`AssistantMessage` represents a message generated by the AI assistant.

It contains the generated text and can also carry additional information depending on the model and interaction.

### Extracting the Generated Text

The example extracts the assistant's generated text using:

```java
response.getResult().getOutput().getText();
```

This gives the application the actual response to return to the API consumer.

---

## Model Configuration

The example uses Ollama as its chat model provider.

Ollama allows locally running AI models without requiring an external model API.

The application uses Llama 3.2.

The model configuration is defined in:

`src/main/resources/application.yml`

```yaml
spring:
  application:
    name: chat-models
  ai:
    model:
      chat: ollama
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.2
        temperature: 0.7

server:
  port: 8081
```

The configuration defines:

| Property                            | Purpose                                   |
|-------------------------------------|-------------------------------------------|
| `spring.application.name`           | Application name                          |
| `spring.ai.model.chat`              | Selects Ollama as the chat model provider |
| `spring.ai.ollama.base-url`         | Ollama server URL                         |
| `spring.ai.ollama.chat.model`       | Chat model to use                         |
| `spring.ai.ollama.chat.temperature` | Default response temperature              |
| `server.port`                       | Application HTTP port                     |

The application uses port `8081` to avoid conflicting with the Spring AI Fundamentals example.

---

## Ollama

Ollama provides a way to run large language models locally.

In this example, Ollama is responsible for hosting and running Llama 3.2.

The interaction looks like:

```text
Spring Boot Application
        │
        ▼
Spring AI ChatModel
        │
        ▼
Ollama
        │
        ▼
Llama 3.2
        │
        ▼
Generated Response
```

Spring AI communicates with Ollama through its Ollama model integration.

The application interacts with the common `ChatModel` interface rather than directly calling Ollama's HTTP API.

---

## Llama 3.2

Llama 3.2 is the language model used in this example.

The model is selected through:

```yaml
spring:
  ai:
    ollama:
      chat:
        model: llama3.2
```

Before running the application, ensure Ollama is installed and the model is available locally.

Pull the model using:

```bash
ollama pull llama3.2
```

Verify that Ollama is running:

```bash
ollama list
```

The model must be available before making requests to the application.

---

## Chat Model Options

Chat model options control aspects of how a model generates responses.

Different providers expose different model-specific options.

Spring AI provides abstractions for common chat options while also allowing provider-specific configuration.

This example demonstrates temperature configuration using `OllamaChatOptions`.

### Temperature

Temperature controls the randomness of generated responses.

A lower temperature generally produces more predictable and focused responses.

A higher temperature generally allows more variation in generated responses.

For example:

```java
var options = OllamaChatOptions.builder().model(modelName).temperature(0.2).build();
```

The example uses the following values:

| Temperature | General behavior             |
|-------------|------------------------------|
| `0.0`       | Very low randomness          |
| `0.2`       | More focused and predictable |
| `0.7`       | More variation               |
| `1.0`       | Higher variation             |
| `2.0`       | High variation               |

The actual behavior depends on the model implementation and its supported options.

Temperature does not determine whether a response is factually correct.

---

## Default Temperature vs Request-Specific Temperature

The example demonstrates two ways of configuring temperature.

### Default Temperature

The default temperature is configured in `application.yml`:

```yaml
spring:
  ai:
    ollama:
      chat:
        temperature: 0.7
```

When a request does not specify a temperature, the model uses its configured default options.

The service submits a prompt without explicitly overriding the model options:

```java
public String chat(String message) {
    var prompt = new Prompt(new UserMessage(message));

    var response = chatModel.call(prompt);

    return response.getResult().getOutput().getText();
}
```

### Request-Specific Temperature

The application also allows a caller to provide a temperature for an individual request.

The service creates `OllamaChatOptions`:

```java
var options = OllamaChatOptions.builder().model(modelName).temperature(temperature).build();
```

The options are then included in the prompt:

```java
var prompt = new Prompt(new UserMessage(message), options);

var response = chatModel.call(prompt);
```

This allows the request-specific temperature to be used for that model invocation.

Conceptually:

```text
Application Configuration
        │
        ▼
Default Temperature: 0.7
        │
        ├───────────────┐
        │               │
        ▼               ▼
Request Without     Request With
Temperature         Temperature
        │               │
        ▼               ▼
Use Default         Use Request
Options             Specific Options
```

This demonstrates how model options can be supplied at the prompt level.

---

## ChatModelService

`ChatModelService` contains the application's chat model interaction logic.

It is responsible for:

- Receiving user messages.
- Constructing prompts.
- Configuring request-specific model options.
- Calling the chat model.
- Extracting generated responses.
- Detecting incomplete model responses.

The service injects the `ChatModel` abstraction:

```java
private final ChatModel chatModel;
```

It also receives the configured model name:

```java
@Value("${spring.ai.ollama.chat.model}")
String modelName
```

This model name is used when creating request-specific Ollama options.

### Default Chat Interaction

The default method constructs a prompt using the user's message:

```java
public String chat(String message) {
    var prompt = new Prompt(new UserMessage(message));

    return extractResponse(chatModel.call(prompt));
}
```

### Custom Temperature

The overloaded method allows a caller to supply a temperature:

```java
public String chat(String message, double temperature) {
    var options = OllamaChatOptions.builder().model(modelName).temperature(temperature).build();

    var prompt = new Prompt(new UserMessage(message), options);

    return extractResponse(chatModel.call(prompt));
}
```

### Response Extraction

The service extracts the generated response through a dedicated method:

```java
private String extractResponse(ChatResponse response) {
    if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
        throw new IllegalStateException("The chat model returned an incomplete response.");
    }

    return response.getResult().getOutput().getText();
}
```

The method checks that the response, generation, and assistant output are present before accessing the generated text.

This prevents incomplete model responses from causing unexpected null dereferences.

The service intentionally allows an empty assistant response to be returned as an empty string.

---

## REST API

The example exposes chat model functionality through a REST endpoint.

### Endpoint

```text
POST /api/chat-models
```

The endpoint accepts a JSON request containing a message and an optional temperature.

### Request DTO

The request is represented by `ChatModelRequest`:

```java
public record ChatModelRequest(@NotBlank(message = "Message must not be blank") String message,
        @DecimalMin(value = "0.0", message = "Temperature must be at least 0.0")
        @DecimalMax(value = "2.0", message = "Temperature must not exceed 2.0")
        Double temperature) {
}
```

The request contains:

| Field         | Type     | Description                           |
|---------------|----------|---------------------------------------|
| `message`     | `String` | Message submitted to the chat model   |
| `temperature` | `Double` | Optional request-specific temperature |

The temperature uses `Double` rather than primitive `double` because it must be possible to distinguish between a missing value and an explicitly supplied value.

### Request Without Temperature

```json
{
  "message": "Explain Spring AI"
}
```

When temperature is omitted, the application uses the configured model options.

### Request With Temperature

```json
{
  "message": "Explain Spring AI",
  "temperature": 0.2
}
```

When temperature is supplied, the application uses the request-specific value.

### Response DTO

The generated response is represented by `ChatModelResponse`:

```java
public record ChatModelResponse(String response) {}
```

Example response:

```json
{
  "response": "Spring AI is a framework that simplifies integrating AI capabilities into Spring applications."
}
```

The actual response content is generated by the model and will vary.

---

## ChatModelController

`ChatModelController` exposes the chat model service through the REST API.

It receives the request, determines whether a custom temperature was provided, and delegates to the service.

Conceptually:

```text
HTTP Request
     │
     ▼
ChatModelController
     │
     ▼
Validate Request
     │
     ▼
Check Temperature
     │
     ├───────────────────┐
     │                   │
     ▼                   ▼
Default Temperature   Custom Temperature
     │                   │
     ▼                   ▼
ChatModelService.chat()  ChatModelService.chat(message, temperature)
     │                   │
     └─────────┬─────────┘
               │
               ▼
         ChatModel
               │
               ▼
         AI Response
               │
               ▼
         HTTP Response
```

The controller does not directly interact with Ollama.

It delegates all model interaction to `ChatModelService`.

This keeps the REST API layer separate from the AI integration logic.

---

## Request Validation

The example uses Jakarta Bean Validation to validate incoming requests.

The controller uses `@Valid`:

```java
public ResponseEntity<ChatModelResponse> chat(@Valid @RequestBody ChatModelRequest request) {
    // ...
}
```

The request DTO defines the validation rules.

### Message Validation

The `@NotBlank` annotation ensures that the message is not null, empty, or composed entirely of whitespace.

Invalid examples include:

```json
{
  "message": ""
}
```

```json
{
  "message": "   "
}
```

### Temperature Validation

The example restricts temperature to a range between `0.0` and `2.0`.

The lower boundary is enforced using:

```java
@DecimalMin(value = "0.0")
```

The upper boundary is enforced using:

```java
@DecimalMax(value = "2.0")
```

Invalid examples include:

```json
{
  "message": "Explain Spring AI",
  "temperature": -0.1
}
```

```json
{
  "message": "Explain Spring AI",
  "temperature": 2.1
}
```

Invalid requests are rejected with HTTP `400 Bad Request`.

---

## Exception Handling

The example includes `ChatModelExceptionHandler` to handle `IllegalStateException` instances raised while processing chat model requests.

The handler uses `@RestControllerAdvice`:

```java
@RestControllerAdvice(assignableTypes = ChatModelController.class)
public class ChatModelExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, "The chat model is currently unable to process the request.");
    }
}
```

When the service encounters an incomplete model response or a model failure represented by `IllegalStateException`, the exception handler returns HTTP `503 Service Unavailable`.

The API response uses Spring's `ProblemDetail` representation.

Example:

```json
{
  "type": "about:blank",
  "title": "Service Unavailable",
  "status": 503,
  "detail": "The chat model is currently unable to process the request."
}
```

This prevents internal exception details from being exposed to API consumers.

The handler is intentionally scoped to `ChatModelController`.

---

## Testing Chat Models

The example uses:

- JUnit 6
- Mockito
- Spring Boot Test
- MockMvc

The tests focus on application behavior rather than testing Ollama or Llama 3.2 themselves.

No live Ollama instance is required to execute the unit and web-layer tests.

### ChatModelServiceTest

The service tests verify the application's interaction with the `ChatModel` abstraction.

The test mocks the following components:

- `ChatModel`
- `ChatResponse`
- `Generation`
- `AssistantMessage`

This allows the service to be tested without making actual AI model requests.

#### Successful Response

The test verifies that the service returns the generated assistant response.

#### Custom Temperature

The test verifies that the service accepts a request-specific temperature.

#### Prompt Options

The test captures the submitted `Prompt` and verifies that the expected temperature and configured model are included in its `OllamaChatOptions`.

#### User Message

The test captures the prompt and verifies that the user's original message is passed to the chat model.

#### Model Failure

The test verifies that an exception raised by the chat model is propagated by the service.

#### Incomplete Model Responses

The tests verify that the service handles:

- A null `ChatResponse`.
- A response without a generation.
- A generation without an assistant output.

These cases result in an `IllegalStateException` with a meaningful error message.

#### Empty Assistant Response

The test verifies that an empty assistant response is returned as an empty string.

### ChatModelControllerTest

The controller tests use MockMvc to test the REST API without starting a real web server.

The tests verify:

| Scenario                                             | Expected HTTP Status      |
|------------------------------------------------------|---------------------------|
| Valid request without temperature                    | `200 OK`                  |
| Valid request with custom temperature                | `200 OK`                  |
| Blank message                                        | `400 Bad Request`         |
| Missing message                                      | `400 Bad Request`         |
| Null message                                         | `400 Bad Request`         |
| Temperature below minimum                            | `400 Bad Request`         |
| Temperature above maximum                            | `400 Bad Request`         |
| Malformed JSON                                       | `400 Bad Request`         |
| Model failure represented by `IllegalStateException` | `503 Service Unavailable` |
| Minimum temperature boundary (`0.0`)                 | `200 OK`                  |
| Maximum temperature boundary (`2.0`)                 | `200 OK`                  |

The controller tests also verify that the correct service method is invoked depending on whether a temperature is supplied.

### ChatModelsApplicationTest

The application test uses `@SpringBootTest` to verify that the Spring application context loads successfully.

This provides a basic check that the application's Spring configuration and bean wiring are valid.

---

## Why Mock the ChatModel?

Calling an actual AI model during ordinary unit tests introduces unnecessary dependencies.

For example, tests that communicate with Ollama require:

- Ollama to be installed.
- The Ollama server to be running.
- The required model to be downloaded.
- Sufficient system resources to execute the model.
- Additional time for model responses.

Instead, Mockito provides a way to simulate model interactions.

For example:

```java
when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
```

This allows the test to control the response returned by the chat model.

The application can then be tested independently of the external model provider.

The tests focus on verifying that:

- Prompts are constructed correctly.
- User messages are passed to the model.
- Model options are configured correctly.
- Generated responses are extracted correctly.
- Failures are handled as expected.

Testing the actual provider integration can be explored separately through integration tests.

---

## Running the Example

### Prerequisites

Before running the application, ensure that you have:

- Java 21.
- Maven.
- Ollama installed.
- Llama 3.2 downloaded.

Download Llama 3.2:

```bash
ollama pull llama3.2
```

Start Ollama if it is not already running.

### Start the Application

From the repository root:

```bash
mvn -pl 10-spring-ai/chat-models spring-boot:run
```

The application starts on:

```text
http://localhost:8081
```

---

## Trying the API

### Submit a Chat Request

```bash
curl -X POST http://localhost:8081/api/chat-models \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Explain what Spring AI is"
  }'
```

The application submits the message to Llama 3.2 using the configured default model options.

### Submit a Request With a Custom Temperature

```bash
curl -X POST http://localhost:8081/api/chat-models \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Explain what Spring AI is",
    "temperature": 0.2
  }'
```

The application uses the supplied temperature for that model invocation.

### Submit an Invalid Request

```bash
curl -X POST http://localhost:8081/api/chat-models \
  -H "Content-Type: application/json" \
  -d '{
    "message": "",
    "temperature": 0.2
  }'
```

The request is rejected because the message must not be blank.

---

## Running the Tests

Run all tests in the example:

```bash
mvn -pl 10-spring-ai/chat-models test
```

Run the complete Maven verification lifecycle:

```bash
mvn -pl 10-spring-ai/chat-models clean verify
```

Run individual test classes:

```bash
mvn -pl 10-spring-ai/chat-models -Dtest=ChatModelServiceTest test
```

```bash
mvn -pl 10-spring-ai/chat-models -Dtest=ChatModelControllerTest test
```

```bash
mvn -pl 10-spring-ai/chat-models -Dtest=ChatModelsApplicationTest test
```

---

## Chat Model Interaction Flow

A simplified view of the complete example is:

```text
HTTP Client
     │
     ▼
POST /api/chat-models
     │
     ▼
ChatModelController
     │
     ▼
ChatModelRequest
     │
     ▼
Bean Validation
     │
     ▼
ChatModelService
     │
     ▼
UserMessage
     │
     ▼
Prompt
     │
     ▼
ChatModel.call()
     │
     ▼
Ollama Chat Model
     │
     ▼
Llama 3.2
     │
     ▼
ChatResponse
     │
     ▼
Generation
     │
     ▼
AssistantMessage
     │
     ▼
Generated Text
     │
     ▼
ChatModelResponse
     │
     ▼
HTTP Response
```

---

## Key Takeaways

- Spring AI provides abstractions for interacting with different AI chat model providers.
- `ChatModel` provides a common interface for direct chat model interactions.
- `ChatClient` provides a higher-level fluent API built around chat model interactions.
- `Prompt` represents the input submitted to a chat model.
- `UserMessage` represents user-provided conversational input.
- `ChatModel.call()` submits a prompt and returns a `ChatResponse`.
- `ChatResponse` contains generated response information.
- `Generation` represents a generated completion.
- `AssistantMessage` contains the assistant's generated message.
- `OllamaChatOptions` allows Ollama-specific model options to be configured.
- Temperature influences the randomness of generated responses.
- Model options can be configured globally or overridden for individual requests.
- Spring AI allows applications to interact with models through common abstractions rather than provider-specific APIs.
- Mockito makes it possible to test chat model interactions without running an actual AI model.
- MockMvc provides a way to test the REST API independently of a running web server.

---

## Next Example

The next example will explore **Prompt Templates** in Spring AI, including how to define reusable prompts, supply dynamic variables, and construct prompts using `PromptTemplate`.
