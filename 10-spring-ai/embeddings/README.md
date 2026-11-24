# Embeddings

This example demonstrates **text embeddings** using Spring AI and Ollama.

It focuses on how Spring AI's `EmbeddingModel` converts text into numerical vector representations, how to generate embeddings for multiple text inputs, and how to expose embedding generation through a REST API.

The example uses Ollama with the `nomic-embed-text` embedding model, allowing embeddings to be generated locally without requiring an external AI API.

## What This Example Demonstrates

- What embeddings are and why they are useful in AI applications.
- The relationship between text and numerical vector representations.
- Spring AI's `EmbeddingModel` abstraction.
- Configuring Ollama as an embedding model provider.
- Using `nomic-embed-text` to generate embeddings.
- Generating embeddings for multiple text inputs.
- Understanding embedding dimensions.
- Converting primitive `float[]` vectors into Java collections.
- Exposing embedding generation through a REST API.
- Validating embedding requests.
- Handling invalid embedding model responses.
- Testing embedding generation using Mockito and MockMvc.

---

## What Are Embeddings?

Embeddings are numerical representations of data, such as text, images, or other information.

In natural language processing, an embedding model converts text into a vector containing floating-point numbers.

For example, consider the following sentence:

```text
Spring Boot simplifies Java application development.
```

An embedding model converts it into a vector resembling:

```text
[0.0123, -0.0456, 0.0789, 0.0234, -0.0678, ...]
```

Each number represents a dimension in the vector space.

The resulting vector captures semantic characteristics of the original text, allowing applications to compare the meaning of different pieces of text mathematically.

For example:

```text
Text A:
Spring Boot simplifies Java application development.

Text B:
Spring Boot makes building Java applications easier.

Text C:
The weather is sunny today.
```

Text A and Text B express similar ideas. Their embeddings may therefore be closer together in vector space than the embedding for Text C.

This makes embeddings useful for applications that need to understand relationships between pieces of information rather than relying only on exact keyword matches.

---

## Why Are Embeddings Important?

Traditional text matching typically relies on words, phrases, or exact keyword comparisons.

For example, searching for:

```text
Java application framework
```

may not match a document containing:

```text
Spring Boot simplifies enterprise application development.
```

Even though the two texts are related, they do not contain exactly the same words.

Embeddings help address this limitation by representing semantic information numerically.

They allow applications to identify related content even when the wording differs.

Common use cases include:

- Semantic search.
- Document similarity.
- Recommendation systems.
- Document classification.
- Duplicate content detection.
- Retrieval-Augmented Generation (RAG).
- Knowledge retrieval.
- Clustering related documents.

---

## How Embeddings Work

An embedding model processes text and produces a numerical vector.

A simplified flow looks like:

```text
Input Text
    |
    v
Embedding Model
    |
    v
Numerical Vector
    |
    v
Embedding Representation
```

For example:

```text
"Spring Boot simplifies Java development."
                  |
                  v
          Embedding Model
                  |
                  v
       [0.12, -0.34, 0.56, ...]
```

The generated vector represents the input text in a numerical format that can be used by mathematical operations and similarity algorithms.

The embedding model determines the dimensionality and characteristics of the resulting vector.

---

## Embeddings in Spring AI

Spring AI provides the `EmbeddingModel` abstraction for generating embeddings.

This abstraction separates application code from the underlying embedding provider.

Instead of directly integrating with a particular provider's API, an application can depend on Spring AI's `EmbeddingModel` interface.

```java
import org.springframework.ai.embedding.EmbeddingModel;
```

The application can then use the abstraction to generate embeddings:

```java
float[] embedding = embeddingModel.embed("Spring AI");
```

Spring AI also supports generating embeddings for multiple text inputs:

```java
List<float[]> embeddings = embeddingModel.embed(texts);
```

This makes it possible to generate embeddings without implementing provider-specific HTTP requests or response parsing.

The configured embedding provider handles the actual model communication.

---

## Ollama Embedding Model

This example uses Ollama as the embedding model provider.

Ollama allows language and embedding models to run locally.

The example uses:

```text
nomic-embed-text
```

`nomic-embed-text` is an embedding model designed to generate vector representations of text.

Unlike a chat model, an embedding model does not generate conversational responses.

Its responsibility is to convert input text into numerical vectors.

### Downloading the Model

Make sure Ollama is installed and running.

Download the model:

```bash
ollama pull nomic-embed-text
```

Verify that the model is available:

```bash
ollama list
```

The model should appear in the available models list.

---

## Configuring Ollama in Spring AI

The application configures Ollama as the embedding provider through `application.yml`.

```yaml
spring:
  application:
    name: embeddings
  ai:
    model:
      embedding: ollama
    ollama:
      base-url: http://localhost:11434
      embedding:
        model: nomic-embed-text

server:
  port: 8084
```

### Configuration Explained

| Property                           | Description                              |
|------------------------------------|------------------------------------------|
| `spring.application.name`          | Application name                         |
| `spring.ai.model.embedding`        | Selects Ollama as the embedding provider |
| `spring.ai.ollama.base-url`        | Ollama server address                    |
| `spring.ai.ollama.embedding.model` | Specifies the embedding model            |
| `server.port`                      | Application HTTP port                    |

The following property selects Ollama as the embedding provider:

```yaml
spring:
  ai:
    model:
      embedding: ollama
```

The following property specifies which model generates the embeddings:

```yaml
spring:
  ai:
    ollama:
      embedding:
        model: nomic-embed-text
```

The chat model and embedding model are separate capabilities in Spring AI.

This example configures only the embedding model because it does not generate conversational responses.

---

## The EmbeddingModel Interface

The application injects Spring AI's `EmbeddingModel` interface into `EmbeddingsService`.

```java
@Service
public class EmbeddingsService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingsService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }
}
```

Spring Boot auto-configuration creates the appropriate embedding model implementation based on the configured provider.

Because the application selects Ollama, Spring AI configures an Ollama-backed implementation.

The service therefore does not need to know how Ollama communicates with its underlying model.

This separation allows the application to change embedding providers without rewriting the service's embedding-generation logic, provided the replacement provider supports the same Spring AI abstraction.

---

## Generating Embeddings

The main responsibility of the example is generating embeddings from text inputs.

Spring AI provides the following method:

```java
List<float[]> vectors = embeddingModel.embed(texts);
```

The method accepts a list of text inputs and returns a list of numerical vectors.

Each returned vector corresponds to an input text.

For example:

```java
List<String> texts = List.of(
        "Spring Boot simplifies Java development.",
        "Spring AI integrates AI capabilities into Spring applications."
);

List<float[]> vectors = embeddingModel.embed(texts);
```

The result contains two embedding vectors:

```text
Input 1
   |
   v
Embedding Vector 1

Input 2
   |
   v
Embedding Vector 2
```

Each vector contains floating-point values representing the corresponding input text.

The order of the returned vectors corresponds to the order of the input texts.

---

## Single Text vs Batch Embeddings

Spring AI supports generating embeddings for individual text inputs and batches of text inputs.

### Single Text Embedding

A single text can be converted into an embedding using:

```java
float[] vector = embeddingModel.embed("Spring AI");
```

The result is a single numerical vector.

### Batch Embeddings

Multiple texts can be processed together:

```java
List<String> texts = List.of("Spring AI", "Spring Boot", "Spring Framework");

List<float[]> vectors = embeddingModel.embed(texts);
```

The result is a list of vectors.

Batch embedding is useful when processing multiple documents, paragraphs, or other text inputs.

It also provides a convenient abstraction for applications that need to generate embeddings for collections of content.

---

## Understanding Embedding Vectors

An embedding vector is an array of floating-point numbers.

For example:

```java
float[] vector = { 0.12f, -0.34f, 0.56f, 0.78f };
```

This vector contains four dimensions.

The number of dimensions is determined by the embedding model.

For example, an embedding model may return a vector containing 768 dimensions.

```java
int dimensions = vector.length;
```

The dimensionality describes the number of numerical values in the vector.

A vector with 768 dimensions contains 768 floating-point values.

### Why Dimensions Matter

Dimensions determine the size of the numerical representation produced by the model.

When storing or comparing embeddings, applications need to account for the vector dimensions.

For example, vectors generated by different embedding models may have different dimensions.

Even if two models produce vectors with the same number of dimensions, their vector spaces are not necessarily compatible.

Therefore, applications should use compatible embedding models when comparing or searching vectors.

---

## Converting Embedding Vectors into Java Collections

Spring AI returns embedding vectors as primitive `float[]` arrays.

The example converts these arrays into `List<Float>` so they can be conveniently exposed through JSON responses.

```java
List<Float> values = new ArrayList<>(vector.length);

for (float value : vector) {
    values.add(value);
}
```

Java's `Arrays.stream()` does not provide an overload for primitive `float[]`, so a loop is used for this conversion.

The resulting collection can be included in a response DTO and serialized by Spring Boot's Jackson integration.

---

## The Embedding Request

The API accepts a collection of text inputs through `EmbeddingRequest`.

```java
public record EmbeddingRequest(@NotEmpty List<@NotBlank String> texts) {
}
```

The request contains:

| Field   | Description                                          |
|---------|------------------------------------------------------|
| `texts` | Collection of text inputs to convert into embeddings |

The validation annotations enforce the following rules:

- The `texts` collection must not be empty.
- Individual text elements must not be blank.
- Null elements are rejected by the element-level `@NotBlank` constraint.

For example, a valid request is:

```json
{
  "texts": [
    "Spring AI",
    "Spring Boot",
    "Spring Framework"
  ]
}
```

An invalid request is:

```json
{
  "texts": []
}
```

Another invalid request is:

```json
{
  "texts": [
    "Spring AI",
    "   "
  ]
}
```

---

## The EmbeddedText Response

Each generated embedding is represented by the `EmbeddedText` record.

```java
public record EmbeddedText(String text, int dimensions, List<Float> vector) {
}
```

The record contains three fields:

| Field        | Description                              |
|--------------|------------------------------------------|
| `text`       | Original text input                      |
| `dimensions` | Number of values in the embedding vector |
| `vector`     | Generated numerical embedding            |

This representation keeps the original text associated with its generated vector.

It also exposes the vector dimensions so that consumers can understand the size of the embedding.

---

## The EmbeddingResponse

The service returns an `EmbeddingResponse` containing all generated embeddings.

```java
public record EmbeddingResponse(List<EmbeddedText> embeddings) {
}
```

This provides a consistent response structure for both individual and batch embedding requests.

A response may look like:

```json
{
  "embeddings": [
    {
      "text": "Spring AI",
      "dimensions": 768,
      "vector": [0.0123, -0.0456, 0.0789]
    },
    {
      "text": "Spring Boot",
      "dimensions": 768,
      "vector": [0.0234, -0.0567, 0.0891]
    }
  ]
}
```

The vector values above are illustrative and shortened for readability. Actual responses contain all numerical values returned by the configured embedding model.

The dimensionality shown is also illustrative; the actual dimension count should be determined from the returned vector.

---

## EmbeddingsService

`EmbeddingsService` contains the application's embedding-generation logic.

It receives Spring AI's `EmbeddingModel` through constructor injection.

```java
@Service
public class EmbeddingsService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingsService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }
}
```

The service exposes:

```java
public EmbeddingResponse generateEmbeddings(List<String> texts)
```

### Generating the Vectors

The service delegates embedding generation to Spring AI:

```java
List<float[]> vectors = embeddingModel.embed(texts);
```

The service verifies that the returned vector collection is not null and contains the expected number of vectors.

```java
if (vectors == null || vectors.size() != texts.size()) {
    throw new IllegalStateException("The embedding model returned an invalid response.");
}
```

This protects the application from incomplete or invalid model responses.

### Validating Individual Vectors

Each vector is checked before being included in the response.

```java
if (vector == null || vector.length == 0) {
    throw new IllegalStateException("The embedding model returned an empty embedding.");
}
```

An embedding must contain numerical values to be useful.

### Building the Response

The service converts each primitive vector into a Java collection and creates an `EmbeddedText` object.

The resulting objects are collected into an `EmbeddingResponse`.

Conceptually:

```text
List<String>
     |
     v
EmbeddingModel
     |
     v
List<float[]>
     |
     v
Validate Vectors
     |
     v
Convert float[] to List<Float>
     |
     v
List<EmbeddedText>
     |
     v
EmbeddingResponse
```

This keeps the provider-specific embedding generation separate from the application's response representation.

---

## REST API

The example exposes embedding generation through a REST endpoint.

```text
POST /api/embeddings
```

The endpoint accepts a list of texts and returns their generated embeddings.

### EmbeddingsController

The controller delegates embedding generation to `EmbeddingsService`.

```java
@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingsController {

    private final EmbeddingsService embeddingsService;

    public EmbeddingsController(EmbeddingsService embeddingsService) {
        this.embeddingsService = embeddingsService;
    }

    @PostMapping
    public ResponseEntity<EmbeddingResponse> generateEmbeddings(@Valid @RequestBody EmbeddingRequest request) {
        EmbeddingResponse response = embeddingsService.generateEmbeddings(request.texts());

        return ResponseEntity.ok(response);
    }
}
```

The controller performs three main responsibilities:

1. Accepts the incoming HTTP request.
2. Validates the request body.
3. Delegates embedding generation to the service and returns the response.

The controller does not contain embedding-generation logic.

---

## Exception Handling

The example uses `EmbeddingsExceptionHandler` to handle failures raised during embedding generation.

The handler maps `IllegalStateException` to HTTP `502 Bad Gateway`.

```java
@ExceptionHandler(IllegalStateException.class)
public ProblemDetail handleIllegalStateException(IllegalStateException exception) {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_GATEWAY, "The embedding model returned a response that could not be processed.");

    problemDetail.setTitle("Embedding Generation Failed");

    return problemDetail;
}
```

A `502 Bad Gateway` response is appropriate when the application cannot successfully process a response from the configured embedding provider.

The handler uses Spring's `ProblemDetail` to return a standardized error response.

It deliberately avoids exposing internal provider details to API consumers.

---

## Embedding Generation Flow

The complete request flow looks like:

```text
HTTP Client
    |
    v
POST /api/embeddings
    |
    v
EmbeddingsController
    |
    v
Request Validation
    |
    v
EmbeddingsService
    |
    v
EmbeddingModel
    |
    v
Ollama
    |
    v
nomic-embed-text
    |
    v
Generated Embedding Vectors
    |
    v
Response Mapping
    |
    v
EmbeddingResponse
    |
    v
HTTP Response
```

The application uses Spring AI to abstract the embedding provider while keeping the REST API independent of Ollama's underlying communication details.

---

## Testing Embeddings

The example uses Spring Boot testing support, JUnit 5, Mockito, and MockMvc.

The tests verify embedding generation, response mapping, request validation, exception handling, and application initialization.

### Service Tests

`EmbeddingsServiceTest` uses a mocked `EmbeddingModel`.

This allows embedding-generation behavior to be tested without requiring Ollama to be running.

The service tests cover:

| Test                                                           | What It Verifies                                            |
|----------------------------------------------------------------|-------------------------------------------------------------|
| `shouldGenerateEmbeddingsForMultipleTexts`                     | Successfully generates and maps multiple embeddings         |
| `shouldThrowExceptionWhenEmbeddingResponseIsNull`              | Handles a null embedding response                           |
| `shouldThrowExceptionWhenEmbeddingCountDoesNotMatchInputCount` | Detects a mismatch between input texts and returned vectors |
| `shouldThrowExceptionWhenEmbeddingVectorIsNull`                | Rejects null embedding vectors                              |
| `shouldThrowExceptionWhenEmbeddingVectorIsEmpty`               | Rejects empty embedding vectors                             |
| `shouldPropagateEmbeddingModelException`                       | Propagates exceptions from the embedding model              |

### Controller Tests

`EmbeddingsControllerTest` uses standalone MockMvc with a mocked service.

The controller tests cover:

| Test                                                 | What It Verifies                               |
|------------------------------------------------------|------------------------------------------------|
| `shouldGenerateEmbeddings`                           | Successfully processes an embedding request    |
| `shouldReturnBadRequestWhenTextsAreMissing`          | Rejects requests without the `texts` field     |
| `shouldReturnBadRequestWhenTextsAreEmpty`            | Rejects an empty collection                    |
| `shouldReturnBadRequestWhenTextIsBlank`              | Rejects blank text elements                    |
| `shouldReturnBadRequestWhenTextContainsNullElement`  | Rejects null elements                          |
| `shouldReturnBadRequestWhenRequestBodyIsMalformed`   | Rejects malformed JSON                         |
| `shouldReturnBadGatewayWhenEmbeddingGenerationFails` | Maps embedding-generation failures to HTTP 502 |

### Application Context Test

`EmbeddingsApplicationTest` uses `@SpringBootTest` to verify that the application context initializes successfully.

It verifies that Spring Boot can configure the application and its required beans.

The context test does not generate an actual embedding.

---

## Important Testing Details

### Mocking EmbeddingModel

The service tests mock `EmbeddingModel`:

```java
embeddingModel = mock(EmbeddingModel.class);
```

A controlled embedding response can then be supplied:

```java
when(embeddingModel.embed(texts)).thenReturn(List.of(
        new float[]{0.1f, 0.2f, 0.3f},
        new float[]{0.4f, 0.5f, 0.6f}
));
```

This makes it possible to verify response conversion and error handling without depending on a real embedding model.

### Testing Request Validation

The controller tests use a Spring-compatible validator:

```java
validator = new LocalValidatorFactoryBean();
validator.afterPropertiesSet();
```

It is registered with standalone MockMvc:

```java
mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new EmbeddingsExceptionHandler())
        .setValidator(validator)
        .build();
```

`LocalValidatorFactoryBean` integrates Jakarta Bean Validation with Spring's validation infrastructure.

This ensures that the request's `@NotEmpty` and `@NotBlank` constraints are evaluated during controller tests.

### Testing Without Ollama

The unit and controller tests use mocks, so they do not need a running Ollama instance.

The application context test may still initialize provider-related infrastructure depending on the Spring AI and Ollama configuration.

For a fully isolated test environment, provider initialization and external service dependencies should be considered separately.

---

## Running the Example

Make sure Ollama is installed and running.

Download the embedding model:

```bash
ollama pull nomic-embed-text
```

Start the application from the repository root:

```bash
mvn -pl 10-spring-ai/embeddings spring-boot:run
```

The application starts on:

```text
http://localhost:8084
```

---

## Trying the API

Send a request containing multiple texts:

```bash
curl -X POST http://localhost:8084/api/embeddings \
  -H "Content-Type: application/json" \
  -d '{
    "texts": [
      "Spring Boot simplifies Java application development.",
      "Spring AI integrates artificial intelligence into Spring applications.",
      "Java is a widely used programming language."
    ]
  }'
```

The API returns the original texts together with their generated embedding vectors and dimensions.

Because embedding vectors can contain hundreds or thousands of values, the complete response may be relatively large.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 10-spring-ai/embeddings test
```

To run the module together with its required Maven dependencies:

```bash
mvn -pl 10-spring-ai/embeddings -am clean verify
```

---

## Key Takeaways

- Embeddings represent text as numerical vectors.
- Embedding models capture semantic characteristics of input text.
- Similar pieces of text can have embeddings that are closer together in vector space.
- Spring AI provides the `EmbeddingModel` abstraction for generating embeddings.
- Ollama can run embedding models locally.
- `nomic-embed-text` is used in this example to generate text embeddings.
- Spring AI supports generating embeddings for individual texts and batches.
- Embedding dimensions represent the number of numerical values in a vector.
- The dimensionality depends on the configured embedding model.
- Embedding vectors returned as `float[]` can be converted into Java collections for API responses.
- Embeddings are useful for semantic search, document similarity, recommendations, and RAG applications.
- Generating embeddings is separate from storing, comparing, or searching them.
- Unit and controller tests can verify embedding-generation behavior without invoking a real embedding provider.

---

## Next Example

The next example will explore **Vector Stores** in Spring AI.

It will demonstrate how generated embeddings can be stored, retrieved, and searched using a vector store abstraction.

The example will build on the embedding-generation concepts introduced here and explore how applications can persist vector representations for similarity-based retrieval.
