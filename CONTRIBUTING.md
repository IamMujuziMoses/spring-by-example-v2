# Contributing to Spring by Example V2

Thank you for contributing to **Spring by Example V2**.

The project focuses on building **small but extensive examples** across the Spring ecosystem.

The goal is not to build large applications, but to create focused examples that make individual Spring concepts easy to understand, run, test, and explore.

---

## Quick Start

Clone the repository:

```bash
git clone https://github.com/<your-username>/spring-by-example-v2.git
```

Navigate to an example:

```bash
cd 01-spring-security/security-filter-chain
```

Run the example:

```bash
mvn spring-boot:run
```

Explore the source code, tests, and `README.md`.

---

## Development Principles

Examples should be:

- Small and focused.
- Easy to run.
- Easy to understand.
- Well tested.
- Clearly documented.
- Consistent with the repository structure.
- Focused on demonstrating a specific concept.

Avoid unnecessary complexity just to demonstrate a feature.

When there is a choice between a highly abstract implementation and a straightforward implementation that makes the concept easier to understand, prefer the straightforward implementation.

---

## Before You Start

Check [ROADMAP.md](ROADMAP.md) before starting work.

Make sure the topic or example you want to add has not already been completed.

Please also check existing issues and pull requests before creating a new example.

If you plan to add a large feature, a new module, or multiple related examples, consider opening an issue first to discuss the idea.

---

## Module Structure

Modules should use numbered directories:

```text
01-spring-security/
02-spring-data/
03-reactive-spring/
04-spring-modulith/
05-spring-testing/
```

A typical example should contain:

```text
01-spring-security/
├── pom.xml
├── security-filter-chain/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   └── README.md
└── ...
```

Each example should contain a `README.md`.

Each completed example README should explain, where applicable:

- What the technology or concept is.
- Why it is useful.
- The concepts demonstrated.
- How the example works.
- How to run it.
- How it is tested.
- Important takeaways.

### README Validation

CI automatically checks that every directory containing Java source has a non-empty `README.md`.

A pull request will fail validation if an example:

- Does not contain a `README.md`.
- Contains an empty `README.md`.

---

## Java Version

The project uses **Java 21**.

Unless a module specifically requires another language or runtime, Java examples should target Java 21.

Use modern Java features where they improve readability, but avoid introducing complexity that distracts from the Spring concept being demonstrated.

---

## Code Style

The project uses **Checkstyle** to maintain consistent Java source code.

Before submitting a pull request, make sure your code follows these rules.

### Naming

Use standard Java naming conventions:

- Classes and interfaces use `PascalCase`.
- Methods use `camelCase`.
- Fields use `camelCase`.
- Parameters use `camelCase`.
- Local variables use `camelCase`.
- Constants use `UPPER_SNAKE_CASE`.

Test method names may use underscores when they make the behavior being tested clearer.

For example:

```java
void saveUser_shouldPersistUser()
```

### Imports

Imports should:

- Be explicit.
- Not use wildcard imports.
- Be alphabetically ordered within their group.
- Be separated into the configured import groups.

Avoid:

```java
import java.util.*;
```

Prefer:

```java
import java.util.List;
import java.util.Map;
```

### Formatting

The Checkstyle configuration enforces:

- Maximum line length of **125 characters**.
- No tab characters.
- Consistent whitespace.
- Consistent modifier ordering.
- Braces around control statements.
- Blank lines between declarations where required.
- One top-level class per source file.
- No unnecessary modifiers.
- No unused imports.
- Consistent declaration ordering.
- No multiple variable declarations in a single statement.

### Example

Prefer:

```java
if (authenticated) {
    return "Authenticated";
}
```

instead of:

```java
if (authenticated)
    return "Authenticated";
```

---

## Java Documentation

Every Java source file must contain an `@author` Javadoc tag.

For example:

```java
/**
 * Demonstrates Spring Security authentication.
 *
 * @author <your-name>
 */
public class AuthenticationExample {
}
```

This requirement is automatically validated by CI.

A pull request will fail if a Java source file is missing an `@author` tag.

---

## Testing

Examples should include tests for meaningful behavior.

Tests should demonstrate the behavior of the concept being taught rather than simply increasing coverage.

For example, if an example demonstrates authorization, tests should cover relevant authorized and unauthorized scenarios.

Use descriptive test names:

```java
@Test
void authenticatedUser_shouldAccessProtectedEndpoint() {
    // ...
}
```

---

## Verification

Before submitting a pull request, run the complete Maven verification:

```bash
mvn clean verify
```

### Docker-Dependent Tests

Some examples use `Testcontainers` to run integration tests against containerized services.

These tests may require Docker to be installed and running. Docker-dependent tests are skipped by default so contributors can build the repository without Docker.

#### Spring Testcontainers Example

To include the `spring-test-containers` example tests, start Docker and execute:

```bash
mvn clean verify -DskipTestcontainersTests=false
```

Or run only the `spring-test-containers` example tests:

```bash
mvn -pl 05-spring-testing/spring-test-containers -am clean verify -DskipTestcontainersTests=false
```

#### Spring Cloud Messaging Example

The Spring Cloud Messaging example uses `Testcontainers` to start a `RabbitMQ` container for integration testing.

The `MessagingIntegrationTest` class is skipped by default using the `skipMessagingIntegrationTest` property.

To include the `MessagingIntegrationTest` class, start Docker and execute:

```bash
mvn clean verify -DskipMessagingIntegrationTest=false
```

Or run only the Spring Cloud Messaging example:

```bash
mvn -pl 09-spring-cloud/messaging/msg-integration-tests -am clean verify -DskipMessagingIntegrationTest=false
```

During execution, Testcontainers starts RabbitMQ, the Order Service and Inventory Service connect to the containerized message broker, and the integration test verifies asynchronous event delivery and inventory updates.

#### Running All Docker-Dependent Tests

To enable both the Spring Testcontainers tests and the Spring Cloud Messaging integration test during the complete Maven verification, start Docker and execute:

```bash
mvn clean verify -DskipTestcontainersTests=false -DskipMessagingIntegrationTest=false
```

When modifying Docker-dependent examples, contributors should run the relevant tests with Docker available before submitting their changes.

This runs the project's build, tests, Checkstyle validation, and other configured verification steps.

You should also make sure the example can be started independently when applicable:

```bash
mvn spring-boot:run
```

### End-to-End Application Verification Tests

Some examples use end-to-end tests to verify application behavior through external HTTP requests.

These tests depend on a separately running application and are skipped by default during the regular Maven build. This allows contributors to execute `mvn clean verify` without having to start the dependent application.

To run the End-to-End Application Verification tests locally, first start the corresponding Integration Between Spring Technologies application:

```bash
mvn -pl 06-capstone-application/integration-between-spring-technologies \
org.springframework.boot:spring-boot-maven-plugin:run
```

In another terminal, execute the end-to-end tests

```bash
mvn -pl 06-capstone-application/end-to-end-application-verification \
-Dskip.e2e.tests=false test
```

The tests communicate with the running application through HTTP. Make sure the application is available at `http://localhost:8080` before executing them.

When modifying end-to-end verification examples, contributors should start the corresponding application and run the relevant tests before submitting their changes.

The dedicated GitHub Actions workflow automatically starts the Integration Between Spring Technologies application and executes the end-to-end verification tests.

### Kubernetes-Dependent Tests

Some examples use Kubernetes to provide external configuration, infrastructure, or application services.

These tests require a running Kubernetes environment and may depend on deployed applications and Kubernetes resources.

Kubernetes-dependent tests are skipped by default so contributors can run the regular Maven build without requiring a Kubernetes cluster.

#### Spring Cloud-Native Configuration Example

The Spring Cloud-Native Configuration example uses Spring Cloud Kubernetes to load external configuration from Kubernetes `ConfigMaps` and `Secrets`.

The `KubernetesConfigurationIT` test verifies the application's effective configuration through HTTP requests to the deployed application.

This test is skipped by default using the `skipCloudNativeConfigurationITs` Maven property.

To include the Kubernetes integration test, start a Kubernetes cluster, deploy the example, and execute:

```bash
mvn clean verify -DskipCloudNativeConfigurationITs=false
```

Or run only the Cloud-Native Configuration example tests:

```bash
mvn -pl :cnc-integration-tests -am clean verify -DskipCloudNativeConfigurationITs=false
```

The Kubernetes cluster and application must already be running and accessible. For the local Kind setup, contributors should deploy the Kubernetes manifests and establish port forwarding:

```bash
kubectl port-forward -n cloud-native-configuration service/cloud-native-config-client 8080:8080
```

The application is expected to be available at:

```text
http://localhost:8080
```

During execution, the test sends an HTTP request to the application and verifies that configuration values loaded from the Kubernetes `ConfigMap` and `Secret` are correctly reflected in the application response.

For a detailed explanation of the test configuration, execution modes, module responsibilities, and Kubernetes setup, see [Managing Kubernetes-Dependent Tests](09-spring-cloud/cloud-native-configuration/README.md#managing-kubernetes-dependent-tests).

When modifying Kubernetes-dependent examples, contributors should deploy the relevant application and run the corresponding tests with Kubernetes available before submitting their changes.

#### Running All Optional Infrastructure-Dependent Tests

To enable the Spring Testcontainers tests, Spring Cloud Messaging integration test, and Cloud-Native Configuration Kubernetes integration test during the complete Maven verification, start Docker and ensure the required Kubernetes environment is available.

Execute:

```bash
mvn clean verify -DskipTestcontainersTests=false -DskipMessagingIntegrationTest=false -DskipCloudNativeConfigurationITs=false
```

Each example may have additional infrastructure requirements. Ensure all required containers, services, and Kubernetes applications are running before executing the tests.

---

### GitHub Actions

Pull requests and pushes to `main` run the repository's automated validation workflows.

The validation includes:

- Maven build and tests.
- Checkstyle.
- README validation.
- Java `@author` validation.

A pull request should pass these checks before it is considered ready for review.

---

## Branch Naming Convention

Use lowercase letters and separate words with hyphens (`-`).

Branches should follow this general format:

```text
<type>/<short-description>
```

### Feature Branches

For new examples, features, or learning material:

```text
feature/add-bean-lifecycle-example
feature/dependency-injection
feature/add-spring-data-example
```

### Documentation Branches

For documentation changes:

```text
docs/improve-readme
docs/add-contributing-guide
docs/update-learning-path
```

### Bug Fix Branches

For fixing issues:

```text
fix/broken-example-test
fix/update-spring-version
fix/maven-build
```

### Refactoring Branches

For code or project structure refactoring:

```text
refactor/maven-parent-structure
refactor/security-configuration
```

---

## Good Branch Name Examples

```text
feature/add-bean-lifecycle-example
feature/add-security-testing-example
docs/update-learning-path
refactor/maven-parent-structure
fix/update-spring-version
```

Keep branch names short, descriptive, and focused on the change being made.

---

## Commit Message Convention

Use clear, descriptive commit messages.

The preferred format is:

```text
(type): Description
```

Examples:

```text
(feature): Added Spring Security authentication example
(feature): Added Spring Data repository example
(fix): Fixed Maven parent configuration
(docs): Updated Spring Security module README
(refactor): Simplified security configuration
```

For new learning examples, describe the Spring concept being demonstrated.

For example:

```text
(feature): Added Spring Security method security example
```

is preferred over:

```text
(feature): Added new files
```

---

## Pull Requests

Pull requests should explain:

- What was added or changed.
- Which concepts are demonstrated.
- How the change was tested.
- Any important implementation decisions.
- Any relevant limitations or follow-up work.

Keep pull requests focused on one example, module, or closely related change.

### Before Opening a Pull Request

Make sure:

- The example follows the repository structure.
- The example has a non-empty `README.md`.
- Every Java file contains an `@author` tag.
- Tests cover the meaningful behavior.
- Checkstyle passes.
- `mvn clean verify` succeeds.
- The example can be run when applicable.
- `ROADMAP.md` reflects the current status.

---

## Updating the Roadmap

When completing an example or module, update [ROADMAP.md](ROADMAP.md) to reflect its new status.

This helps contributors see what has already been completed and prevents duplicate work.

For example, when an example is completed:

```text
- [x] Spring Security
```

When work is still in progress:

```text
- 🚧 Spring Data
```

Keep the roadmap synchronized with the actual state of the repository.

---

## Reporting Issues

When reporting an issue, include:

- The module affected.
- The example affected.
- Steps to reproduce the problem.
- Expected behavior.
- Actual behavior.
- Relevant error messages or stack traces.
- Java and Maven versions when relevant.

A small, reproducible example is especially helpful.

---

## Learning First

This repository is primarily a learning project.

When adding an example, prefer an implementation that makes the underlying concept easy to understand over one that hides everything behind framework defaults.

The examples should help someone answer not only:

> "How do I use this?"

but also:

> "How does this work?"

and:

> "Why does this work this way?"

---

Thank you for helping make **Spring by Example V2** better!

> **Small examples, extensive coverage.**

#### [Back To Top ⬆️](#contributing-to-spring-by-example-v2)