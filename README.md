# Spring by Example V2

A focused collection of **small but extensive examples** covering the modern Spring ecosystem.

**Spring by Example V2** builds on the foundations established in [Spring by Example V1](https://github.com/IamMujuziMoses/spring-by-example).

V1 focused on learning the Spring Framework itself, understanding its internals, and seeing Spring concepts applied through OpenMRS examples.

V2 moves outward into the broader Spring ecosystem and focuses on technologies commonly used to build production-oriented Spring applications.

---

![Java](https://img.shields.io/badge/Java-21-red?style=for-the-badge&logo=openjdk)
![Spring](https://img.shields.io/badge/Spring-Framework-orange?style=for-the-badge&logo=spring&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge&logo=coveralls)
![Contributions Welcome](https://img.shields.io/badge/Contributions-Welcome-green?style=for-the-badge&logo=github)
![Coverage](https://img.shields.io/badge/coverage-00%25-blue?style=for-the-badge&logo=codecov&logoColor=white)

---

## Guiding Principle

> **Small examples, extensive coverage.**

Each example should be:

- Small enough to understand in isolation.
- Focused on one concept or closely related group of concepts.
- Runnable and testable.
- Clearly documented.
- Detailed enough to explain important behavior.
- Connected to the larger Spring ecosystem.

---

## Project Goals

- ✅ Explore the major Spring ecosystem projects.
- ✅ Understand how the technologies fit together.
- ✅ Build practical, runnable examples.
- ✅ Practice production-oriented Spring development.
- ✅ Connect V2 concepts with the foundations learned in V1.
- ✅ Build a reusable reference for future Spring development.

---

## Modules

| Module | Topic                | Status          |
|--------|----------------------|-----------------|
| 01     | Spring Security      | 🚧  In Progress |
| 02     | Spring Data          | ⬜  Planned      |
| 03     | Reactive Spring      | ⬜  Planned      |
| 04     | Spring Modulith      | ⬜  Planned      |
| 05     | Spring Testing       | ⬜  Planned      |
| 06     | Capstone Application | ⬜  Planned      |
| 07     | Spring Batch         | ⬜  Planned      |
| 08     | Spring Integration   | ⬜  Planned      |
| 09     | Spring Cloud         | ⬜  Planned      |
| 10     | Spring AI            | ⬜  Planned      |
| 11     | GraalVM Native       | ⬜  Planned      |
| 12     | Kotlin               | ⬜  Planned      |

---

## Learning Path

```text
Spring Security
       ↓
Spring Data
       ↓
Reactive Spring
       ↓
Spring Modulith
       ↓
Spring Testing
       ↓
Capstone Application
       ↓
Spring Batch
       ↓
Spring Integration
       ↓
Spring Cloud
       ↓
Spring AI
       ↓
GraalVM Native
       ↓
Kotlin
```

---

### Module 01 — Spring Security 🚧

Explore application security with Spring Security.

- SecurityFilterChain
- Authentication
- UserDetailsService
- Password encoding
- Authorization
- SecurityContext
- Method security
- CSRF
- Session management
- OAuth2
- JWT
- Security testing

---

### Module 02 — Spring Data

Explore database access and repository abstractions.

- Spring Data repositories
- CrudRepository
- Query methods
- `@Query`
- Entity mapping
- Relationships
- Transactions
- Pagination
- Specifications
- Projections
- Auditing
- Custom repositories
- Database testing

---

### Module 03 — Reactive Spring

Explore reactive programming with Spring.

- Reactor
- `Mono`
- `Flux`
- Reactive pipelines
- Backpressure
- WebFlux
- Reactive controllers
- Reactive clients
- Reactive data access
- Error handling
- Reactive testing

---

### Module 04 — Spring Modulith

Explore modular monolith architecture with Spring Modulith.

- Application modules
- Module boundaries
- Module dependencies
- Application events
- Event externalization
- Module verification
- Documentation
- Module testing

---

### Module 05 — Spring Testing

Explore testing strategies for Spring applications.

- Spring TestContext Framework
- `@SpringJUnitConfig`
- `@SpringBootTest`
- MVC testing
- Test slices
- MockMvc
- Transaction testing
- Security testing
- Integration testing
- Testcontainers
- Application context testing
- Context caching

---

### Module 06 — Capstone Application

Combine concepts from the first five modules into a realistic Spring application.

- Spring Boot
- Spring Security
- Spring Data
- REST APIs
- Transactions
- Modular architecture
- Testing
- Integration between the technologies

> The application should remain understandable rather than becoming a large production system.

---

### Module 07 — Spring Batch

Explore batch processing with Spring Batch.

- Jobs
- Steps
- Job parameters
- Item readers
- Item processors
- Item writers
- Chunk processing
- Tasklets
- Job execution
- Restartability
- Skip and retry
- Batch testing

---

### Module 08 — Spring Integration

Explore enterprise integration patterns.

- Message channels
- Message endpoints
- Message handlers
- Transformers
- Filters
- Routers
- Service activators
- Gateways
- Adapters
- Error channels
- Integration testing

---

### Module 09 — Spring Cloud

Explore distributed application patterns using Spring Cloud.

- Service discovery
- Configuration management
- API gateways
- Load balancing
- Resilience
- Distributed tracing
- Declarative HTTP clients
- Messaging
- Cloud-native configuration

> The exact technology choices will be evaluated when this module begins.

---

### Module 10 — Spring AI

Explore Spring-based AI application development.

- Spring AI fundamentals
- Chat models
- Prompt templates
- Structured output
- Embeddings
- Vector stores
- Retrieval-Augmented Generation
- Tool calling
- AI application testing
- Model provider integration

---

### Module 11 — GraalVM Native
 
Explore native compilation and Spring AOT capabilities.

- Native executables
- Spring AOT
- Native image constraints
- Reflection
- Resource configuration
- Runtime hints 
- Build-time processing 
- Native testing 
- Containerized native applications

---

### Module 12 — Kotlin

Explore Kotlin development with Spring.

- Kotlin fundamentals for Spring developers 
- Kotlin classes and data classes 
- Null safety 
- Extension functions 
- Kotlin configuration 
- Dependency injection with Kotlin 
- Kotlin and Spring Boot 
- Kotlin testing 
- Coroutines 
- Kotlin with WebFlux

---

## Technology Baseline

The project currently targets:

- Java 21
- Spring Framework 7
- Spring Boot 4
- Maven
- JUnit Jupiter

> Individual modules may introduce additional dependencies.

---

## Getting Started

Clone the repository:

```bash
git clone https://github.com/<your-username>/spring-by-example-v2.git
```

Choose any module and run it independently using Maven.

For example:

```bash
cd 01-spring-security/security-filter-chain
mvn clean test
```

Each module contains:

- A focused example
- Source code
- Unit tests
- Detailed documentation
- Suggested next steps

---

Modules will follow a numbered structure:

```text
01-spring-security/
02-spring-data/
03-reactive-spring/
...
```

---

## Status

**Spring by Example V2 is currently under development.**

Modules will be added incrementally as each topic is explored and documented.

---

## Roadmap

The project's progress is tracked in [ROADMAP.md](ROADMAP.md), where you can see completed modules, upcoming topics, and future plans.

---

## License

This project is licensed under the MIT License.

See the [LICENSE](LICENSE) file for details.

---

## Contributing

Contributions are welcome!

If you'd like to improve an example, fix an issue, or add a new learning module, please read the [CONTRIBUTING.md](CONTRIBUTING.md) guide before opening a pull request.

---

## Contributors

Thank you to everyone who has contributed! Here are the people who have helped so far:

<a href="https://github.com/IamMujuziMoses/spring-by-example-v2/graphs/contributors">
    <img src="https://contrib.rocks/image?repo=IamMujuziMoses/spring-by-example-v2" />
</a>

#### [Back To Top ⬆️](#spring-by-example-v2)
