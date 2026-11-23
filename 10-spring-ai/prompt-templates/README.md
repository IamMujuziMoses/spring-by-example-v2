# Prompt Templates

This example demonstrates **prompt templates** in Spring AI.

It focuses on how to define reusable prompts, introduce dynamic variables into prompt templates, supply values at runtime, and use the rendered prompt with a Spring AI `ChatModel` to generate responses.

The example uses a classpath-based `.st` prompt template and Ollama as the chat model provider.

## What This Example Demonstrates

- What prompt templates are and why they are useful.
- The role of `PromptTemplate` in Spring AI.
- Defining reusable prompts in external `.st` resource files.
- Using placeholders to represent dynamic prompt variables.
- Supplying variable values at runtime.
- Rendering a prompt before sending it to a chat model.
- Using `ChatModel` to generate a response from a rendered prompt.
- Separating prompt content from application logic.
- Validating prompt template request data.
- Handling chat model failures.
- Testing prompt rendering and response generation.

---

## What Are Prompt Templates?

A prompt is the instruction or input supplied to an AI model to guide its response.

For example, a simple prompt might be:

```text
Explain Spring Boot to a beginner.
```

However, applications often need to generate similar prompts for different users, technologies, audiences, or tasks.

Instead of constructing a new prompt manually every time, a prompt template provides a reusable structure containing placeholders for dynamic values.

For example:

```text
Explain {technology} to {audience}.

The explanation should have a {detailLevel} level of detail.

Address the following request:
{question}
```

The application supplies values for the placeholders at runtime.

This makes it possible to reuse the same prompt structure for different requests.

---

## Why Use Prompt Templates?

Without prompt templates, an application might construct prompts directly inside its service methods:

```java
String prompt = "Explain " + technology + " to " + audience + " with a " + detailLevel + " level of detail.";
```

This approach can become difficult to maintain when prompts contain many instructions, formatting requirements, or dynamic values.

It also mixes prompt content with application logic.

Prompt templates provide a cleaner separation:

```text
Application Logic
       │
       ▼
Prompt Template
       │
       ▼
Dynamic Variables
       │
       ▼
Rendered Prompt
       │
       ▼
Chat Model
       │
       ▼
Generated Response
```

The template can be maintained independently of the Java code that uses it.

This is particularly useful when prompts become longer or need frequent adjustments.

---

## The `PromptTemplate` Class

Spring AI provides `PromptTemplate` for creating prompts from reusable templates.

A template contains placeholders that are replaced with values supplied by the application.

For example:

```java
PromptTemplate promptTemplate = new PromptTemplate("Explain {technology} to {audience}.");
```

The application can provide the placeholder values using a map:

```java
Map<String, Object> variables = Map.of(
        "technology", "Spring Boot",
        "audience", "a beginner"
);
```

The template can then be rendered:

```java
Prompt prompt = promptTemplate.create(variables);
```

The resulting prompt contains:

```text
Explain Spring Boot to a beginner.
```

The resulting `Prompt` can be supplied to a chat model.

---

## Externalizing Prompt Templates

This example stores the prompt template in a separate resource file rather than embedding it directly in Java code.

The resource is located at:

```text
src/main/resources/
└── prompts/
    └── technology-explanation.st
```

The file contains:

```text
You are a knowledgeable software development tutor.

Explain {technology} to {audience}.

The explanation should have a {detailLevel} level of detail.

Address the following request:
{question}

Use clear language and practical examples where appropriate.
Organize the response with relevant headings and bullet points.
```

The `.st` extension is commonly used for StringTemplate-style templates.

Spring AI's `PromptTemplate` supports placeholder-based prompt rendering, allowing the application to replace variables such as `{technology}` and `{audience}` with runtime values.

Externalizing the prompt provides several benefits:

- Prompt content is separated from Java application logic.
- Prompts can be updated without changing the service implementation.
- Longer prompts are easier to read and maintain.
- Prompt instructions can be reviewed independently.
- The same template can be reused for multiple requests.

---

## Loading the Prompt Template

The service loads the template from the classpath using Spring's `Resource` abstraction.

The resource is injected using:

```java
@Value("classpath:/prompts/technology-explanation.st")
private Resource promptResource;
```

The template content is read as UTF-8:

```java
String templateContent = promptResource.getContentAsString(StandardCharsets.UTF_8);
```

A `PromptTemplate` is then created from the loaded content:

```java
this.promptTemplate = new PromptTemplate(templateContent);
```

This initialization allows the service to reuse the loaded template when processing requests.

The prompt is not reconstructed manually for every request.

---

## Dynamic Prompt Variables

The template defines four variables:

| Variable        | Purpose                                  |
|-----------------|------------------------------------------|
| `{technology}`  | The technology that should be explained. |
| `{audience}`    | The intended audience.                   |
| `{detailLevel}` | The requested level of detail.           |
| `{question}`    | The specific question or instruction.    |

These variables allow the same template to generate different prompts.

For example, the application could receive the following values:

```text
technology = Spring Security
audience = a beginner Java developer
detailLevel = detailed
question = Explain how authentication works.
```

The rendered prompt becomes:

```text
You are a knowledgeable software development tutor.

Explain Spring Security to a beginner Java developer.

The explanation should have a detailed level of detail.

Address the following request:
Explain how authentication works.

Use clear language and practical examples where appropriate.
Organize the response with relevant headings and bullet points.
```

Another request could use:

```text
technology = Spring Data JPA
audience = an experienced Java developer
detailLevel = concise
question = Explain repository interfaces.
```

The same template is reused, but the rendered prompt changes according to the supplied values.

---

## Supplying Variables at Runtime

The service collects the request values into a map:

```java
Map<String, Object> variables = Map.of(
        "technology", technology,
        "audience", audience,
        "detailLevel", detailLevel,
        "question", question
);
```

The map is passed to the template:

```java
Prompt prompt = promptTemplate.create(variables);
```

Spring AI resolves the placeholders and produces a `Prompt` containing the rendered instructions.

This separates the reusable prompt structure from the values that change between requests.

It also makes the prompt generation logic easier to test because the template and its variables can be inspected independently of the actual AI provider.

---

## Sending the Rendered Prompt to a Chat Model

After rendering the template, the service sends the resulting `Prompt` to the configured `ChatModel`.

```java
ChatResponse response = chatModel.call(prompt);
```

The `ChatModel` abstraction provides a provider-independent way to interact with supported chat models.

The application does not need to construct an Ollama-specific request directly in the service.

Spring AI handles the integration with the configured model provider.

The response is extracted from the returned `ChatResponse`:

```java
String generatedResponse = response.getResult().getOutput().getText();
```

The response flow is:

```text
Prompt Template
       │
       ▼
Runtime Variables
       │
       ▼
Rendered Prompt
       │
       ▼
ChatModel
       │
       ▼
ChatResponse
       │
       ▼
Generated Text
```

---

## Understanding the Main Spring AI Types

This example uses several important Spring AI abstractions.

### `PromptTemplate`

Represents a reusable prompt template.

It accepts a template containing placeholders and creates a prompt after receiving variable values.

### `Prompt`

Represents the prompt supplied to a chat model.

In this example, it contains the instructions rendered from the external template.

### `ChatModel`

Provides the abstraction for interacting with a chat model.

The service uses:

```java
chatModel.call(prompt);
```

This keeps the service implementation independent of the specific chat model provider.

### `ChatResponse`

Represents the response returned by the chat model.

It contains the generated result.

### `Generation`

Represents a generated response from the model.

The service accesses the generation through:

```java
response.getResult();
```

### `AssistantMessage`

Represents the assistant's generated message.

The generated text is obtained through:

```java
response.getResult().getOutput().getText();
```

These abstractions separate prompt construction, model interaction, and generated response handling.

---

## Prompt Templates vs Hardcoded Prompts

Both approaches can be used to construct prompts, but they serve different maintenance needs.

### Hardcoded Prompt

```java
String prompt = "Explain " + technology + " to " + audience;
```

The prompt is constructed directly in Java.

This is straightforward for very small prompts but can become difficult to maintain as instructions grow.

### Prompt Template

```java
PromptTemplate promptTemplate = new PromptTemplate("Explain {technology} to {audience}.");

Prompt prompt = promptTemplate.create(Map.of("technology", technology, "audience", audience));
```

The prompt structure is reusable, while the values are supplied separately.

### External Prompt Template

This example takes the separation further by storing the template in a resource file.

```text
Java Service
     │
     ▼
Classpath Resource
     │
     ▼
Prompt Template
     │
     ▼
Runtime Variables
     │
     ▼
Rendered Prompt
```

This approach is useful when prompts contain detailed instructions or need to be maintained independently of application code.

---

## Application Architecture

The example follows a simple layered structure:

```text
HTTP Client
     │
     ▼
PromptTemplateController
     │
     ▼
PromptTemplateService
     │
     ├── Load Prompt Template
     │
     ├── Resolve Variables
     │
     ├── Create Prompt
     │
     ▼
ChatModel
     │
     ▼
Ollama
     │
     ▼
Generated Response
     │
     ▼
HTTP Response
```

Each component has a distinct responsibility.

### Controller

The controller exposes the REST endpoint and receives the prompt template request.

It delegates prompt generation to the service.

### Service

The service:

- Loads the prompt template.
- Supplies the runtime variables.
- Creates the rendered prompt.
- Calls the chat model.
- Extracts the generated response.

### DTOs

The request and response records define the API contract.

### Exception Handler

The exception handler translates chat model failures into an appropriate HTTP response.

---

## REST API

The example exposes:

```http
POST /api/prompt-templates
Content-Type: application/json
```

Example request:

```json
{
  "technology": "Spring Security",
  "audience": "a beginner Java developer",
  "detailLevel": "detailed",
  "question": "Explain how authentication works."
}
```

Example response:

```json
{
  "response": "Spring Security is a framework that provides authentication and authorization..."
}
```

The actual response is generated by the configured chat model and will vary between requests.

---

## Request Validation

The request DTO uses Jakarta Bean Validation annotations to ensure that the required template variables are provided.

For example:

```java
public record PromptTemplateRequest(@NotBlank String technology, @NotBlank String audience, @NotBlank String detailLevel,
        @NotBlank String question) {
}
```

The `@NotBlank` annotation rejects null, empty, and whitespace-only values.

The controller uses `@Valid` to trigger validation:

```java
@PostMapping
public PromptTemplateResponse generate(@Valid @RequestBody PromptTemplateRequest request) {
    // Delegate to the service.
}
```

This prevents incomplete requests from reaching the prompt generation service.

---

## Error Handling

The example includes a dedicated exception handler for failures raised during prompt generation.

For example, the chat model might be unavailable or fail to return a usable response.

The service checks that the returned response contains the expected generation and assistant output.

If the response is incomplete, it throws an `IllegalStateException`.

The exception handler maps this failure to HTTP `503 Service Unavailable`.

This communicates that the application could not currently complete the AI operation.

It also keeps error handling separate from the controller and service implementation.

---

## Configuring Ollama

This example uses Ollama as the chat model provider.

The configuration is defined in `application.yml`:

```yaml
spring:
  application:
    name: prompt-templates
  ai:
    model:
      chat: ollama
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.2
        temperature: 0.7

server:
  port: 8082
```

### Ollama Base URL

```yaml
base-url: http://localhost:11434
```

This specifies the address where the Ollama service is running.

### Chat Model

```yaml
model: llama3.2
```

This selects the Llama 3.2 model.

### Temperature

```yaml
temperature: 0.7
```

Temperature controls the randomness of generated responses.

Lower values generally produce more predictable responses, while higher values allow more variation.

The temperature is configured for the chat model rather than being a prompt template variable.

---

## Testing Prompt Templates

The example uses Spring Boot's testing support, JUnit, Mockito, and MockMvc.

The tests focus on three areas:

- Prompt generation and rendering.
- REST API behavior and validation.
- Application context initialization.

### Testing the Service

The service tests mock `ChatModel` so that prompt generation can be tested without requiring a running Ollama instance.

The tests verify:

- A generated response is returned successfully.
- All template variables are rendered into the prompt.
- An empty assistant response is handled.
- A null chat response is rejected.
- A missing generation is rejected.
- A missing assistant output is rejected.
- Chat model exceptions are propagated.

One important test captures the prompt passed to the chat model:

```java
ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);

verify(chatModel).call(promptCaptor.capture());
```

The captured prompt can then be inspected to verify that the template variables were resolved correctly.

This is important because the service should send the rendered prompt to the model, not the original template containing unresolved placeholders.

### Testing the Controller

The controller tests use MockMvc to exercise the REST endpoint.

They verify:

- A valid request returns a successful response.
- All request fields are passed to the service.
- Blank values are rejected.
- Missing fields are rejected.
- Null fields are rejected.
- Malformed JSON is rejected.
- Chat model failures produce HTTP `503 Service Unavailable`.

The tests also verify that invalid requests do not reach the service.

### Testing Application Context Initialization

The application context test uses:

```java
@SpringBootTest
```

This verifies that the Spring application context can start successfully.

It also helps verify that the service dependencies and classpath prompt resource can be initialized through the application's Spring configuration.

---

## Important Testing Detail

The service tests mock the `ChatModel` rather than connecting to Ollama.

This is intentional.

The purpose of these tests is to verify the application's prompt construction and integration logic, not to verify the quality of an AI-generated answer.

The distinction is:

```text
Unit and Web Layer Tests
          │
          ▼
Mocked ChatModel
          │
          ▼
Verify Application Behavior
```

An actual Ollama interaction requires a running Ollama service and an available model.

That kind of provider integration test can be added separately when testing real model communication is necessary.

---

## Prompt Template Execution Flow

A simplified view of the complete example is:

```text
HTTP POST Request
        │
        ▼
PromptTemplateController
        │
        ▼
Request Validation
        │
        ▼
PromptTemplateService
        │
        ▼
Load External Template
        │
        ▼
Collect Runtime Variables
        │
        ▼
PromptTemplate.create(...)
        │
        ▼
Rendered Prompt
        │
        ▼
ChatModel.call(...)
        │
        ▼
ChatResponse
        │
        ▼
Extract Assistant Text
        │
        ▼
PromptTemplateResponse
        │
        ▼
HTTP Response
```

---

## Why Separate Prompt Content from Application Logic?

Separating prompt content from Java code improves maintainability.

For example, a developer may want to change the instructions from:

```text
Use clear language and practical examples where appropriate.
```

to:

```text
Use clear language, practical examples, and a short code example.
```

With an external template, this change can be made in the resource file without changing the service implementation.

However, changing a prompt can still change application behavior, so prompt modifications should be reviewed and tested like other application changes.

For larger AI applications, prompt versioning, evaluation, and experimentation may also become important.

---

## Running the Example

### Prerequisites

- Java 21.
- Maven.
- Ollama installed and running.
- The Llama 3.2 model available locally.

Pull the model if it is not already available:

```bash
ollama pull llama3.2
```

Start Ollama if it is not already running:

```bash
ollama serve
```

From the repository root, start the application:

```bash
mvn -pl 10-spring-ai/prompt-templates spring-boot:run
```

The application starts on:

```text
http://localhost:8082
```

### Try the Endpoint

```bash
curl -X POST http://localhost:8082/api/prompt-templates \
  -H "Content-Type: application/json" \
  -d '{
    "technology": "Spring Security",
    "audience": "a beginner Java developer",
    "detailLevel": "detailed",
    "question": "Explain how authentication works."
  }'
```

The application renders the external prompt template, sends it to Ollama, and returns the generated response.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 10-spring-ai/prompt-templates test
```

Run the complete verification lifecycle:

```bash
mvn -pl 10-spring-ai/prompt-templates clean verify
```

---

## Key Takeaways

- Prompt templates provide reusable structures for AI prompts.
- Spring AI provides `PromptTemplate` for rendering prompts with dynamic variables.
- Placeholders such as `{technology}` and `{audience}` are resolved using runtime values.
- `PromptTemplate.create(...)` creates a `Prompt` from the template and supplied variables.
- External `.st` resources help separate prompt content from Java application logic.
- The rendered prompt can be sent to a `ChatModel` using `chatModel.call(prompt)`.
- `ChatResponse` contains the generated response.
- `ChatModel` provides an abstraction over the underlying chat model provider.
- Request validation prevents incomplete prompt variables from reaching the service.
- Mocking `ChatModel` makes prompt generation tests independent of an actual AI provider.
- Prompt content should be maintained and tested because changes to instructions can affect generated responses.

---

## Next Example

The next example will explore **Structured Output** in Spring AI, including how to guide AI models to return structured data and map generated responses to Java objects.
