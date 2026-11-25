# Model Provider Integration

This example demonstrates **model provider integration** in Spring AI.

It focuses on how Spring AI integrates with different AI model providers, how provider-specific configuration is managed, and how Spring profiles can be used to switch between providers without changing application code.

The example supports two chat model providers:

- **Ollama** — runs a locally hosted language model.
- **OpenAI** — uses OpenAI's hosted language models through its API.

Both providers expose the same Spring AI `ChatClient` programming model. This allows the application to switch providers through configuration rather than introducing provider-specific application logic.

## What This Example Demonstrates

- Integrating multiple AI model providers with Spring AI.
- Using the Ollama chat model.
- Using the OpenAI chat model.
- Configuring provider-specific properties.
- Using Spring profiles to switch between model providers.
- Managing API keys through environment variables.
- Selecting the active chat model using `spring.ai.model.chat`.
- Disabling unused model auto-configurations.
- Building provider-independent application services with `ChatClient`.
- Testing provider configuration without making actual AI API calls.
- Verifying that the expected chat model is selected for each profile.

---

## What Is Model Provider Integration?

Model provider integration is the process of connecting an application to an AI model through a provider that hosts or exposes the model.

Different providers offer different models, infrastructure, pricing, authentication mechanisms, and capabilities.

For example:

- Ollama allows developers to run supported models locally.
- OpenAI provides hosted models accessible through an API.

Although their configuration and connection mechanisms differ, Spring AI provides a common abstraction for interacting with supported providers.

This means application code can focus on communicating with the AI model rather than managing provider-specific HTTP requests or SDK implementations.

---

## Why Spring AI Supports Multiple Providers

Without a common abstraction, an application integrating multiple providers might need separate implementations for each provider.

Conceptually:

```text
Application
    |
    +---- Ollama-specific implementation
    |
    +---- OpenAI-specific implementation
    |
    +---- Another provider-specific implementation
```

This introduces additional complexity when switching providers.

Spring AI provides common abstractions such as `ChatModel` and `ChatClient`.

The application can therefore use the same interaction pattern regardless of the selected provider.

```text
                Application
                     |
                     v
                  ChatClient
                     |
                     v
                  ChatModel
                     |
             +-------+-------+
             |               |
             v               v
         Ollama           OpenAI
             |               |
             v               v
        Local Model      Hosted Model
```

The provider-specific configuration determines which implementation Spring AI uses.

---

## ChatModel

`ChatModel` is a Spring AI abstraction representing a chat model.

It provides a common interface for interacting with different chat model implementations.

Spring AI provides provider-specific implementations, including:

- `OllamaChatModel`
- `OpenAiChatModel`

These implementations communicate with their respective providers.

The application does not need to instantiate either implementation directly.

Instead, Spring Boot auto-configuration creates the appropriate model based on the configured provider and available dependencies.

Conceptually:

```text
spring.ai.model.chat
          |
          v
    Provider Selection
          |
     +----+----+
     |         |
     v         v
  Ollama     OpenAI
     |         |
     v         v
OllamaChatModel
           OpenAiChatModel
```

Only the selected chat model should be active for a given profile.

---

## ChatClient

The example uses Spring AI's `ChatClient` to communicate with the selected chat model.

`ChatClient` provides a fluent API for constructing prompts and retrieving model responses.

The service builds the client using `ChatClient.Builder`:

```java
public ChatAssistantService(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
}
```

The application then sends a question to the model:

```java
String answer = chatClient.prompt().system(SYSTEM_PROMPT).user(question).call().content();
```

The interaction consists of:

1. Creating a prompt.
2. Providing system instructions.
3. Providing the user's question.
4. Calling the selected chat model.
5. Extracting the response content.

The important detail is that the service does not need to know whether the underlying model is Ollama or OpenAI.

The same service implementation works with either provider.

---

## Providers Used in This Example

### Ollama

<text></text>

Ollama allows developers to run supported language models locally.

This example uses the `llama3.2` model.

The application communicates with the Ollama server running locally.

The default Ollama URL is:

```text
http://localhost:11434
```

The model can be downloaded using:

```bash
ollama pull llama3.2
```

The Ollama provider is useful for:

- Local development.
- Experimenting with language models.
- Avoiding dependence on a hosted model API during development.
- Running supported models without sending prompts to a hosted AI provider.

Ollama must be installed and running before making actual requests through this provider.

### OpenAI

OpenAI provides hosted language models accessible through its API.

This example uses:

```text
gpt-4o-mini
```

The application authenticates using an API key.

The API key is supplied through an environment variable:

```bash
OPENAI_API_KEY=your-api-key
```

The example does not hardcode the API key in the application configuration.

This is important because API credentials should be managed outside source code and should not be committed to version control.

---

## Provider Dependencies

The module includes both provider starters in its `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-ollama</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

Including both dependencies makes both provider integrations available to the application.

However, having multiple provider starters on the classpath does not mean that the application should activate every available model implementation.

Provider selection must be configured explicitly.

---

## Provider Selection

Spring AI provides model selection properties that determine which provider should be used for a particular model type.

For chat models, the relevant property is:

```yaml
spring:
  ai:
    model:
      chat: ollama
```

To select OpenAI instead:

```yaml
spring:
  ai:
    model:
      chat: openai
```

The example uses Spring profiles to manage these settings.

This keeps provider selection outside the application service implementation.

---

## Spring Profiles

Spring profiles allow different configuration settings to be activated for different environments or execution modes.

This example uses three configuration files:

```text
src/main/resources/
    application.yml
    application-ollama.yml
    application-openai.yml
```

### Default Configuration

The main `application.yml` file contains common application settings and disables unused model types:

```yaml
spring:
  application:
    name: model-provider-integration
  profiles:
    active: ${AI_PROVIDER:ollama}
  ai:
    model:
      embedding: none
      image: none
      moderation: none
      audio:
        speech: none
        transcription: none

server:
  port: 8089
```

The active profile is determined by the `AI_PROVIDER` environment variable.

If the environment variable is not provided, the application uses the `ollama` profile.

For example:

```text
AI_PROVIDER=ollama
```

or:

```text
AI_PROVIDER=openai
```

The application also disables unused embedding, image, moderation, and audio model auto-configurations.

This is important because both provider starters are available on the classpath. Without disabling unused model types, Spring AI may attempt to configure provider components that the example does not use.

For example, an OpenAI model component may require an API key even when Ollama is selected as the chat provider.

Disabling unused model types avoids unnecessary auto-configuration and credential requirements.

### Ollama Configuration

The `application-ollama.yml` file contains Ollama-specific settings:

```yaml
spring:
  ai:
    model:
      chat: ollama
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.2
        options:
          temperature: 0.2
```

The configuration specifies:

| Property                                    | Purpose                              |
|---------------------------------------------|--------------------------------------|
| `spring.ai.model.chat`                      | Selects Ollama as the chat provider. |
| `spring.ai.ollama.base-url`                 | Specifies the Ollama server URL.     |
| `spring.ai.ollama.chat.model`               | Selects the language model.          |
| `spring.ai.ollama.chat.options.temperature` | Controls response randomness.        |

### OpenAI Configuration

The `application-openai.yml` file contains OpenAI-specific settings:

```yaml
spring:
  ai:
    model:
      chat: openai
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        model: gpt-4o-mini
        options:
          temperature: 0.2
```

The configuration specifies:

| Property                                    | Purpose                                         |
|---------------------------------------------|-------------------------------------------------|
| `spring.ai.model.chat`                      | Selects OpenAI as the chat provider.            |
| `spring.ai.openai.api-key`                  | Reads the API key from an environment variable. |
| `spring.ai.openai.chat.model`               | Selects the OpenAI chat model.                  |
| `spring.ai.openai.chat.options.temperature` | Controls response randomness.                   |

The API key is not stored directly in the configuration file.

---

## Switching Between Providers

The main purpose of this example is to demonstrate that the application can switch providers without changing its Java implementation.

### Using Ollama

Ollama is the default provider.

Run the application:

```bash
mvn -pl 10-spring-ai/model-provider-integration spring-boot:run
```

The application activates the `ollama` profile.

### Using OpenAI

Set the provider and API key before starting the application:

```bash
AI_PROVIDER=openai OPENAI_API_KEY=your-api-key \
mvn -pl 10-spring-ai/model-provider-integration spring-boot:run
```

The application activates the `openai` profile.

### Provider Switching Flow

```text
Application Startup
        |
        v
Read AI_PROVIDER
        |
        v
Select Spring Profile
        |
        +-------------------+
        |                   |
        v                   v
     ollama               openai
        |                   |
        v                   v
Ollama Configuration  OpenAI Configuration
        |                   |
        v                   v
OllamaChatModel       OpenAiChatModel
        |                   |
        +---------+---------+
                  |
                  v
              ChatClient
                  |
                  v
          ChatAssistantService
                  |
                  v
             AI Response
```

The service and controller remain unchanged when switching providers.

---

## Application Architecture

The example follows a simple layered architecture:

```text
HTTP Client
    |
    v
ChatAssistantController
    |
    v
ChatAssistantService
    |
    v
ChatClient
    |
    v
Selected ChatModel
    |
    +-------------------+
    |                   |
    v                   v
  Ollama              OpenAI
    |                   |
    v                   v
 Local Model        Hosted Model
```

### ChatAssistantController

The controller exposes the HTTP endpoint:

```text
POST /api/model-provider/chat
```

It accepts a question and delegates processing to `ChatAssistantService`.

### ChatAssistantService

The service contains the application logic for interacting with the AI model.

It:

- Builds a `ChatClient`.
- Supplies system instructions.
- Sends the user's question.
- Retrieves the model response.
- Checks for an empty response.
- Returns the application response DTO.

The service is provider-independent.

### ChatRequest

The request DTO represents the question submitted by the client.

It uses Jakarta Bean Validation to ensure that the question is not blank.

### ChatResponse

The response DTO contains the original question and the generated answer.

### ChatAssistantExceptionHandler

The exception handler provides consistent error responses.

If the AI model returns an empty response or the service cannot complete the operation, the handler maps the resulting `IllegalStateException` to HTTP `502 Bad Gateway` using Spring's `ProblemDetail`.

This communicates that the application could not obtain a usable response from the AI service.

---

## REST API

The example exposes one endpoint:

```text
POST /api/model-provider/chat
```

### Request

```json
{
  "question": "What is Spring Boot?"
}
```

### Successful Response

```json
{
  "question": "What is Spring Boot?",
  "answer": "Spring Boot is a framework built on Spring that simplifies application development through auto-configuration and starter dependencies."
}
```

The exact answer depends on the selected model.

### Error Handling

The endpoint validates the incoming question.

A missing or blank question results in a validation error.

If the AI model returns an empty response, the application returns HTTP `502 Bad Gateway`.

---

## Testing Model Provider Integration

The example uses Spring Boot Test, JUnit 6, Mockito, AssertJ, and MockMvc.

The tests are designed to verify application behavior and provider configuration without depending on actual AI model calls.

The test suite contains five test classes.

### 1. ChatAssistantServiceTest

Tests the application service independently of the actual model provider.

It verifies:

- Successful question processing.
- System prompt configuration.
- The user's question is passed to the chat client.
- Null model responses are rejected.
- Blank model responses are rejected.
- Exceptions from the chat client are propagated appropriately.

The test mocks the fluent `ChatClient` API.

This allows service behavior to be tested without connecting to Ollama or OpenAI.

### 2. ChatAssistantControllerTest

Tests the REST API using `@WebMvcTest` and `MockMvc`.

It verifies:

- Successful requests.
- Missing question validation.
- Blank question validation.
- Malformed JSON handling.
- HTTP `502 Bad Gateway` error handling.

The service is mocked so that the tests focus on the controller's HTTP behavior.

### 3. ChatAssistantIntegrationTest

Tests the integration between the application service and Spring AI's `ChatClient`.

Unlike the service unit test, this test uses the real `ChatClient` infrastructure.

The `ChatModel` is mocked.

This allows the test to verify that:

- The `ChatClient` is correctly constructed.
- System instructions are included in the prompt.
- The user's question is included in the prompt.
- The model response is returned through the application service.

The test does not require an actual Ollama or OpenAI connection.

### 4. ModelProviderIntegrationApplicationTest

Tests the application context with the default Ollama configuration.

It verifies that:

- The application context starts successfully.
- `ChatAssistantService` is available.
- `ChatClient.Builder` is available.
- The configured `ChatModel` is an instance of `OllamaChatModel`.

The provider assertion is particularly important.

A successful application context alone does not prove that the intended provider was selected.

The test explicitly checks the selected implementation.

### 5. OpenAiProviderApplicationTest

Tests the application context with the OpenAI profile.

It uses:

```java
@ActiveProfiles("openai")
```

A test API key is supplied through:

```java
@TestPropertySource(properties = "spring.ai.openai.api-key=test-api-key")
```

The test verifies that:

- The application context starts with the OpenAI profile.
- `ChatAssistantService` is available.
- `ChatClient.Builder` is available.
- The configured `ChatModel` is an instance of `OpenAiChatModel`.

The test does not make an actual request to OpenAI.

The test key is only used to initialize the provider configuration.

---

## Why Provider Selection Tests Matter

Testing that the application context starts successfully is not sufficient when an application supports multiple providers.

For example, the application might start but activate an unintended provider because of incorrect configuration.

The provider-selection tests verify the actual implementation:

```text
Default Configuration
        |
        v
OllamaChatModel
```

And:

```text
OpenAI Profile
        |
        v
OpenAiChatModel
```

This gives the example explicit coverage of its main objective: switching between model providers through configuration.

---

## Why the Tests Do Not Call Real AI Providers

The standard test suite should not depend on external AI services.

Making actual model requests during normal tests introduces several problems:

- Tests become dependent on network availability.
- Ollama must be installed and running.
- OpenAI requires a valid API key.
- API requests may incur costs.
- Model responses may vary between executions.
- Tests become slower and less predictable.

Instead, the example mocks the model when testing prompt interactions and uses provider auto-configuration for provider-selection tests.

This provides reliable automated coverage without requiring external model calls.

Manual requests can be used to verify actual provider communication.

---

## Running the Example

### Prerequisites

For the Ollama provider:

- Java 21.
- Maven.
- Ollama installed and running.
- The `llama3.2` model downloaded.

Download the model:

```bash
ollama pull llama3.2
```

For the OpenAI provider:

- Java 21.
- Maven.
- A valid OpenAI API key.

### Start with Ollama

From the repository root:

```bash
mvn -pl 10-spring-ai/model-provider-integration spring-boot:run
```

The application starts on:

```text
http://localhost:8089
```

### Start with OpenAI

```bash
AI_PROVIDER=openai OPENAI_API_KEY=your-api-key \
mvn -pl 10-spring-ai/model-provider-integration spring-boot:run
```

The application also starts on port `8089`.

---

## Testing the REST Endpoint

Once the application is running, send a request:

```bash
curl -X POST http://localhost:8089/api/model-provider/chat \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Explain the difference between Spring Framework and Spring Boot."
  }'
```

The selected provider processes the question and returns a response.

The same endpoint and request format work with both Ollama and OpenAI.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 10-spring-ai/model-provider-integration -am test
```

Run verification:

```bash
mvn -pl 10-spring-ai/model-provider-integration -am verify
```

---

## Key Takeaways

- Spring AI provides common abstractions for integrating different AI model providers.
- `ChatModel` represents the chat model abstraction.
- `ChatClient` provides a fluent API for interacting with chat models.
- Ollama supports locally hosted language models.
- OpenAI provides hosted language models through its API.
- Provider-specific dependencies make integrations available to the application.
- `spring.ai.model.chat` selects the active chat provider.
- Spring profiles allow provider configuration to be switched without changing application code.
- Environment variables provide a safer way to supply API credentials.
- Unused model auto-configurations can be disabled using Spring AI model selectors.
- Provider-independent application services reduce coupling to individual AI providers.
- Provider-selection tests verify that the expected implementation is active.
- Mocking model interactions keeps automated tests independent of external AI services.

---

## Next Example

This example completes the planned **Spring AI** examples in Module 10.
