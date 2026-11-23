# Structured Output

This example demonstrates **structured output generation** using Spring AI.

It focuses on how to convert an AI model's natural language response into a structured Java object, how `BeanOutputConverter` uses a Java type to define the expected output format, and how Bean Validation ensures that the generated data satisfies application-level requirements.

The example uses an Ollama chat model to generate technology recommendations and return them as a `TechnologyRecommendation` record.

## What This Example Demonstrates

- What structured output means in Spring AI.
- The difference between unstructured and structured AI responses.
- Using `BeanOutputConverter` to convert model responses into Java objects.
- Defining structured output using Java records.
- Generating output format instructions using `BeanOutputConverter`.
- Integrating output format instructions into a `PromptTemplate`.
- Supplying variables to a prompt template.
- Calling a chat model using `ChatModel`.
- Converting JSON responses into Java objects.
- Validating AI-generated objects using Jakarta Bean Validation.
- Handling empty, malformed, and invalid structured responses.
- Testing structured output generation with Mockito and JUnit.

---

## What Is Structured Output?

Structured output is the process of instructing an AI model to return information in a predefined format rather than unrestricted natural language.

A typical chat model response might look like this:

```text
Spring Boot is a Java framework that simplifies the development
of production-ready Spring applications. It provides auto-configuration,
embedded servers, and dependency management.
```

Although this response contains useful information, an application cannot reliably access individual pieces of information without additional parsing.

Structured output addresses this problem by defining an expected response format.

For example:

```json
{
  "name": "Spring Boot",
  "category": "Java Application Framework",
  "description": "A framework for building production-ready Spring applications.",
  "keyFeatures": [
    "Auto-configuration",
    "Embedded servers",
    "Dependency management"
  ],
  "advantages": [
    "Faster application development",
    "Reduced configuration"
  ],
  "limitations": [
    "Can introduce unnecessary dependencies",
    "Requires understanding of Spring conventions"
  ],
  "suitableUseCases": [
    "REST APIs",
    "Microservices",
    "Enterprise applications"
  ]
}
```

The response now follows a predictable structure that can be converted into a Java object.

---

## Structured Output in Spring AI

Spring AI provides mechanisms for working with structured model responses.

One of these is `BeanOutputConverter`.

`BeanOutputConverter` uses a Java class or record to determine the expected output structure and generates format instructions that can be included in a prompt.

The converter is responsible for converting the model's response into an instance of the specified Java type.

The general flow is:

```text
Java Record
    │
    ▼
BeanOutputConverter
    │
    ▼
Output Format Instructions
    │
    ▼
Prompt Template
    │
    ▼
Chat Model
    │
    ▼
Structured JSON Response
    │
    ▼
BeanOutputConverter
    │
    ▼
Java Object
```

This allows application code to work with strongly typed objects instead of manually parsing arbitrary text responses.

---

## The TechnologyRecommendation Record

The example defines a Java record representing the expected structure of a technology recommendation.

```java
public record TechnologyRecommendation(
        @NotBlank String name,
        @NotBlank String category,
        @NotBlank String description,
        @NotEmpty List<@NotBlank String> keyFeatures,
        @NotEmpty List<@NotBlank String> advantages,
        @NotEmpty List<@NotBlank String> limitations,
        @NotEmpty List<@NotBlank String> suitableUseCases
) {
}
```

The record contains the following fields:

| Field | Description |
|---|---|
| `name` | Name of the recommended technology. |
| `category` | Category to which the technology belongs. |
| `description` | Description of the technology. |
| `keyFeatures` | Main features of the technology. |
| `advantages` | Benefits of using the technology. |
| `limitations` | Potential disadvantages or limitations. |
| `suitableUseCases` | Situations where the technology is appropriate. |

The record serves two purposes:

1. It defines the expected structure of the AI-generated response.
2. It provides a strongly typed object that the application can use.

The validation annotations also define application-level requirements for the generated data.

---

## BeanOutputConverter

`BeanOutputConverter` is a Spring AI component that converts model output into a specified Java type.

It can also generate format instructions describing the expected response structure.

The example creates a converter using the `TechnologyRecommendation` record:

```java
BeanOutputConverter<TechnologyRecommendation> outputConverter =
        new BeanOutputConverter<>(TechnologyRecommendation.class);
```

The generic type indicates the Java object that the converter should produce.

### Generating Format Instructions

The converter provides a `getFormat()` method:

```java
String format = outputConverter.getFormat();
```

These instructions describe the expected output format to the AI model.

For example, the instructions communicate that the response should follow the JSON structure corresponding to `TechnologyRecommendation`.

This is important because the model needs to know the expected structure before generating its response.

### Converting the Response

After receiving the model's response, the converter transforms it into a Java object:

```java
TechnologyRecommendation recommendation =
        outputConverter.convert(generatedContent);
```

The application can then access individual fields:

```java
recommendation.name();
recommendation.category();
recommendation.description();
recommendation.keyFeatures();
```

This removes the need for application code to manually parse the JSON response.

---

## Prompt Templates and Structured Output

The example uses a `PromptTemplate` to construct the prompt sent to the AI model.

The prompt template is stored externally in:

```text
src/main/resources/prompts/technology-recommendation.st
```

Its contents are:

```text
You are an experienced software architecture advisor.

Recommend a technology based on the following information:

Technology: {technology}
Intended use case: {useCase}
Audience: {audience}

Provide a practical recommendation. Explain what the technology is,
its category, its main features, its advantages, its limitations,
and the situations where it is suitable.

Return only the structured response that matches the required output format.

{format}
```

The template contains four variables:

| Variable | Purpose |
|---|---|
| `technology` | Technology for which a recommendation is requested. |
| `useCase` | Intended use case. |
| `audience` | Intended audience for the recommendation. |
| `format` | Output format instructions generated by `BeanOutputConverter`. |

The `{format}` placeholder is particularly important.

It allows the application to include the expected JSON structure in the prompt without manually maintaining a separate JSON schema.

---

## Supplying Prompt Variables

The service creates a map containing the prompt variables:

```java
Map<String, Object> variables = Map.of(
        "technology", technology,
        "useCase", useCase,
        "audience", audience,
        "format", outputConverter.getFormat()
);
```

The `PromptTemplate` uses these variables to construct the final prompt:

```java
PromptTemplate promptTemplate =
        new PromptTemplate(promptTemplateContent);

Prompt prompt = promptTemplate.create(variables);
```

The resulting prompt contains:

- The technology being evaluated.
- The intended use case.
- The target audience.
- The structured output instructions.

This separates prompt content from application logic and makes the prompt easier to maintain.

---

## Calling the Chat Model

Once the prompt has been constructed, the service sends it to the configured chat model.

```java
ChatResponse response = chatModel.call(prompt);
```

The `ChatModel` abstraction allows the application to interact with the configured AI model without directly depending on provider-specific implementation details.

The response contains the model's generated content.

The example extracts the generated text:

```java
String generatedContent =
        response.getResult().getOutput().getText();
```

This content is then passed to `BeanOutputConverter` for conversion into the expected Java type.

---

## Converting AI Responses into Java Objects

The conversion process is performed using:

```java
TechnologyRecommendation recommendation =
        outputConverter.convert(generatedContent);
```

The converter maps the generated JSON properties to the corresponding record components.

Conceptually:

```text
AI Model Response
       │
       ▼
Generated JSON
       │
       ▼
BeanOutputConverter
       │
       ▼
TechnologyRecommendation
       │
       ▼
Application Code
```

The application can now work with the generated recommendation as a normal Java object.

This is particularly useful when AI-generated information needs to be returned through REST APIs, processed by business logic, or persisted for later use.

---

## Validating AI-Generated Structured Output

Successful JSON conversion does not necessarily mean that the generated object contains valid application data.

For example, the AI model might return:

```json
{
  "name": "Spring Boot",
  "category": "Java Application Framework"
}
```

The JSON may be convertible into the Java record even though several required properties are missing.

Jackson can assign `null` to missing reference-type record components.

Therefore, JSON-to-object conversion alone does not guarantee that all required fields are populated.

The example addresses this using Jakarta Bean Validation.

### Validation Constraints

The `TechnologyRecommendation` record declares constraints:

```java
@NotBlank String name
```

Ensures that the name is not null, empty, or whitespace-only.

```java
@NotEmpty List<@NotBlank String> keyFeatures
```

Ensures that the list is not null or empty and that its elements are not blank.

The same constraints are applied to the other required fields.

### Validating the Converted Object

The service injects Jakarta's `Validator`:

```java
private final Validator validator;
```

After converting the generated JSON, it validates the resulting object:

```java
Set<ConstraintViolation<TechnologyRecommendation>> violations =
        validator.validate(recommendation);

if (!violations.isEmpty()) {
    throw new IllegalStateException(
            "The AI model returned an incomplete or invalid structured response."
    );
}
```

If validation fails, the service rejects the generated recommendation.

This establishes an important distinction:

- `BeanOutputConverter` handles JSON-to-object conversion.
- Bean Validation checks whether the resulting object satisfies application requirements.

Both are important when working with AI-generated structured data.

---

## Handling Invalid Model Responses

AI-generated responses cannot always be assumed to be valid.

The model might return an empty response, malformed JSON, or data that does not satisfy the application's requirements.

The service therefore handles several failure scenarios.

### Empty Responses

The service checks whether the response, generation, output, or generated text is missing or blank.

```java
if (response == null
        || response.getResult() == null
        || response.getResult().getOutput() == null
        || response.getResult().getOutput().getText() == null
        || response.getResult().getOutput().getText().isBlank()) {
    throw new IllegalStateException(
            "The AI model returned an empty response."
    );
}
```

This prevents the converter from processing missing or empty content.

### Invalid JSON

The converter can fail when the generated content cannot be converted into the expected Java type.

The example handles conversion failures:

```java
try {
    recommendation = outputConverter.convert(generatedContent);
} catch (RuntimeException exception) {
    throw new IllegalStateException(
            "The AI model returned an invalid structured response.",
            exception
    );
}
```

The original exception is retained as the cause.

### Invalid Application Data

Even when conversion succeeds, the generated object might violate the validation constraints.

The service validates the converted object and rejects invalid results.

### Chat Model Failures

Exceptions raised by the chat model are allowed to propagate.

This keeps model invocation failures separate from JSON conversion and application-level validation failures.

---

## REST API

The example exposes a REST endpoint:

```text
POST /api/structured-output
```

It accepts a technology recommendation request.

### Request DTO

```java
public record TechnologyRequest(
        @NotBlank String technology,
        @NotBlank String useCase,
        @NotBlank String audience
) {
}
```

The request contains:

- `technology`: The technology to evaluate.
- `useCase`: The intended purpose.
- `audience`: The intended audience.

### Example Request

```json
{
  "technology": "Spring Boot",
  "useCase": "Building REST APIs and microservices",
  "audience": "Java developers"
}
```

### Example Response

```json
{
  "name": "Spring Boot",
  "category": "Java Application Framework",
  "description": "Spring Boot simplifies the development of production-ready Spring applications.",
  "keyFeatures": [
    "Auto-configuration",
    "Embedded servers",
    "Spring Boot starters"
  ],
  "advantages": [
    "Faster application development",
    "Reduced manual configuration"
  ],
  "limitations": [
    "Can introduce additional dependencies",
    "Requires familiarity with the Spring ecosystem"
  ],
  "suitableUseCases": [
    "REST APIs",
    "Microservices",
    "Enterprise applications"
  ]
}
```

The response is serialized from the `TechnologyRecommendation` Java record.

The API therefore returns a structured response rather than exposing the model's raw text.

---

## Exception Handling

The example uses a `@RestControllerAdvice` to handle structured output generation failures.

An `IllegalStateException` is mapped to HTTP `502 Bad Gateway`.

This represents a failure to obtain or process a usable response from the upstream AI model.

The response uses Spring's `ProblemDetail`:

```java
ProblemDetail problemDetail =
        ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);

problemDetail.setTitle("Structured Output Generation Failed");

problemDetail.setDetail(
        "The AI model returned a response that could not be processed."
);
```

This provides a consistent HTTP error response without exposing internal implementation details to API consumers.

---

## Application Configuration

The example uses Ollama as its chat model provider.

The relevant configuration is:

```yaml
spring:
  application:
    name: structured-output
  ai:
    model:
      chat: ollama
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.2
        temperature: 0.2

server:
  port: 8083
```

### Ollama

[Ollama](https://ollama.com/) allows locally hosted language models to be used without depending on a remote model API.

The example uses `llama3.2`.

Before running the application, ensure Ollama is installed and running.

Pull the model if it is not already available:

```bash
ollama pull llama3.2
```

### Temperature

The example uses a temperature of `0.2`.

A relatively low temperature encourages more consistent responses.

However, temperature does not guarantee valid JSON or compliance with the expected schema. The application still needs conversion and validation.

---

## Structured Output Flow

The complete application flow is:

```text
HTTP POST /api/structured-output
              │
              ▼
       TechnologyRequest
              │
              ▼
       Request Validation
              │
              ▼
   StructuredOutputService
              │
              ▼
      BeanOutputConverter
              │
              ▼
     Generate Format Instructions
              │
              ▼
         PromptTemplate
              │
              ▼
          ChatModel
              │
              ▼
       Generated JSON
              │
              ▼
      Response Validation
              │
              ▼
  TechnologyRecommendation
              │
              ▼
       HTTP JSON Response
```

This demonstrates how Spring AI structured output fits into a typical Spring Boot REST application.

---

## Testing Structured Output

The example uses JUnit 5, Mockito, Spring Boot Test, and MockMvc.

The tests cover both the service and REST controller.

### Service Tests

The service tests use a mocked `ChatModel` to avoid requiring a running Ollama instance for unit testing.

They verify the following scenarios:

| Test | Expected behavior |
|---|---|
| Valid model response | Converts JSON into a `TechnologyRecommendation`. |
| Prompt variable substitution | Includes the technology, use case, audience, and output format instructions in the prompt. |
| Null chat response | Rejects the response. |
| Missing generation | Rejects the response. |
| Missing model output | Rejects the response. |
| Blank model response | Rejects the response. |
| Invalid JSON | Rejects the response. |
| Incomplete JSON | Rejects the converted object through Bean Validation. |
| Invalid generated fields | Rejects blank or empty required values. |
| Chat model failure | Propagates the original model exception. |

### Testing Successful Conversion

The test supplies a valid JSON response through the mocked chat model.

The service converts it into a `TechnologyRecommendation`.

Assertions verify the generated fields, including the lists of features, advantages, limitations, and suitable use cases.

### Testing Prompt Variables

The test captures the prompt passed to the chat model:

```java
ArgumentCaptor<Prompt> promptCaptor =
        ArgumentCaptor.forClass(Prompt.class);

verify(chatModel).call(promptCaptor.capture());
```

It then verifies that the expected variables and output format instructions were included.

This ensures that the prompt contains enough information to guide structured response generation.

### Testing Invalid Structured Output

The test supplies incomplete JSON:

```json
{
  "name": "Spring Boot",
  "category": "Java Application Framework"
}
```

The converter may successfully create an object with null values for missing fields.

The test therefore verifies that application-level validation rejects the incomplete object.

### Controller Tests

The controller tests use MockMvc to verify:

- Successful recommendation requests.
- Blank request fields.
- Missing request fields.
- Null request fields.
- Malformed JSON.
- Service failures.

These tests verify both request validation and HTTP response behavior.

### Application Context Test

The application context test verifies that the Spring Boot application context can start successfully.

It checks that the application components and configuration can be loaded.

---

## Important Testing Details

### Mocking the Chat Model

The service tests mock `ChatModel` rather than calling Ollama.

This makes the tests:

- Independent of an external model provider.
- Faster to execute.
- Deterministic.
- Suitable for testing specific response and failure scenarios.

The tests focus on the application's handling of model responses rather than the quality of the model's generated recommendations.

### Using a Real Validator

The service constructor requires a Jakarta `Validator`.

The tests create a real validator using:

```java
validatorFactory = Validation.buildDefaultValidatorFactory();

Validator validator = validatorFactory.getValidator();
```

The validator factory is kept open for the duration of the tests and closed in an `@AfterEach` method.

This ensures that the tests exercise actual Bean Validation constraints rather than mocking validation behavior.

### Structured Output Is Not Guaranteed by the Model

Providing format instructions does not guarantee that an AI model will always return valid structured data.

The application must still account for:

- Malformed JSON.
- Missing properties.
- Invalid property values.
- Empty responses.
- Model invocation failures.

This is why conversion and validation are separate responsibilities in the example.

---

## Running the Example

From the repository root:

```bash
mvn -pl 10-spring-ai/structured-output spring-boot:run
```

Ensure Ollama is running and the `llama3.2` model is available.

The application starts on:

```text
http://localhost:8083
```

### Sending a Request

Using `curl`:

```bash
curl -X POST http://localhost:8083/api/structured-output \
  -H "Content-Type: application/json" \
  -d '{
    "technology": "Spring Boot",
    "useCase": "Building REST APIs and microservices",
    "audience": "Java developers"
  }'
```

The application sends the request details to the AI model and returns the generated recommendation as JSON.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 10-spring-ai/structured-output test
```

The service unit tests do not require Ollama because the chat model is mocked.

If the application context test requires model-provider initialization in your environment, configure an appropriate test setup that avoids making an actual model connection.

---

## Key Takeaways

- Structured output allows AI models to return information in predefined formats.
- `BeanOutputConverter` converts model responses into strongly typed Java objects.
- Java records provide a convenient way to define structured response models.
- `BeanOutputConverter.getFormat()` generates output format instructions.
- Format instructions can be included in a `PromptTemplate`.
- `ChatModel` provides the abstraction for interacting with the configured chat model.
- JSON conversion and application-level validation are separate responsibilities.
- Jakarta Bean Validation ensures that required generated fields contain acceptable values.
- AI-generated responses should never be assumed to be valid simply because the model was given formatting instructions.
- Mocking `ChatModel` allows structured output behavior to be tested without running an actual AI model.
- Structured output makes AI-generated information easier to consume in ordinary application code and REST APIs.

---

## Next Example

The next example will explore **Embeddings** in Spring AI, including what embeddings are, how text is converted into numerical vector representations, and how embeddings support semantic similarity and retrieval-based applications.
