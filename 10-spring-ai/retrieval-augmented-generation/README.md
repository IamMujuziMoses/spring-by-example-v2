# Retrieval-Augmented Generation (RAG)

This example demonstrates **Retrieval-Augmented Generation (RAG)** with Spring AI.

It focuses on combining a vector store with an AI chat model to provide relevant knowledge as context when answering user questions.

The example uses Spring AI's `QuestionAnswerAdvisor`, Ollama for local AI model execution, `nomic-embed-text` for generating embeddings, and `SimpleVectorStore` for storing and retrieving documents.

## What This Example Demonstrates

- What Retrieval-Augmented Generation (RAG) means.
- The limitations of relying exclusively on an AI model's pretrained knowledge.
- How documents are converted into vector embeddings.
- How embeddings enable semantic similarity search.
- How a vector store maintains knowledge documents.
- How Spring AI's `SimpleVectorStore` stores and retrieves documents.
- How `QuestionAnswerAdvisor` retrieves relevant context.
- How retrieved documents are incorporated into an AI prompt.
- How `ChatClient` generates answers using retrieved context.
- How to build a RAG REST API with Spring Boot.
- Testing document ingestion, question answering, and retrieval behavior.

---

## What Is Retrieval-Augmented Generation?

Retrieval-Augmented Generation (RAG) is an approach that combines information retrieval with generative AI.

Instead of relying entirely on an AI model's pretrained knowledge, an application retrieves relevant information from an external knowledge source and supplies it to the model as context.

This allows an AI application to answer questions using information provided by the application.

A simplified RAG workflow looks like:

```text
User Question
      │
      ▼
Generate Query Embedding
      │
      ▼
Search Vector Store
      │
      ▼
Retrieve Relevant Documents
      │
      ▼
Augment AI Prompt
      │
      ▼
Send Prompt to Chat Model
      │
      ▼
Generate Answer
      │
      ▼
Return Answer
```

The important distinction is that the application retrieves relevant knowledge before asking the model to generate its response.

---

## Why RAG?

Large language models are trained on existing datasets. Their pretrained knowledge may not contain an application's private, domain-specific, or recently updated information.

For example, imagine an application that contains internal documentation about:

- Spring Boot configuration.
- Spring Security authentication.
- Spring Data repositories.
- Application deployment procedures.

A user might ask:

```text
How does Spring Boot configure applications?
```

A general-purpose model might provide an answer based on its pretrained knowledge.

However, the application may need the answer to be grounded in its own documentation.

RAG addresses this by retrieving relevant documents and including them in the prompt sent to the model.

This helps the model generate responses based on the information available in the application's knowledge base.

RAG does not guarantee factual accuracy, but it provides relevant source information that can help constrain the generated response.

---

## RAG Architecture Used in This Example

This example combines three main Spring AI components:

| Component        | Responsibility                                                   |
|------------------|------------------------------------------------------------------|
| `EmbeddingModel` | Converts text into numerical vector representations.             |
| `VectorStore`    | Stores documents and retrieves similar documents.                |
| `ChatClient`     | Sends augmented prompts to the chat model and obtains responses. |

The example uses the following implementations:

| Technology              | Purpose                                                      |
|-------------------------|--------------------------------------------------------------|
| Spring Boot             | Application framework and REST API.                          |
| Spring AI               | AI integration and RAG orchestration.                        |
| Ollama                  | Runs local AI models.                                        |
| Llama 3.2               | Generates natural-language answers.                          |
| `nomic-embed-text`      | Generates text embeddings.                                   |
| `SimpleVectorStore`     | Stores documents and performs similarity searches in memory. |
| `QuestionAnswerAdvisor` | Retrieves relevant documents and augments the chat prompt.   |

The architecture looks like:

```text
                 Spring Boot Application
                          │
                          ▼
                    RagController
                          │
                          ▼
                      RagService
                          │
             ┌────────────┴────────────┐
             │                         │
             ▼                         ▼
         VectorStore               ChatClient
             │                         │
             ▼                         ▼
      SimpleVectorStore       QuestionAnswerAdvisor
             │                         │
             ▼                         ▼
       EmbeddingModel            Retrieve Context
             │                         │
             ▼                         ▼
        Ollama Model              Augmented Prompt
                                       │
                                       ▼
                                  Chat Model
                                       │
                                       ▼
                                    Answer
```

The `QuestionAnswerAdvisor` connects the retrieval process to the chat interaction.

---

## What Are Embeddings?

Embeddings are numerical representations of text.

An embedding model converts a piece of text into a vector containing numerical values.

For example:

```text
Text:
Spring Boot simplifies application configuration.

Embedding:
[0.21, -0.34, 0.78, 0.12, ...]
```

The actual embedding contains many dimensions. The values above are illustrative.

Texts with similar meanings can produce vectors that are close to each other in vector space.

For example:

```text
Document:
Spring Boot simplifies application configuration.

Question:
How does Spring Boot configure applications?
```

Although the wording differs, the two texts have related meanings.

An embedding model helps represent this semantic similarity numerically.

This makes it possible to retrieve relevant documents even when a user's question does not contain the exact words used in the original document.

---

## Understanding Vector Stores

A vector store manages documents and their vector embeddings.

It supports similarity searches that identify documents whose embeddings are close to the embedding of a supplied query.

A simplified representation looks like:

```text
Knowledge Document
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
Stored Document + Embedding
```

When a user asks a question:

```text
User Question
      │
      ▼
Embedding Model
      │
      ▼
Query Vector
      │
      ▼
Vector Similarity Search
      │
      ▼
Relevant Documents
```

A vector store therefore provides the retrieval mechanism used by a RAG application.

---

## SimpleVectorStore

This example uses Spring AI's `SimpleVectorStore`.

It provides a straightforward in-memory implementation of the `VectorStore` abstraction.

The configuration creates the vector store using an `EmbeddingModel`:

```java
@Configuration
public class RagConfiguration {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
```

Spring AI uses the supplied embedding model to generate embeddings for documents and search queries.

### Important: In-Memory Storage

`SimpleVectorStore` stores its data in memory.

This makes it useful for:

- Learning how vector stores work.
- Demonstrating semantic search.
- Building small examples.
- Testing RAG workflows.

However, it is not a persistent production database.

Documents are lost when the application stops unless a separate persistence mechanism is introduced.

Production applications can use persistent vector store implementations such as PostgreSQL with pgvector, Redis, Elasticsearch, or other supported vector databases.

---

## Adding Knowledge to the Vector Store

Before answering questions, the application needs knowledge documents.

The example exposes an endpoint for adding documents:

```http
POST /api/rag/documents
Content-Type: application/json
```

Example request:

```json
{
  "documents": [
    {
      "content": "Spring Boot uses auto-configuration to simplify application setup.",
      "metadata": {
        "category": "spring-boot"
      }
    },
    {
      "content": "Spring Security provides authentication and authorization.",
      "metadata": {
        "category": "spring-security"
      }
    }
  ]
}
```

Each document contains:

- `content`: The text that should be stored and made available for retrieval.
- `metadata`: Additional information associated with the document.

Metadata can describe document categories, sources, authors, or other useful attributes.

The service converts the incoming knowledge documents into Spring AI `Document` objects:

```java
List<Document> documents = request.documents().stream()
        .map(document -> new Document(document.content(), document.metadata() != null ? document.metadata() : Map.of()))
        .toList();

vectorStore.add(documents);
```

The vector store uses the configured embedding model to generate embeddings and store the documents.

The application then returns the number of documents added.

Conceptually:

```text
REST Request
     │
     ▼
Knowledge Documents
     │
     ▼
Spring AI Documents
     │
     ▼
VectorStore.add(...)
     │
     ▼
Embedding Generation
     │
     ▼
Document Storage
```

---

## Metadata

Metadata provides additional information about a document without changing its main content.

For example:

```json
{
  "content": "Spring Security provides authentication and authorization.",
  "metadata": {
    "category": "spring-security",
    "source": "security-documentation"
  }
}
```

Metadata can be useful when organizing knowledge and applying filters during retrieval.

For example, an application might retrieve documents only from a particular category.

This example preserves metadata when converting incoming knowledge documents into Spring AI documents.

---

## QuestionAnswerAdvisor

The central component in this example is Spring AI's `QuestionAnswerAdvisor`.

It connects a vector store to a chat interaction.

The advisor retrieves relevant documents using the user's question and adds the retrieved information to the prompt before the chat model generates its answer.

The configuration looks like:

```java
QuestionAnswerAdvisor questionAnswerAdvisor =
        QuestionAnswerAdvisor.builder(vectorStore).searchRequest(SearchRequest.builder().topK(4).build())
                .build();
```

### Understanding `topK`

The `topK` setting determines the maximum number of similar documents requested during a search.

For example:

```java
SearchRequest.builder().topK(4).build()
```

This requests up to four relevant documents.

A smaller value can reduce the amount of context supplied to the model.

A larger value can provide more information, but it may also introduce irrelevant content or consume more context tokens.

The appropriate value depends on the size and organization of the knowledge base and the application's retrieval requirements.

---

## Configuring ChatClient with QuestionAnswerAdvisor

The example registers the advisor as a default advisor on the `ChatClient`:

```java
@Bean
public ChatClient ragChatClient(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
    QuestionAnswerAdvisor questionAnswerAdvisor =
            QuestionAnswerAdvisor.builder(vectorStore).searchRequest(SearchRequest.builder().topK(4).build()).build();

    return chatClientBuilder.defaultAdvisors(questionAnswerAdvisor).build();
}
```

This configuration means that chat interactions through this `ChatClient` use the configured retrieval advisor.

When the application sends a question, the advisor retrieves relevant documents and augments the prompt.

The application does not need to manually perform the similarity search and construct the augmented prompt for each question.

Spring AI handles that integration through the advisor.

---

## Asking Questions

The example exposes a question-answering endpoint:

```http
POST /api/rag/questions
Content-Type: application/json
```

Example request:

```json
{
  "question": "How does Spring Boot configure applications?"
}
```

The service sends the question to the configured `ChatClient`:

```java
String answer = chatClient.prompt().user(request.question()).call().content();
```

Because the `ChatClient` has `QuestionAnswerAdvisor` registered, the request goes through the retrieval process before the model generates its answer.

The service checks that the returned answer is not null or blank before returning it.

The response contains the original question and the generated answer.

Example response:

```json
{
  "question": "How does Spring Boot configure applications?",
  "answer": "Spring Boot uses auto-configuration to simplify application setup."
}
```

The exact wording of an AI-generated answer may vary.

---

## Complete RAG Workflow

The example demonstrates two separate operations.

### 1. Knowledge Ingestion

The application first receives documents and stores them in the vector store.

```text
Knowledge Documents
        │
        ▼
Embedding Model
        │
        ▼
Vector Embeddings
        │
        ▼
SimpleVectorStore
```

### 2. Question Answering

When a user asks a question, the advisor retrieves relevant documents and augments the prompt.

```text
User Question
      │
      ▼
Embedding Model
      │
      ▼
Query Vector
      │
      ▼
SimpleVectorStore
      │
      ▼
Relevant Documents
      │
      ▼
QuestionAnswerAdvisor
      │
      ▼
Augmented Prompt
      │
      ▼
Llama 3.2
      │
      ▼
Generated Answer
```

Together, these operations form the RAG workflow.

---

## RAG vs Traditional Chat

Traditional chat applications send a question directly to a chat model.

### Traditional Chat

```text
User Question
      │
      ▼
Chat Model
      │
      ▼
Generated Answer
```

The model relies on its pretrained knowledge and the conversation context supplied to it.

### Retrieval-Augmented Generation

```text
User Question
      │
      ▼
Vector Store Search
      │
      ▼
Relevant Documents
      │
      ▼
Augmented Prompt
      │
      ▼
Chat Model
      │
      ▼
Generated Answer
```

RAG adds an explicit retrieval step so the model can use relevant external knowledge when generating an answer.

---

## RAG and Hallucinations

A hallucination occurs when an AI model generates information that is incorrect, unsupported, or fabricated.

RAG can help reduce hallucinations by supplying relevant information from an application's knowledge base.

For example, if the knowledge base contains:

```text
Spring Boot uses auto-configuration to simplify application setup.
```

The application can retrieve this information and include it in the prompt.

However, RAG does not eliminate hallucinations.

The model may still misunderstand the retrieved information, generate unsupported details, or provide an incorrect answer.

The quality of a RAG application depends on several factors:

- Quality and accuracy of the knowledge documents.
- Embedding model quality.
- Retrieval relevance.
- Number of documents retrieved.
- Prompt instructions.
- Chat model capabilities.

For applications requiring stronger grounding, additional measures such as source attribution, relevance thresholds, evaluation, and explicit instructions to abstain when information is unavailable may be necessary.

---

## Application Configuration

The example uses Ollama to run the models locally.

The relevant `application.yml` configuration is:

```yaml
spring:
  application:
    name: rag
  ai:
    model:
      chat: ollama
      embedding: ollama
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.2
        options:
          temperature: 0.2
      embedding:
        model: nomic-embed-text

server:
  port: 8086
```

### Chat Model

The example uses:

```yaml
chat:
  model: llama3.2
```

Llama 3.2 generates natural-language answers using the retrieved context.

### Embedding Model

The example uses:

```yaml
embedding:
  model: nomic-embed-text
```

The embedding model converts documents and questions into vector representations used for similarity search.

### Temperature

The example uses a relatively low temperature:

```yaml
temperature: 0.2
```

A lower temperature generally encourages more predictable and less varied responses.

It does not guarantee factual correctness.

---

## Running Ollama

Install and start Ollama before running the application.

Pull the required models:

```bash
ollama pull llama3.2
ollama pull nomic-embed-text
```

Make sure Ollama is running and accessible at:

```text
http://localhost:11434
```

The application uses the local Ollama service for both chat generation and embedding generation.

---

## Running the Example

From the repository root:

```bash
mvn -pl 10-spring-ai/rag spring-boot:run
```

The application starts on:

```text
http://localhost:8086
```

### Add Knowledge Documents

```bash
curl -X POST http://localhost:8086/api/rag/documents \
  -H "Content-Type: application/json" \
  -d '{
    "documents": [
      {
        "content": "Spring Boot uses auto-configuration to simplify application setup.",
        "metadata": {
          "category": "spring-boot"
        }
      },
      {
        "content": "Spring Security provides authentication and authorization.",
        "metadata": {
          "category": "spring-security"
        }
      }
    ]
  }'
```

### Ask a Question

```bash
curl -X POST http://localhost:8086/api/rag/questions \
  -H "Content-Type: application/json" \
  -d '{
    "question": "How does Spring Boot configure applications?"
  }'
```

The application retrieves relevant information from the vector store and supplies it to the chat model.

---

## Testing Retrieval-Augmented Generation

The example uses Spring Boot testing support, JUnit, Mockito, and MockMvc.

The tests are organized into service, controller, application-context, and integration tests.

### Service Tests

`RagServiceTest` verifies:

- Adding knowledge documents to the vector store.
- Preserving document content and metadata.
- Handling null metadata.
- Returning the number of documents added.
- Generating answers through `ChatClient`.
- Handling null and blank AI responses.
- Propagating vector-store failures.
- Propagating chat-model failures.

These tests verify the service's responsibilities independently of a running Ollama instance.

### Controller Tests

`RagControllerTest` verifies:

- Successful document ingestion.
- Successful question answering.
- Request validation.
- Missing and empty document collections.
- Missing and blank document content.
- Missing and blank questions.
- Mapping service failures to HTTP 502 responses.

The tests use `MockMvc` to exercise the REST API without starting a real HTTP server.

### Application Context Test

`RagApplicationTest` verifies that the Spring application context loads and that the required application components are available.

This helps identify configuration and dependency-injection problems.

### Integration Test

`RagIntegrationTest` verifies the interaction between:

- `SimpleVectorStore`.
- The embedding model.
- `QuestionAnswerAdvisor`.
- `ChatClient`.
- The chat model.

The test uses a real in-memory vector store and deterministic mocked embeddings.

It adds multiple knowledge documents, asks a question, and verifies that the relevant document is retrieved and included in the augmented prompt while unrelated knowledge is excluded.

The chat model is mocked so the test does not depend on a running Ollama instance or nondeterministic AI-generated responses.

This is particularly important because it tests the central RAG retrieval workflow rather than only verifying that individual methods were called.

---

## Important Testing Consideration

The integration test uses deterministic embeddings instead of invoking the actual Ollama embedding model.

For example:

```java
private float[] createEmbedding(String text) {
    if (text.contains("Spring Boot") || text.contains("configure applications")) {
        return new float[]{1.0F, 0.0F};
    }

    if (text.contains("Spring Security")) {
        return new float[]{0.0F, 1.0F};
    }

    throw new IllegalArgumentException("Unexpected text supplied for embedding: " + text);
}
```

This gives the test predictable vector similarity results.

The Spring Boot document and question receive similar embeddings, while the Spring Security document receives a different embedding.

The test can therefore verify retrieval behavior without depending on an external AI service.

The test does not verify the quality of Ollama's actual embeddings or the quality of a real model-generated answer. Those concerns require separate model integration and evaluation tests.

---

## Important Limitations

This example intentionally focuses on the basic RAG workflow.

It does not cover:

- Persistent vector database storage.
- Document ingestion from PDFs, Word documents, or websites.
- Document chunking and splitting.
- Advanced metadata filtering.
- Hybrid search combining keyword and vector search.
- Reranking retrieved documents.
- Conversation memory.
- Source citations and document attribution.
- Retrieval evaluation and relevance thresholds.
- Production deployment and scaling.
- Access control for knowledge documents.

These topics can be explored separately as the RAG implementation becomes more sophisticated.

---

## Key Takeaways

- Retrieval-Augmented Generation combines information retrieval with generative AI.
- RAG allows applications to provide external knowledge to an AI model.
- Embeddings represent text as numerical vectors.
- Vector stores support semantic similarity search.
- `SimpleVectorStore` provides an in-memory vector store implementation.
- `QuestionAnswerAdvisor` retrieves relevant documents and augments the chat prompt.
- `ChatClient` provides a convenient interface for interacting with the chat model.
- `EmbeddingModel` generates vector representations for documents and questions.
- Knowledge ingestion and question answering are separate stages of the RAG workflow.
- RAG can improve grounding but does not guarantee factual accuracy.
- Integration testing can verify retrieval and prompt augmentation without requiring a running AI model.

---

## Next Example

The next example will explore **Tool Calling** in Spring AI, including how AI models can request application-defined functions, how Spring AI executes those tools, and how tool results are incorporated into model responses.
