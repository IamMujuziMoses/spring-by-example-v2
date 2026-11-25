# AI Application Testing

This example demonstrates **testing AI-powered applications with Spring AI and Spring Boot**.

It focuses on how to test an application that integrates with an AI chat model without requiring a running AI provider during normal test execution.

The example implements a software technology assistant that accepts questions, sends them to an AI model with a system prompt, and returns the generated answer.

It demonstrates different testing approaches, including unit testing, web-layer testing, testing the actual Spring AI `ChatClient` interaction, and application context testing.

## What This Example Demonstrates

- Testing AI-powered applications with Spring AI.
- Unit testing services that use `ChatClient`.
- Mocking Spring AI components using Mockito.
- Testing system prompts and user messages.
- Testing successful AI responses.
- Handling null and blank AI responses.
- Testing exception propagation.
- Testing REST endpoints with `MockMvc`.
- Testing request validation and malformed JSON.
- Testing HTTP error responses with `ProblemDetail`.
- Testing the actual `ChatClient` interaction with a mocked `ChatModel`.
- Capturing and inspecting prompts sent to the chat model.
- Testing Spring application context initialization.
- Testing AI applications without requiring a running Ollama instance.

---

## What Is AI Application Testing?

AI application testing involves verifying the behavior of applications that integrate with AI models.

Unlike traditional applications, AI-powered applications depend on external model providers to generate responses.

For example, an application might:

1. Receive a question from a user.
2. Construct a prompt containing system instructions and the user's question.
3. Send the prompt to an AI model.
4. Receive the generated response.
5. Return the response to the user.

A simplified flow looks like:

```text
HTTP Request
│
▼
REST Controller
│
▼
AI Assistant Service
│
▼
Prompt Construction
│
├── System Message
│
└── User Message
│
▼
ChatClient
│
▼
ChatModel
│
▼
AI Generated Response
│
▼
REST Response
```

Testing this application requires verifying both traditional application behavior and the integration points responsible for communicating with AI models.

---

## Application Overview

The example implements a software technology assistant.

Users submit questions about software engineering concepts, and the application uses an AI model to generate answers.

For example:

```json
{
"question": "What is Spring Boot?"
}
```

The application sends the question to the AI model and returns a response:

```json
{
"question": "What is Spring Boot?",
"answer": "Spring Boot simplifies the development of Spring applications."
}
```

The application exposes the following endpoint:

| Method | Endpoint            | Description                                          |
|--------|---------------------|------------------------------------------------------|
| POST   | `/api/ai-assistant` | Submit a question and receive an AI-generated answer |

---

## Spring AI Configuration

The example uses Spring AI with Ollama as the chat model provider.

The application is configured to use the `llama3.2` model.

```yaml
spring:
  application:
    name: ai-application-testing
  ai:
    model:
      chat: ollama
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.2
        options:
          temperature: 0.2

server:
  port: 8088
```

The configuration specifies:

- `spring.ai.model.chat`: Selects Ollama as the chat model provider.
- `spring.ai.ollama.base-url`: Specifies the Ollama server address.
- `spring.ai.ollama.chat.model`: Selects the `llama3.2` model.
- `temperature`: Controls the randomness of generated responses.
- `server.port`: Runs the application on port `8088`.

The example uses a real Ollama model when the application is run normally.

However, the automated tests replace the model with mocks, so Ollama does not need to be running during test execution.

---

## AI Assistant Service

The `AiAssistantService` is responsible for constructing prompts, invoking the AI model, and processing the generated response.

It uses Spring AI's `ChatClient` abstraction.

### System Prompt

The service defines a system prompt that establishes the assistant's behavior:

```java
static final String SYSTEM_PROMPT = """
        You are a software technology assistant.
        Explain software engineering concepts clearly and accurately.
        Use practical examples where appropriate.
        Adapt the level of detail to the user's question.
        If you are uncertain about something, acknowledge the uncertainty.
        """;
```

A system prompt provides instructions that guide how the AI model should respond.

The system prompt and user question serve different purposes:

| Message        | Purpose                                           |
|----------------|---------------------------------------------------|
| System message | Defines the assistant's behavior and instructions |
| User message   | Contains the question submitted by the user       |

### Constructing the ChatClient

The service receives a `ChatClient.Builder` through constructor injection:

```java
public AiAssistantService(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
}
```

This allows Spring AI to configure the client using the selected chat model provider.

### Sending a Question

The service sends the system prompt and user question to the model:

```java
String answer = chatClient.prompt().system(SYSTEM_PROMPT).user(question).call().content();
```

The `ChatClient` provides a fluent API for constructing prompts and invoking the model.

### Handling Empty Responses

The service checks whether the model returned a usable response:

```java
if (answer == null || answer.isBlank()) {
        throw new IllegalStateException("The AI model returned an empty response.");
}
```

This is important because an application should not return an empty answer as though the AI operation succeeded.

The exception is handled by the application's exception handler.

---

## Testing AI Applications

The example uses several testing approaches.

Each approach focuses on a different application boundary.

```text
AI Application Testing
                 │
┌────────────────┼────────────────┐
│                │                │
▼                ▼                ▼
Unit Tests     Web Tests      Integration Tests
│                │                │
▼                ▼                ▼
Service   Logic HTTP Behavior ChatClient Flow
                 │
                 ▼
            Context Testing
```

The tests use:

- JUnit 6 for writing and executing tests.
- Mockito for mocking dependencies.
- AssertJ for assertions.
- Spring Boot Test for application testing.
- Spring MVC Test and `MockMvc` for HTTP testing.

---

## 1. Unit Testing the AI Assistant Service

**Test class:** `AiAssistantServiceTest`

The service unit tests focus on application logic without invoking an actual AI model.

The `ChatClient` API is mocked to control the responses returned by the model interaction.

### Why Mock ChatClient?

Calling an external AI provider during every unit test introduces several problems:

- Tests depend on an external service.
- Tests may require API credentials or a running model.
- Test execution becomes slower.
- Model-generated responses may vary.
- Tests become less predictable.

Mocking `ChatClient` allows the service behavior to be tested independently of the external provider.

### Mocking the ChatClient API

The `ChatClient` API uses a fluent interface.

A simplified interaction looks like:

```text
ChatClient
│
▼
prompt()
│
▼
system()
│
▼
user()
│
▼
call()
│
▼
content()
```

The test mocks each stage of this interaction.

For example:

```java
when(chatClient.prompt()).thenReturn(requestSpec);
when(requestSpec.system(anyString())).thenReturn(requestSpec);
when(requestSpec.user(QUESTION)).thenReturn(requestSpec);
when(requestSpec.call()).thenReturn(responseSpec);
when(responseSpec.content()).thenReturn(ANSWER);
```

This allows the test to simulate a successful AI response without contacting a real model.

### Successful Response

The service should return the original question together with the generated answer.

```java
AiAnswerResponse response = aiAssistantService.answerQuestion(QUESTION);

assertThat(response.question()).isEqualTo(QUESTION);
assertThat(response.answer()).isEqualTo(ANSWER);
```

The test also verifies that the service supplies the expected system prompt and user question.

### Empty AI Response

The service should reject null and blank responses.

The tests cover both situations:

```java
when(responseSpec.content()).thenReturn(null);
```

And:

```java
when(responseSpec.content()).thenReturn(" ");
```

Both should result in:

```text
IllegalStateException:
The AI model returned an empty response.
```

### Exception Propagation

The service should also propagate failures from the `ChatClient` interaction.

For example:

```java
when(requestSpec.call()).thenThrow(new IllegalStateException("Chat model unavailable"));
```

The test verifies that the service does not silently hide the failure.

### Service Test Coverage

| Scenario           | Expected behavior                         |
|--------------------|-------------------------------------------|
| Valid AI response  | Returns the question and answer           |
| Null AI response   | Throws `IllegalStateException`            |
| Blank AI response  | Throws `IllegalStateException`            |
| ChatClient failure | Propagates the exception                  |
| System prompt      | Supplies the expected system instructions |
| User question      | Supplies the submitted question           |

---

## 2. Testing the REST Controller

**Test class:** `AiAssistantControllerTest`

The controller tests verify the HTTP interface independently of the AI service implementation.

The example uses `@WebMvcTest` and `MockMvc`.

### Why Use @WebMvcTest?

`@WebMvcTest` loads the Spring MVC components needed to test the web layer without starting the entire application.

The service is replaced with a Mockito mock.

```java
@WebMvcTest(AiAssistantController.class)
class AiAssistantControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiAssistantService aiAssistantService;
}
```

This isolates the controller from the actual AI model integration.

### Successful HTTP Request

The test submits a valid question:

```java
mockMvc.perform(post("/api/ai-assistant").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "What is Spring Boot?"
                                }
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.question").value("What is Spring Boot?"))
```

The test verifies that the endpoint accepts the request and returns the expected response.

### Request Validation

The request DTO uses Jakarta Bean Validation:

```java
public record AiQuestionRequest(@NotBlank(message = "Question must not be blank") String question) {
}
```

The controller uses `@Valid` to trigger validation.

The tests cover:

- Missing question.
- Blank question.

Both should return HTTP 400 Bad Request.

### Malformed JSON

The controller test also verifies that malformed JSON is rejected.

For example:

```json
{
"question":
```

The expected response is:

```text
HTTP 400 Bad Request
```

This verifies that invalid JSON is rejected by Spring MVC before the service is invoked.

### AI Service Failure

The controller test simulates an AI service failure:

```java
when(aiAssistantService.answerQuestion(anyString()))
.thenThrow(new IllegalStateException("The AI model returned an empty response."));
```

The exception handler converts the exception into an HTTP 502 Bad Gateway response.

The test verifies:

```java
.andExpect(status().isBadGateway())
.andExpect(jsonPath("$.title").value("AI Assistant Operation Failed"));
```

### Controller Test Coverage

| Scenario           | Expected HTTP status |
|--------------------|----------------------|
| Valid question     | 200 OK               |
| Missing question   | 400 Bad Request      |
| Blank question     | 400 Bad Request      |
| Malformed JSON     | 400 Bad Request      |
| AI service failure | 502 Bad Gateway      |

---

## 3. Testing the Spring AI ChatClient Integration

**Test class:** `AiAssistantIntegrationTest`

This test demonstrates a different approach from the service unit test.

Instead of mocking `ChatClient`, it uses a real Spring AI `ChatClient` and mocks the underlying `ChatModel`.

This allows the test to exercise the actual prompt construction and model invocation flow.

### Why Use a Real ChatClient?

Mocking `ChatClient` is useful for isolating service logic, but it does not verify that the application constructs the expected prompt correctly.

Using a real `ChatClient` allows the test to verify:

- The system message is included.
- The user question is included.
- The prompt is passed to the chat model.
- The generated response is returned by the service.

The external model itself remains mocked.

### Creating the ChatClient

The test creates a real `ChatClient` using a mocked `ChatModel`:

```java
when(chatModel.getOptions()).thenReturn(ChatOptions.builder().build());

ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel);

aiAssistantService = new AiAssistantService(chatClientBuilder);
```

This avoids requiring an actual Ollama connection.

### Mocking the ChatModel Response

The test creates a real `ChatResponse` containing an assistant message:

```java
ChatResponse chatResponse = ChatResponse.builder().generations(List.of(new Generation(new AssistantMessage(ANSWER))))
        .metadata(ChatResponseMetadata.builder().model("test-model").build()).build();


when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
```

The mocked model returns a deterministic response whenever the `ChatClient` invokes it.

### Capturing the Prompt

The test uses Mockito's `ArgumentCaptor` to capture the prompt passed to the model:

```java
ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);

verify(chatModel).call(promptCaptor.capture());

List<Message> messages = promptCaptor.getValue().getInstructions();
```

The captured prompt can then be inspected.

### Verifying the System Message

```java
assertThat(messages)
.anySatisfy(message -> {
assertThat(message.getMessageType()).isEqualTo(MessageType.SYSTEM);
assertThat(message.getText())
.contains("You are a software technology assistant.");
});
```

This verifies that the system instructions are included in the prompt.

### Verifying the User Message

```java
assertThat(messages)
.anySatisfy(message -> {
assertThat(message.getMessageType()).isEqualTo(MessageType.USER);
assertThat(message.getText()).isEqualTo(QUESTION);
});
```

This verifies that the user's question is included.

### Integration Test Flow

```text
AiAssistantService
│
▼
Real ChatClient
│
▼
Prompt Construction
│
├── System Message
│
└── User Message
│
▼
Mocked ChatModel
│
▼
Mocked Model Response
│
▼
AiAssistantService
│
▼
AiAnswerResponse
```

The important distinction is that the `ChatClient` is real, while the `ChatModel` is mocked.

This tests the Spring AI client interaction without depending on an external AI provider.

---

## 4. Testing the Spring Application Context

**Test class:** `AiApplicationTestingApplicationTest`

The application context test verifies that Spring Boot can start the application context and create the required dependencies.

It uses `@SpringBootTest`:

```java
@SpringBootTest
class AiApplicationTestingApplicationTest {

    @Autowired
    private AiAssistantService aiAssistantService;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Test
    void shouldLoadApplicationContext() {
        assertThat(aiAssistantService).isNotNull();
        assertThat(chatClientBuilder).isNotNull();
    }
}
```

This verifies that:

- The Spring application context loads successfully.
- The AI assistant service is registered as a Spring bean.
- Spring AI provides the required `ChatClient.Builder`.
- Dependency injection works correctly.

The test does not invoke the AI model.

It verifies application initialization and bean configuration.

---

## Testing Strategies Compared

The example demonstrates different levels of testing and the responsibilities of each.

| Testing approach            | Main focus                                    | External AI provider |
|-----------------------------|-----------------------------------------------|----------------------|
| Service unit tests          | Service logic and response handling           | Mocked               |
| Controller tests            | HTTP behavior and validation                  | Not involved         |
| ChatClient integration test | Prompt construction and model invocation      | Mocked               |
| Application context test    | Spring configuration and dependency injection | Not invoked          |

These approaches complement each other.

A service unit test verifies the service's behavior, while the ChatClient integration test verifies that the actual Spring AI client receives the expected prompt.

The controller tests verify the HTTP contract, and the context test verifies that the application can be assembled by Spring.

---

## Why Not Use a Real AI Model in Every Test?

AI model calls introduce external dependencies that are not always suitable for automated tests.

For example:

- The AI provider might be unavailable.
- Network connectivity might be interrupted.
- Model responses can vary.
- Model execution can take considerable time.
- API usage may introduce additional costs.
- Tests may require credentials or local model installation.

For these reasons, the example uses mocks for its normal automated test suite.

Mocking makes the tests faster, deterministic, and suitable for continuous integration.

However, mocking does not verify the actual model's response quality or behavior.

A separate provider-level integration test can be introduced when verifying the application against a real model is necessary.

---

## Testing Limitations

The example intentionally focuses on application-level testing.

It does not cover:

- Evaluating the semantic quality of AI-generated responses.
- Measuring hallucination rates.
- Testing model accuracy against large evaluation datasets.
- Testing model performance and latency.
- Testing embeddings and vector retrieval.
- Testing RAG evaluation with `RelevancyEvaluator`.
- Testing tool-calling behavior.
- Running automated tests against multiple AI providers.
- Load testing AI model endpoints.

These topics can be explored in separate examples.

---

## Running the Example

### Prerequisites

- Java 21.
- Maven.
- Ollama.
- The `llama3.2` model.

Pull the model:

```bash
ollama pull llama3.2
```

Start Ollama if it is not already running.

From the repository root:

```bash
mvn -pl 10-spring-ai/ai-application-testing spring-boot:run
```

The application starts on:

```text
http://localhost:8088
```

### Submitting a Question

```bash
curl -X POST http://localhost:8088/api/ai-assistant \
-H "Content-Type: application/json" \
-d '{
"question": "What is Spring Boot?"
}'
```

The application returns an AI-generated answer.

---

## Running the Tests

The automated tests do not require Ollama to be running because the AI model interactions are mocked.

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 10-spring-ai/ai-application-testing test
```

To run the example together with its required Maven reactor dependencies:

```bash
mvn -pl 10-spring-ai/ai-application-testing -am test
```

To run the full verification lifecycle:

```bash
mvn -pl 10-spring-ai/ai-application-testing -am verify
```

---

## Key Takeaways

- AI-powered applications require testing beyond ordinary business logic.
- Spring AI's `ChatClient` can be mocked to test service behavior independently.
- Mockito allows deterministic testing of AI responses and failures.
- System prompts and user messages can be verified independently.
- A real `ChatClient` can be tested using a mocked `ChatModel`.
- `ArgumentCaptor` makes it possible to inspect prompts sent to the model.
- `MockMvc` can test AI-powered REST endpoints without invoking an actual AI model.
- Bean Validation protects endpoints against invalid requests.
- `ProblemDetail` provides structured HTTP error responses.
- `@SpringBootTest` verifies application context initialization and dependency injection.
- Mocking AI model interactions makes automated tests suitable for continuous integration.
- Mock-based tests do not replace evaluation against a real model when model quality or provider behavior needs to be verified.

---

## Next Example

The next example will explore **Model Provider Integration** in Spring AI, including integrating with different AI model providers, configuring provider-specific settings, and working with Spring AI's model abstractions.
