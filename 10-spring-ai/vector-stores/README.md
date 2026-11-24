# Vector Stores

This example demonstrates **vector stores** in Spring AI.

It focuses on how Spring AI stores documents as vector embeddings, performs semantic similarity searches, and retrieves relevant documents using natural-language queries.

The example uses **Ollama with `nomic-embed-text`** to generate embeddings and Spring AI's `SimpleVectorStore` to store and retrieve documents in memory.

## What This Example Demonstrates

- What vector stores are and why they are useful in AI applications.
- The relationship between documents, embeddings, and vector stores.
- Spring AI's `VectorStore` abstraction.
- The `Document` abstraction for representing text and metadata.
- Configuring `SimpleVectorStore`.
- Integrating Ollama as the embedding model provider.
- Adding documents to a vector store.
- Automatic embedding generation during document ingestion.
- Performing semantic similarity searches.
- Configuring search requests with `SearchRequest`.
- Controlling the number of search results using `topK`.
- Retrieving document content and metadata.
- Testing vector store operations with Mockito.
- Testing actual vector storage and similarity search using deterministic embeddings.

---

## What Is a Vector Store?

A vector store is a storage system designed to store and retrieve data represented as numerical vectors.

In AI applications, text is commonly converted into embeddings before being stored.

An embedding is a numerical representation of content that captures aspects of its meaning.

For example, these sentences have different wording but related meanings:

```text
Spring Boot simplifies Java application development.

Building Java applications is easier with Spring Boot.
```

A suitable embedding model can produce vectors that are close to each other because the sentences express related concepts.

A vector store uses these vectors to find relevant content based on similarity rather than relying only on exact keyword matches.

Conceptually:

```text
Text
 │
 ▼
Embedding Model
 │
 ▼
Numerical Vector
 │
 ▼
Vector Store
 │
 ▼
Similarity Search
 │
 ▼
Relevant Documents
```

This makes vector stores useful for applications that need to retrieve information based on meaning.

---

## What Are Embeddings?

Embeddings are numerical representations of data.

An embedding model converts text into a vector containing floating-point values.

For example:

```text
Input:
"Spring Boot simplifies Java development."

Embedding:
[0.12, -0.34, 0.56, 0.78, ...]
```

The actual vector can contain hundreds or thousands of dimensions, depending on the embedding model.

Each dimension contributes to the representation learned by the model.

The important idea is that semantically related content tends to have similar vector representations.

For example:

```text
"Spring Boot simplifies Java development."
                    │
                    ▼
              Embedding Model
                    │
                    ▼
               Vector A

"Spring Boot makes Java development easier."
                    │
                    ▼
              Embedding Model
                    │
                    ▼
               Vector B
```

Vector A and Vector B may be close in the embedding space because the sentences have related meanings.

This allows an application to retrieve relevant content even when a search query uses different words from the stored document.

---

## What Is Semantic Similarity Search?

Semantic similarity search retrieves documents based on how closely their meanings relate to a query.

Traditional keyword search looks for matching words or phrases.

Semantic search uses embeddings to identify related content.

For example, consider these stored documents:

```text
Document 1:
Spring Boot simplifies Java application development.

Document 2:
Spring AI provides abstractions for integrating AI models
into Spring applications.

Document 3:
Cooking recipes require ingredients and preparation.
```

A user searches for:

```text
How can I build applications using the Spring ecosystem?
```

A keyword-based search might not find every relevant document because the wording differs.

A semantic search can retrieve the Spring Boot and Spring AI documents because their meanings are related to the query.

Conceptually:

```text
Stored Documents
       │
       ▼
Generate Embeddings
       │
       ▼
Store Vectors
       │
       │
       ▼
Natural-Language Query
       │
       ▼
Generate Query Embedding
       │
       ▼
Compare Vector Similarity
       │
       ▼
Rank Matching Documents
       │
       ▼
Return Relevant Results
```

---

## Spring AI VectorStore Abstraction

Spring AI provides the `VectorStore` abstraction for working with vector storage systems.

It provides a common API for adding documents and searching for similar documents.

The application does not need to manage embedding generation and vector storage manually.

The abstraction exposes operations such as:

```java
vectorStore.add(documents);
```

and:

```java
vectorStore.similaritySearch(searchRequest);
```

This separates application logic from the underlying vector storage implementation.

Different vector store implementations can be integrated without requiring the application service to be rewritten around a particular database API.

---

## SimpleVectorStore

This example uses Spring AI's `SimpleVectorStore`.

`SimpleVectorStore` is an in-memory implementation of the `VectorStore` abstraction.

It stores documents and their embeddings in application memory and supports similarity searches.

It is useful for:

- Learning how Spring AI vector stores work.
- Demonstrating document ingestion.
- Understanding semantic similarity search.
- Writing tests that do not require an external vector database.

However, it is not intended as a production persistence solution.

The documents are stored in memory and are lost when the application stops.

For production applications, a persistent vector store should be considered.

---

## Project Dependencies

The example uses the following dependencies:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-ollama</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-vector-store</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

The important dependencies are:

| Dependency                       | Purpose                                                    |
|----------------------------------|------------------------------------------------------------|
| `spring-ai-starter-model-ollama` | Integrates Ollama as the embedding model provider          |
| `spring-ai-vector-store`         | Provides vector store abstractions and `SimpleVectorStore` |
| `spring-boot-starter-webmvc`     | Provides REST API support                                  |
| `spring-boot-starter-validation` | Provides request validation                                |
| `spring-boot-starter-test`       | Provides testing support                                   |

---

## Ollama Embedding Model

The example uses Ollama with the `nomic-embed-text` model.

Ollama provides a locally running model that converts text into numerical embeddings.

Unlike a chat model, an embedding model is designed to represent text as vectors rather than generate conversational responses.

Download the model:

```bash
ollama pull nomic-embed-text
```

Ensure Ollama is running and available at:

```text
http://localhost:11434
```

The embedding model is configured in `application.yml`:

```yaml
spring:
  application:
    name: vector-stores

  ai:
    model:
      embedding: ollama

    ollama:
      base-url: http://localhost:11434
      embedding:
        model: nomic-embed-text

server:
  port: 8085
```

The important configuration is:

```yaml
spring:
  ai:
    model:
      embedding: ollama
```

This selects Ollama as the embedding model provider.

The model configuration:

```yaml
spring:
  ai:
    ollama:
      embedding:
        model: nomic-embed-text
```

specifies which Ollama embedding model Spring AI should use.

---

## Configuring SimpleVectorStore

The example configures `SimpleVectorStore` as a Spring bean.

```java
@Configuration
public class VectorStoreConfiguration {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

}
```

Spring injects the configured `EmbeddingModel` into the vector store.

The embedding model is responsible for converting document content into vectors.

The vector store uses these embeddings when storing and searching documents.

Conceptually:

```text
Spring Application Context
          │
          ▼
     EmbeddingModel
          │
          ▼
     SimpleVectorStore
          │
          ▼
       VectorStore
```

The application injects the `VectorStore` abstraction rather than depending directly on `SimpleVectorStore`.

For example:

```java
private final VectorStore vectorStore;

public VectorStoreService(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
}
```

This keeps the service independent of the specific vector store implementation.

---

## The Document Abstraction

Spring AI provides the `Document` abstraction to represent content that can be stored and retrieved through a vector store.

A document contains:

- A unique identifier.
- Text content.
- Metadata.

For example:

```java
Document document = new Document("Spring Boot simplifies Java application development.",
        Map.of("category", "spring", "topic", "spring-boot"));
```

The document contains the original text and additional metadata.

Metadata provides useful contextual information about the document.

For example:

```json
{
  "category": "spring",
  "topic": "spring-boot"
}
```

Metadata can be used to describe the source, category, topic, or other attributes associated with a document.

It can also support filtering when the selected vector store and search API provide that capability.

---

## Adding Documents to a Vector Store

The application exposes an endpoint for adding documents:

```text
POST /api/vector-stores/documents
```

The request accepts multiple documents.

Example request:

```json
{
  "documents": [
    {
      "content": "Spring Boot simplifies Java application development.",
      "metadata": {
        "category": "spring",
        "topic": "spring-boot"
      }
    },
    {
      "content": "Spring AI provides abstractions for integrating AI models into Spring applications.",
      "metadata": {
        "category": "spring",
        "topic": "spring-ai"
      }
    }
  ]
}
```

The request is represented by `AddDocumentsRequest`:

```java
public record AddDocumentsRequest(@NotEmpty List<@Valid DocumentInput> documents) {
}
```

Each document is represented by `DocumentInput`:

```java
public record DocumentInput(@NotBlank String content, Map<String, Object> metadata) {
}
```

The service converts the request objects into Spring AI `Document` objects.

```java
List<Document> documents = request.documents().stream()
        .map(input -> new Document(input.content(), input.metadata() == null ? Map.of() : input.metadata())).toList();
```

The documents are then added to the vector store:

```java
vectorStore.add(documents);
```

This is an important operation.

The application does not manually generate embeddings before calling `add()`.

The configured vector store uses the associated embedding model to generate embeddings for the documents.

Conceptually:

```text
Document Input
      │
      ▼
Spring AI Document
      │
      ▼
VectorStore.add()
      │
      ▼
EmbeddingModel
      │
      ▼
Generate Document Embedding
      │
      ▼
Store Document and Embedding
```

The service returns the number of documents submitted:

```json
{
  "documentsAdded": 2
}
```

---

## Performing Semantic Similarity Search

The application exposes another endpoint for searching stored documents:

```text
POST /api/vector-stores/search
```

The request accepts a natural-language query and an optional result limit.

Example:

```json
{
  "query": "How can I build applications using the Spring ecosystem?",
  "topK": 3
}
```

The request is represented by `VectorSearchRequest`:

```java
public record VectorSearchRequest(@NotBlank String query, @Min(1) @Max(20) Integer topK) {
}
```

The `query` field contains the text to search for.

The optional `topK` field specifies the maximum number of results to retrieve.

If `topK` is not provided, the service uses a default value of five.

---

## SearchRequest

Spring AI provides `SearchRequest` to configure a vector similarity search.

The service builds the search request:

```java
SearchRequest searchRequest = SearchRequest.builder().query(request.query()).topK(topK).build();
```

The request contains the query and the maximum number of results.

The service then executes the search:

```java
List<Document> documents = vectorStore.similaritySearch(searchRequest);
```

Spring AI uses the configured embedding model to generate an embedding for the query.

The vector store compares the query embedding against the stored document embeddings and returns the most similar documents.

Conceptually:

```text
Search Query
     │
     ▼
EmbeddingModel
     │
     ▼
Query Embedding
     │
     ▼
Vector Similarity Search
     │
     ▼
Rank Documents
     │
     ▼
Return Top Results
```

---

## Understanding topK

The `topK` parameter controls the maximum number of documents returned by a similarity search.

For example:

```java
SearchRequest.builder().query("Spring Boot").topK(3).build();
```

This requests up to three matching documents.

The example uses:

```java
private static final int DEFAULT_TOP_K = 5;
```

When the request does not specify `topK`, the default value is used.

The request also validates the allowed range:

```java
@Min(1)
@Max(20)
Integer topK
```

This prevents invalid values such as zero or values greater than twenty.

The maximum is an application-level constraint introduced by this example, not a universal Spring AI limitation.

---

## Search Results

The service maps retrieved Spring AI documents into application response objects.

```java
List<VectorSearchResult> results = documents.stream()
        .map(document -> new VectorSearchResult(document.getId(), document.getText(), document.getMetadata()))
        .toList();
```

The response contains:

- The document identifier.
- The original text.
- The document metadata.

The response is represented by `VectorSearchResponse`:

```java
public record VectorSearchResponse(String query, List<VectorSearchResult> results) {
}
```

Each result is represented by:

```java
public record VectorSearchResult(String id, String content, Map<String, Object> metadata) {
}
```

An example response looks like:

```json
{
  "query": "How can I build applications using the Spring ecosystem?",
  "results": [
    {
      "id": "generated-document-id",
      "content": "Spring Boot simplifies Java application development.",
      "metadata": {
        "category": "spring",
        "topic": "spring-boot"
      }
    }
  ]
}
```

The actual document identifiers and results depend on the documents stored and the embedding model.

The response deliberately returns document content and metadata rather than exposing the numerical embedding vectors.

---

## VectorStoreService

The `VectorStoreService` contains the application logic for document ingestion and semantic search.

It depends on the `VectorStore` abstraction:

```java
private final VectorStore vectorStore;

public VectorStoreService(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
}
```

The service provides two main operations:

### Adding documents

```java
public AddDocumentsResponse addDocuments(AddDocumentsRequest request) {
    List<Document> documents = request.documents().stream()
            .map(input -> new Document(input.content(), input.metadata() == null ? Map.of() : input.metadata())).toList();

    vectorStore.add(documents);

    return new AddDocumentsResponse(documents.size());
}
```

### Searching documents

```java
public VectorSearchResponse search(VectorSearchRequest request) {
    int topK = request.topK() == null ? DEFAULT_TOP_K : request.topK();
    SearchRequest searchRequest = SearchRequest.builder().query(request.query()).topK(topK).build();
    List<Document> documents = vectorStore.similaritySearch(searchRequest);

    List<VectorSearchResult> results = documents.stream()
            .map(document -> new VectorSearchResult(document.getId(), document.getText(), document.getMetadata()))
            .toList();

    return new VectorSearchResponse(request.query(), results);
}
```

The service does not directly interact with Ollama.

It delegates embedding and retrieval operations to the configured vector store.

This is one of the benefits of Spring AI's abstraction-based design.

---

## REST API

The example exposes two REST endpoints.

| HTTP Method | Endpoint                       | Description                               |
|-------------|--------------------------------|-------------------------------------------|
| `POST`      | `/api/vector-stores/documents` | Adds documents to the vector store        |
| `POST`      | `/api/vector-stores/search`    | Searches documents by semantic similarity |

The controller delegates operations to `VectorStoreService`.

### Adding Documents

```bash
curl -X POST http://localhost:8085/api/vector-stores/documents \
  -H "Content-Type: application/json" \
  -d '{
    "documents": [
      {
        "content": "Spring Boot simplifies Java application development.",
        "metadata": {
          "category": "spring",
          "topic": "spring-boot"
        }
      },
      {
        "content": "Spring AI integrates AI capabilities into Spring applications.",
        "metadata": {
          "category": "spring",
          "topic": "spring-ai"
        }
      },
      {
        "content": "Java is a programming language widely used for enterprise applications.",
        "metadata": {
          "category": "programming",
          "topic": "java"
        }
      }
    ]
  }'
```

Example response:

```json
{
  "documentsAdded": 3
}
```

### Searching Documents

```bash
curl -X POST http://localhost:8085/api/vector-stores/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "How can I build enterprise applications using the Spring ecosystem?",
    "topK": 3
  }'
```

The response contains documents ranked by semantic similarity.

The exact results depend on the embedding model and the documents stored.

---

## Exception Handling

The example includes a `VectorStoreExceptionHandler` implemented using `@RestControllerAdvice`.

It handles `IllegalStateException` failures from vector store operations.

```java
@ExceptionHandler(IllegalStateException.class)
public ProblemDetail handleIllegalStateException(IllegalStateException exception) {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_GATEWAY, "The vector store operation could not be completed.");

    problemDetail.setTitle("Vector Store Operation Failed");

    return problemDetail;
}
```

The handler returns HTTP `502 Bad Gateway` when a vector store operation fails with an `IllegalStateException`.

It avoids exposing internal exception messages or provider-specific details to API clients.

Request validation failures are handled separately by Spring MVC and Bean Validation.

---

## Testing Vector Stores

The example uses JUnit 5, Mockito, AssertJ, and Spring Boot testing support.

The tests cover document ingestion, semantic search, request validation, error handling, and vector store initialization.

### Service Tests

`VectorStoreServiceTest` uses a mocked `VectorStore`.

It verifies:

- Adding multiple documents.
- Preserving document content and metadata.
- Replacing null metadata with an empty map.
- Using the default result limit.
- Using a custom result limit.
- Mapping retrieved documents and metadata into response objects.
- Returning an empty result list when no documents match.
- Propagating ingestion failures.
- Propagating search failures.

Mocking the vector store allows the service logic to be tested independently of the embedding provider.

### Controller Tests

`VectorStoreControllerTest` uses standalone `MockMvc`.

It verifies:

- Successful document ingestion.
- Rejection of missing documents.
- Rejection of an empty documents list.
- Rejection of blank document content.
- Rejection of missing document content.
- Successful semantic search.
- Rejection of blank search queries.
- Rejection of `topK` values outside the allowed range.
- Returning HTTP `502 Bad Gateway` when a vector store operation fails.

The tests use `LocalValidatorFactoryBean` to enable Jakarta Bean Validation in the standalone MockMvc setup.

### Application Context Test

`VectorStoresApplicationTest` uses `@SpringBootTest`.

It verifies that the Spring application context can initialize with the configured application beans.

This test checks application wiring but does not perform an actual embedding request.

### Vector Store Integration Test

`VectorStoreIntegrationTest` uses the actual `SimpleVectorStore` implementation and a mocked `EmbeddingModel`.

It verifies that:

1. Documents can be added to the vector store.
2. Embeddings are generated for stored documents.
3. A search query receives an embedding.
4. Similarity search retrieves the expected documents.
5. Document metadata is preserved.

The test supplies deterministic embeddings using Mockito.

For example:

```java
private float[] createEmbedding(String text) {
    if (text.startsWith("Spring Boot")) {
        return new float[]{1.0F, 0.0F, 0.0F};
    }

    if (text.startsWith("Spring AI")) {
        return new float[]{0.9F, 0.1F, 0.0F};
    }

    if (text.startsWith("Cooking")) {
        return new float[]{0.0F, 1.0F, 0.0F};
    }

    if (text.startsWith("How do I build")) {
        return new float[]{0.95F, 0.05F, 0.0F};
    }

    throw new IllegalArgumentException("Unexpected text: " + text);
}
```

The deterministic vectors make it possible to verify similarity-search behavior without relying on an external AI service.

**Important:** This test exercises the actual in-memory vector store, but it does not verify the natural-language understanding capabilities of Ollama. The embedding model itself is mocked.

---

## Running the Example

Ensure Ollama is installed and running.

Download the embedding model:

```bash
ollama pull nomic-embed-text
```

Start Ollama if it is not already running:

```bash
ollama serve
```

From the repository root, start the application:

```bash
mvn -pl 10-spring-ai/vector-stores spring-boot:run
```

The application starts on:

```text
http://localhost:8085
```

---

## Running the Tests

Run the module tests from the repository root:

```bash
mvn -pl 10-spring-ai/vector-stores test
```

To run the module and its required Maven dependencies:

```bash
mvn -pl 10-spring-ai/vector-stores -am clean verify
```

To run only the vector store integration test:

```bash
mvn -pl 10-spring-ai/vector-stores -am \
    -Dtest=VectorStoreIntegrationTest \
    -Dsurefire.failIfNoSpecifiedTests=false test
```

The integration test uses deterministic embeddings and does not require a running Ollama instance.

---

## In-Memory vs Persistent Vector Stores

This example deliberately uses `SimpleVectorStore` to focus on the Spring AI programming model.

However, an in-memory vector store has limitations.

| In-Memory Vector Store                  | Persistent Vector Store                                    |
|-----------------------------------------|------------------------------------------------------------|
| Stores documents in application memory  | Stores documents in persistent storage                     |
| Data is lost when the application stops | Data can survive application restarts                      |
| Useful for demonstrations and testing   | Suitable for persistent application data                   |
| Minimal infrastructure                  | Usually requires database or vector storage infrastructure |

Spring AI supports integrations with persistent vector stores, including PostgreSQL with PGvector and other vector database technologies.

The choice of vector store depends on application requirements such as persistence, scalability, filtering, operational complexity, and deployment environment.

---

## Vector Stores and Retrieval-Augmented Generation

Vector stores are an important building block for Retrieval-Augmented Generation (RAG).

RAG combines document retrieval with a language model to generate responses grounded in relevant external information.

A simplified RAG workflow looks like:

```text
          Documents
              │
              ▼
       Embedding Model
              │
              ▼
          Vector Store
              │
              │
              ▼
        User Question
              │
              ▼
       Query Embedding
              │
              ▼
       Similarity Search
              │
              ▼
      Retrieved Documents
              │
              ▼
       Prompt + Context
              │
              ▼
          Chat Model
              │
              ▼
        Generated Answer
```

The vector store is responsible for retrieving relevant documents.

The retrieved documents can then be supplied to a chat model as context.

This example focuses on document ingestion and retrieval. It does not implement the complete RAG workflow.

---

## Key Takeaways

- Vector stores store documents alongside their numerical embeddings.
- Embeddings represent the semantic characteristics of content.
- Semantic similarity search retrieves relevant content based on meaning rather than exact keyword matches.
- Spring AI provides the `VectorStore` abstraction for working with vector storage systems.
- `Document` represents text content and associated metadata.
- `EmbeddingModel` converts document and query text into vectors.
- `VectorStore.add()` handles document ingestion and embedding generation.
- `SearchRequest` configures similarity searches.
- `topK` controls the maximum number of results returned.
- `similaritySearch()` retrieves documents based on vector similarity.
- `SimpleVectorStore` is useful for demonstrations and testing but is not a persistent production storage solution.
- Vector stores are an important foundation for Retrieval-Augmented Generation.

---

## Next Example

The next example will explore **Retrieval-Augmented Generation (RAG)** in Spring AI, demonstrating how retrieved documents from a vector store can be combined with a chat model to generate context-aware responses.
