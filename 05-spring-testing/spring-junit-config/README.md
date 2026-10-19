# Spring JUnit Config

This example demonstrates how to use Spring's `@SpringJUnitConfig` annotation to configure Spring integration tests with JUnit Jupiter.

## Overview

Spring's TestContext Framework provides support for integration testing by loading and managing an application context during test execution. It allows tests to use Spring-managed beans and application configuration without manually creating the context.

The `@SpringJUnitConfig` annotation simplifies test configuration by combining two annotations:

- `@ExtendWith(SpringExtension.class)` integrates Spring's TestContext Framework with JUnit Jupiter.
- `@ContextConfiguration` specifies the configuration classes or resources used to load the application context.

Instead of declaring both annotations separately, `@SpringJUnitConfig` provides a concise alternative.

---

## Learning Objectives

By completing this example, you will learn how to:

- Configure Spring integration tests using `@SpringJUnitConfig`.
- Load an application context from Java-based configuration.
- Inject Spring-managed beans into a test class.
- Verify that the application context loads successfully.
- Test the behavior of a Spring-managed service.
- Understand how `@SpringJUnitConfig` relates to the Spring TestContext Framework.

---

## Application Configuration

The example uses Java-based configuration to define the beans available in the Spring application context.

### AppConfig.java

```java
@Configuration
public class AppConfig {

    @Bean
    public GreetingService greetingService() {
        return new GreetingService();
    }
}
```

The `@Configuration` annotation identifies the class as a source of bean definitions.

The `@Bean` annotation registers the returned `GreetingService` instance in the Spring application context.

When the test loads `AppConfig`, Spring creates and manages the configured bean.

---

## Service Implementation

The `GreetingService` class provides a simple greeting operation.

### GreetingService.java

```java
public class GreetingService {

    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
```

The service keeps the example focused on Spring test configuration rather than application complexity.

---

## Using @SpringJUnitConfig

The `@SpringJUnitConfig` annotation combines Spring's JUnit Jupiter extension with application context configuration.

### Traditional annotation approach

Before `@SpringJUnitConfig`, a test could declare the two annotations separately:

```java
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
public class GreetingServiceTest {
}
```

Each annotation serves a distinct purpose:

- `@ExtendWith(SpringExtension.class)` connects JUnit Jupiter to Spring's testing support.
- `@ContextConfiguration(classes = AppConfig.class)` specifies the configuration used to create the test application context.

### Simplified annotation approach

With `@SpringJUnitConfig`, both declarations can be replaced with one annotation:

```java
@SpringJUnitConfig(classes = AppConfig.class)
public class GreetingServiceTest {
}
```

The `classes` attribute specifies the Java configuration class Spring should use when loading the application context.

This reduces annotation boilerplate while retaining the underlying Spring TestContext Framework functionality.

---

## Test Execution Flow

When JUnit Jupiter executes `GreetingServiceTest`, the following steps occur:

1. JUnit discovers the test class.
2. Spring's `SpringExtension` integrates the test with the Spring TestContext Framework.
3. Spring loads the application context using `AppConfig`.
4. Spring creates the configured `GreetingService` bean.
5. Spring injects the required dependencies into the test instance.
6. JUnit executes the test methods.
7. The assertions verify context initialization, dependency injection, and service behavior.

The TestContext Framework manages the context lifecycle and can reuse compatible application contexts across tests, reducing the overhead of repeatedly initializing Spring.

---

## Running the Example

Run the example from the repository root.

### Run the example and required modules

```bash
mvn -pl 05-spring-testing/spring-junit-config -am clean verify
```

The `-pl` option selects the example module, while `-am` also builds the modules it depends on.

### Run tests for this module only

```bash
mvn -pl 05-spring-testing/spring-junit-config test
```

This command assumes the required project dependencies are already available in the local Maven repository.

---

## Expected Results

Both tests should pass:

| Test                           | Expected Result                                       |
|--------------------------------|-------------------------------------------------------|
| `shouldLoadApplicationContext` | The application context is not null.                  |
| `shouldInjectGreetingService`  | The service is injected and returns `Hello, Spring!`. |

The Maven build should complete successfully when the project is configured correctly.

---

## @SpringJUnitConfig vs. @SpringBootTest

These annotations serve different testing needs.

| Annotation           | Purpose                                                                             |
|----------------------|-------------------------------------------------------------------------------------|
| `@SpringJUnitConfig` | Loads a Spring test context using specified configuration classes or resources.     |
| `@SpringBootTest`    | Loads a Spring Boot application context using Spring Boot's testing infrastructure. |

Use `@SpringJUnitConfig` when you want to test Spring-managed components with explicit configuration and do not need Spring Boot's full application bootstrapping.

Use `@SpringBootTest` when you want to test the application using Spring Boot's configuration and startup behavior.

---

## Key Takeaways

- `@SpringJUnitConfig` combines `@ExtendWith(SpringExtension.class)` and `@ContextConfiguration`.
- It provides a concise way to configure Spring integration tests with JUnit Jupiter.
- The `classes` attribute identifies the Java configuration used to load the application context.
- Spring-managed beans can be injected into tests using `@Autowired`.
- The Spring TestContext Framework manages the test context lifecycle and supports context caching.
- `@SpringJUnitConfig` is useful for focused Spring integration tests that do not require full Spring Boot application startup.

---

## Out of Scope

This example focuses on `@SpringJUnitConfig` and Java-based configuration. The following topics are covered separately:

- Spring Boot integration testing with `@SpringBootTest`.
- MVC testing and `MockMvc`.
- Test slices.
- Transaction testing.
- Security testing.
- Testcontainers.
- Application context caching in greater detail.

---

## Next Example

Continue to the **`@SpringBootTest`** example to learn how to load and test a Spring Boot application context.
