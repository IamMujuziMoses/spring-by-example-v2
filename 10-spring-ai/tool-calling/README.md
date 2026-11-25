# Tool Calling

This example demonstrates **tool calling** in Spring AI.

It focuses on how an AI model can request the execution of application-defined Java methods, how Spring AI executes those methods, and how their results are returned to the model to produce a final response.

The example implements calculator tools that allow an AI model to perform addition, subtraction, and multiplication using Java's `BigDecimal` API.

## What This Example Demonstrates

- What tool calling means in Spring AI.
- The relationship between an AI model and application-defined tools.
- Defining tools using `@Tool`.
- Describing tool parameters using `@ToolParam`.
- Registering tools with `ChatClient`.
- How the model requests tool execution.
- How Spring AI invokes the requested Java methods.
- How tool results are returned to the AI model.
- How the model generates a final response using tool results.
- Handling empty AI responses.
- Exposing tool calling through a REST API.
- Testing calculator operations, service behavior, controller endpoints, and Spring AI's tool execution flow.

---

## What Is Tool Calling?

Tool calling allows an AI model to request that an application execute a specific operation on its behalf.

An AI model can understand a user's request and determine that it needs additional functionality to answer it.

However, the model does not directly execute application code. Instead, it produces a structured tool call describing the operation it wants performed and the arguments required.

The application executes the requested operation and returns the result to the model.

For example, consider this question:

```text
What is 25 multiplied by 4?
```

The model can determine that multiplication is required and request the execution of a calculator tool.

The application executes the Java method and returns `100` to the model.

The model can then formulate a natural-language response.

---

## Why Tool Calling?

Language models are good at understanding natural language, generating explanations, and interpreting user instructions.

However, they are not necessarily the right place to perform every operation.

Some tasks require application functionality, such as:

- Performing precise mathematical calculations.
- Retrieving information from a database.
- Calling external APIs.
- Accessing application services.
- Sending notifications.
- Performing business operations.

Tool calling allows an AI model to use these capabilities without implementing the underlying functionality itself.

The application remains responsible for executing the operations.

---

## Tool Calling Architecture

This example uses Spring AI's `ChatClient` and application-defined Java methods.

The architecture looks like:

```text
User
│
▼
REST Controller
│
▼
ToolCallingService
│
▼
ChatClient
│
▼
AI Model
│
│ Requests a tool call
▼
Spring AI Tool Execution
│
▼
CalculatorTools
│
▼
Tool Result
│
▼
AI Model
│
▼
Final Response
│
▼
REST Controller
│
▼
User
```

The important distinction is that the AI model requests tool execution, while Spring AI and the application execute the actual Java method.

---

## Defining Tools

Spring AI provides the `@Tool` annotation for exposing Java methods as tools that an AI model can request.

This example defines a calculator component containing three operations:

- Addition.
- Subtraction.
- Multiplication.

The component is implemented as a Spring-managed bean.

### CalculatorTools

```java
@Component
public class CalculatorTools {

    @Tool(description = "Add two numbers and return their sum.")
    public BigDecimal add(@ToolParam(description = "The first number") BigDecimal firstNumber,
                          @ToolParam(description = "The second number") BigDecimal secondNumber) {
        return firstNumber.add(secondNumber);
    }

    @Tool(description = "Subtract the second number from the first number.")
    public BigDecimal subtract(@ToolParam(description = "The first number") BigDecimal firstNumber,
                               @ToolParam(description = "The number to subtract") BigDecimal secondNumber) {
        return firstNumber.subtract(secondNumber);
    }

    @Tool(description = "Multiply two numbers and return their product.")
    public BigDecimal multiply(@ToolParam(description = "The first number") BigDecimal firstNumber,
                               @ToolParam(description = "The second number") BigDecimal secondNumber) {
        return firstNumber.multiply(secondNumber);
    }
}
```

### Understanding `@Tool`

The `@Tool` annotation marks a Java method as a tool that can be exposed to an AI model.

For example:

```java
@Tool(description = "Multiply two numbers and return their product.")
public BigDecimal multiply(@ToolParam(description = "The first number") BigDecimal firstNumber,
        @ToolParam(description = "The second number") BigDecimal secondNumber) {
    return firstNumber.multiply(secondNumber);
}
```

The annotation provides a description of what the method does.

This description helps the model determine when the tool is appropriate.

### Understanding `@ToolParam`

The `@ToolParam` annotation describes the parameters accepted by a tool.

For example:

```java
@ToolParam(description = "The first number") BigDecimal firstNumber
```

The description helps the model understand what value should be supplied for the parameter.

Clear tool and parameter descriptions are important because the model uses them when deciding which tool to request and what arguments to provide.

### Why Use `BigDecimal`?

The calculator uses Java's `BigDecimal` instead of `double` or `float`.

Floating-point types can introduce precision errors when performing decimal arithmetic.

For example, financial calculations and other decimal-sensitive operations often require predictable decimal precision.

`BigDecimal` provides decimal arithmetic without the binary floating-point representation issues associated with `double` and `float`.

---

## Registering Tools with ChatClient

Defining a tool does not automatically make it available to the AI model.

The application must expose the tool when making a request through Spring AI.

This example injects `CalculatorTools` into `ToolCallingService` and registers it with the `ChatClient`.

```java
@Service
public class ToolCallingService {

    private final ChatClient chatClient;
    
    private final CalculatorTools calculatorTools;

    public ToolCallingService(ChatClient.Builder chatClientBuilder, CalculatorTools calculatorTools) {
        this.chatClient = chatClientBuilder.build();
        this.calculatorTools = calculatorTools;
    }

    public ToolCallingResponse answerQuestion(String question) {
        String answer = chatClient.prompt().user(question).tools(calculatorTools).call().content();

        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException("The AI model returned an empty response.");
        }

        return new ToolCallingResponse(question, answer);
    }
}
```

The important part is:

```java
.tools(calculatorTools)
```

This registers the calculator component as a source of tools for the current ChatClient request.

The model can then request any of the exposed calculator operations.

Registering the tools on the request also means that the application controls which tools are available for that particular interaction.

---

## How Tool Calling Works

Tool calling involves multiple interactions between the application and the AI model.

Consider this question:

```text
What is 6 multiplied by 7?
```

### 1. User Sends a Request

The user submits a question through the REST API.

```text
What is 6 multiplied by 7?
```

The controller passes the question to `ToolCallingService`.

### 2. Application Sends the Request to the Model

The service creates a ChatClient request and registers the calculator tools.

```java
chatClient.prompt()
.user(question)
.tools(calculatorTools)
.call()
.content();
```

Spring AI sends the user's question and the available tool definitions to the model.

### 3. Model Requests Tool Execution

The model determines that multiplication is required.

Instead of returning a final answer immediately, it requests the `multiply` tool with the following arguments:

```json
{
"firstNumber": 6,
"secondNumber": 7
}
```

The model does not execute the multiplication itself.

It only requests that the application execute the operation.

### 4. Spring AI Executes the Tool

Spring AI processes the tool call and invokes the corresponding Java method:

```java
calculatorTools.multiply(new BigDecimal("6"), new BigDecimal("7"));
```

The method returns:

```text
42
```

### 5. Tool Result Is Returned to the Model

Spring AI adds the tool result to the conversation and sends the updated interaction back to the model.

Conceptually:

```text
User Question
│
▼
AI Model
│
▼
Tool Call: multiply(6, 7)
│
▼
CalculatorTools.multiply()
│
▼
Result: 42
│
▼
AI Model
```

### 6. Model Generates the Final Answer

The model uses the tool result to generate a natural-language response.

For example:

```text
6 multiplied by 7 equals 42.
```

The final response is returned to the application.

---

## Tool Calling vs Function Calling

The terms *tool calling* and *function calling* are closely related.

Function calling commonly refers to the model requesting the execution of a function exposed by an application.

Tool calling is a broader concept that can include functions and other application capabilities.

In Spring AI, tools provide a standardized way to expose application functionality to supported AI models.

The underlying model provider may use its own terminology and representation for tool calls.

---

## Tool Calling and Application Responsibilities

Tool calling does not transfer application responsibility to the AI model.

The application remains responsible for:

- Defining the available tools.
- Controlling which tools are exposed.
- Validating tool arguments.
- Executing the requested operations.
- Enforcing authorization and business rules.
- Handling tool execution failures.
- Returning appropriate results.

For example, a banking application should not allow an AI model to transfer money merely because it requested a transfer tool.

The application must independently enforce authorization, transaction limits, validation, and any required user confirmation.

Tool calling should therefore be treated as an application-controlled execution mechanism, not as an authorization mechanism.

---

## Exposing Tool Calling Through a REST API

This example exposes tool calling through a REST endpoint.

### Request DTO

The request contains the user's question.

```java
public record ToolCallingRequest(@NotBlank(message = "Question must not be blank") String question) {
}
```

The `@NotBlank` constraint ensures that the question is not null, empty, or whitespace-only.

### Response DTO

The response contains the original question and the answer generated by the AI model.

```java
public record ToolCallingResponse(String question, String answer) {
}
```

### Controller

The controller exposes the following endpoint:

```java
@RestController
@RequestMapping("/api/tool-calling")
public class ToolCallingController {

    private final ToolCallingService toolCallingService;

    public ToolCallingController(ToolCallingService toolCallingService) {
        this.toolCallingService = toolCallingService;
    }

    @PostMapping
    public ToolCallingResponse answerQuestion(@Valid @RequestBody ToolCallingRequest request) {
        return toolCallingService.answerQuestion(request.question());
    }
}
```

The controller delegates the request to the service.

It does not need to know which tools are available or how they are executed.

Those responsibilities belong to the service and Spring AI.

---

## Handling AI Errors

The service checks whether the model returns an empty response.

```java
if (answer == null || answer.isBlank()) {
        throw new IllegalStateException("The AI model returned an empty response.");
}
```

An empty response is treated as an unsuccessful operation.

The example uses a REST exception handler to translate this failure into an HTTP `502 Bad Gateway` response.

```java
@RestControllerAdvice(assignableTypes = ToolCallingController.class)
public class ToolCallingExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "The AI service could not complete the requested operation.");

        problemDetail.setTitle("Tool Calling Operation Failed");
        problemDetail.setType(URI.create("urn:problem:tool-calling-operation-failed"));

        return problemDetail;
    }
}
```

The handler returns a generic error detail instead of exposing internal exception information to API clients.

---

## Application Configuration

The example uses Ollama as the chat model provider.

The application is configured to use the `llama3.2` model.

### application.yml

```yaml
spring:
  application:
    name: tool-calling
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
  port: 8087

```

### Configuration Explained

| Property                                    | Purpose                                    |
|---------------------------------------------|--------------------------------------------|
| `spring.ai.model.chat`                      | Selects Ollama as the chat model provider. |
| `spring.ai.ollama.base-url`                 | Specifies the Ollama server URL.           |
| `spring.ai.ollama.chat.model`               | Selects the chat model.                    |
| `spring.ai.ollama.chat.options.temperature` | Controls response randomness.              |
| `server.port`                               | Sets the application port.                 |

The temperature is set to `0.2` to encourage relatively consistent responses.

---

## Testing Tool Calling

The example includes tests at different levels.

The objective is to verify the calculator operations, service behavior, REST API, and Spring AI's tool execution flow without requiring an external model for the standard test suite.

### CalculatorToolsTest

Tests the calculator operations directly.

The tests cover:

- Adding two numbers.
- Subtracting two numbers.
- Multiplying two numbers.
- Preserving decimal precision during multiplication.

For example:

```java
@Test
void shouldMultiplyTwoNumbers() {
    BigDecimal result = calculatorTools.multiply(new BigDecimal("6"), new BigDecimal("7"));

    assertThat(result).isEqualByComparingTo("42");
}
```

These tests verify the actual Java methods independently of the AI model.

### ToolCallingServiceTest

Tests the service using mocked Spring AI components.

The tests cover:

- Building the ChatClient.
- Supplying calculator tools to a ChatClient request.
- Returning the model's answer.
- Handling null model responses.
- Handling blank model responses.
- Propagating failures from the chat model.

These tests verify the service's interaction with Spring AI without requiring a real model.

### ToolCallingControllerTest

Tests the REST API using `MockMvc`.

The tests cover:

- Successfully answering a question.
- Rejecting a missing question.
- Rejecting a blank question.
- Returning `502 Bad Gateway` when tool calling fails.

The tests also verify request validation and the API's error response.

### ToolCallingApplicationTest

Verifies that the Spring application context loads successfully.

It checks that the required application components and Spring AI dependencies are available.

### ToolCallingIntegrationTest

This test verifies the Spring AI tool execution flow using:

- The real `ChatClient`.
- The actual `CalculatorTools` implementation.
- A mocked `ChatModel`.

The mocked model returns a tool-call response requesting multiplication.

Spring AI processes the tool call, invokes the actual calculator method, and supplies the result to the model.

The mocked model then returns the final answer.

The test verifies that:

- The model initially requests the calculator tool.
- The actual calculator operation is executed.
- The conversation includes the tool result.
- The model is called again after tool execution.
- The final answer is returned correctly.

Conceptually:

```text
Mocked ChatModel
│
▼
Tool Call: multiply(6, 7)
│
▼
Real CalculatorTools
│
▼
Result: 42
│
▼
Mocked ChatModel
│
▼
Final Answer
```

This is a deterministic integration test because the model response is controlled by the test.

It verifies Spring AI's tool execution integration without depending on the behavior or availability of Ollama.

### Why Mock the ChatModel?

A real model can choose different tools, generate different arguments, or produce different wording depending on the prompt and model configuration.

That makes tests involving a live model less predictable.

Mocking the `ChatModel` allows the test to provide a known tool-call response and verify how Spring AI processes it.

This separates two concerns:

1. Whether Spring AI correctly executes a requested tool.
2. Whether a particular model correctly decides when and how to request that tool.

The first is covered by the deterministic integration test.

The second can be explored with an optional real-model integration test.

---

## Testing with a Real Ollama Model

The application can also be tested manually with Ollama.

Make sure Ollama is running and the required model is available.

Pull the model if necessary:

```bash
ollama pull llama3.2
```

Start the application:

```bash
mvn -pl 10-spring-ai/tool-calling spring-boot:run
```

Send a request:

```bash
curl -X POST http://localhost:8087/api/tool-calling \
-H "Content-Type: application/json" \
-d '{
"question": "What is 25 multiplied by 4?"
}'
```

An example response is:

```json
{
"question": "What is 25 multiplied by 4?",
"answer": "25 multiplied by 4 equals 100."
}
```

The exact wording of the answer may vary depending on the model.

Try other questions:

```json
{
"question": "What is 150 plus 75?"
}
```

```json
{
"question": "What is 200 minus 85?"
}
```

```json
{
"question": "What is 12 multiplied by 15?"
}
```

The model should select the appropriate calculator operation and use its result to formulate an answer.

Actual tool selection depends on the model's tool-calling capabilities and behavior.

---

## Running the Example

From the repository root:

```bash
mvn -pl 10-spring-ai/tool-calling spring-boot:run
```

The application starts on port `8087`.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 10-spring-ai/tool-calling test
```

Run the complete module verification:

```bash
mvn -pl 10-spring-ai/tool-calling clean verify
```

The standard test suite does not require Ollama to be running because the chat model is mocked in the tests.

---

## Key Takeaways

- Tool calling allows AI models to request application-defined operations.
- The model requests tool execution but does not directly execute application code.
- Spring AI provides `@Tool` to expose Java methods as tools.
- `@ToolParam` describes the parameters accepted by a tool.
- Tools must be registered with the ChatClient to make them available to a request.
- Spring AI processes tool calls and invokes the corresponding Java methods.
- Tool results are returned to the model so it can generate a final response.
- Applications remain responsible for validation, authorization, and business rules.
- `BigDecimal` provides reliable decimal arithmetic for the calculator operations.
- Tool calling can be tested deterministically by mocking the chat model.
- Real-model testing can additionally verify whether a model selects and invokes tools as expected.

---

## Next Example

The next example will explore **AI Application Testing** in Spring AI, including testing AI services, mocking chat and embedding models, testing prompts and tool interactions, and verifying AI application behavior without relying on live model providers.
