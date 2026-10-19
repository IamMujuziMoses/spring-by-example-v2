# Spring TestContext Framework

This example demonstrates the **Spring TestContext Framework** and how it integrates with JUnit Jupiter to load and manage a Spring application context during testing.

It focuses on how Spring initializes the test context, injects Spring-managed dependencies into test classes, and simplifies integration testing without manually creating an `ApplicationContext`.

## What This Example Demonstrates

- What the Spring TestContext Framework is.
- Integrating Spring testing support with JUnit Jupiter.
- `SpringExtension`.
- `@ContextConfiguration`.
- Loading an application context from Java-based configuration.
- Injecting Spring-managed beans into test classes.
- Verifying application context initialization.
- Testing Spring-managed components.
- Reusing cached application contexts across compatible tests.

---

## What Is the Spring TestContext Framework?

The Spring TestContext Framework provides infrastructure for testing applications that use the Spring Framework.

It manages the application context required by tests and supports features such as dependency injection, context caching, and integration with testing frameworks.

Without Spring's testing support, a test that requires Spring-managed beans might need to create and close an application context manually.

For example:

```java
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

GreetingService greetingService = context.getBean(GreetingService.class);

assertEquals("Hello, Spring!", greetingService.greet("Spring"));

((AnnotationConfigApplicationContext) context).close();
```

This approach works, but the test is responsible for creating the context and retrieving the bean.

The TestContext Framework allows Spring to manage this setup for the test.

Conceptually:

```text
JUnit Jupiter
      │
      ▼
SpringExtension
      │
      ▼
Spring TestContext Framework
      │
      ▼
ApplicationContext
      │
      ▼
Dependency Injection
      │
      ▼
Test Execution
```

This allows tests to focus on verifying application behavior rather than managing Spring infrastructure manually.

---

## SpringExtension

The `SpringExtension` class integrates Spring's TestContext Framework with JUnit Jupiter.

It allows Spring to participate in the JUnit test lifecycle, including preparing test instances and injecting dependencies.

The example uses:

```java
@ExtendWith(SpringExtension.class)
public class GreetingServiceTest {
}
```

The `@ExtendWith` annotation is a JUnit Jupiter annotation that registers an extension.

`SpringExtension` connects JUnit Jupiter to Spring's testing infrastructure.

This enables the test to use Spring's dependency injection and application context management.

---

## @ContextConfiguration

The `@ContextConfiguration` annotation identifies the configuration Spring should use to load the test application context.

In this example:

```java
@ContextConfiguration(classes = AppConfig.class)
```

The `AppConfig` class provides the bean definitions required by the test.

Spring uses this configuration to create or retrieve the appropriate application context.

The configuration can be supplied through Java configuration classes or other supported configuration sources.

---

## Java-Based Configuration

The example uses Java-based configuration to register the `GreetingService` bean.

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

The `@Bean` annotation tells Spring to register the object returned by `greetingService()` as a bean in the application context.

This bean will be available for injection into the test class.

---

## The Service Being Tested

The example uses a simple service to demonstrate Spring-managed dependency injection.

### GreetingService.java

```java
public class GreetingService {

    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
```

The service accepts a name and returns a greeting.

The implementation is intentionally simple because the main purpose of this example is to demonstrate the testing infrastructure provided by Spring.

---

## Injecting Dependencies into Tests

One of the main benefits of the TestContext Framework is that Spring can inject dependencies directly into test classes.

The test declares the dependencies using `@Autowired`:

```java
@Autowired
private ApplicationContext applicationContext;

@Autowired
private GreetingService greetingService;
```

Spring injects the application context and the `GreetingService` bean when preparing the test instance.

This avoids the need to retrieve the service manually through `applicationContext.getBean(...)`.

The relationship looks like:

```text
AppConfig
    │
    ▼
ApplicationContext
    │
    ├── GreetingService
    │
    └── Dependency Injection
             │
             ▼
      GreetingServiceTest
```

The test can then use the injected service directly.

---

## Application Context Lifecycle

The TestContext Framework manages the application context used by the test.

Conceptually:

```text
Test Execution Begins
        │
        ▼
Load Test Configuration
        │
        ▼
Create or Retrieve ApplicationContext
        │
        ▼
Prepare Test Instance
        │
        ▼
Inject Dependencies
        │
        ▼
Execute Test Methods
        │
        ▼
Complete Test Execution
```

Spring does not necessarily create a completely new application context for every test method.

Compatible tests can share a cached context, reducing the time required to initialize Spring.

The framework manages the context lifecycle according to its testing configuration and context caching rules.

---

## Context Caching

The Spring TestContext Framework supports application context caching.

When multiple tests request compatible application contexts, Spring can reuse a previously loaded context instead of initializing it again.

For example:

```text
GreetingServiceTest
        │
        ▼
ApplicationContext A
        ▲
        │
AnotherCompatibleTest
```

Reusing the context can make test suites faster, particularly when application configuration is expensive to initialize.

Context reuse depends on the configuration and cache key. Tests that require different contexts may need separate instances.

This example introduces context caching conceptually; it does not implement a dedicated context caching test.

---

## SpringExtension vs @SpringJUnitConfig

This example uses `@ExtendWith(SpringExtension.class)` and `@ContextConfiguration` separately to make their responsibilities explicit.

Spring also provides `@SpringJUnitConfig`, a composed annotation that combines these two annotations for JUnit Jupiter tests.

The equivalent declaration is:

```java
@SpringJUnitConfig(AppConfig.class)
public class GreetingServiceTest {
}
```

Both approaches integrate Spring's TestContext Framework with JUnit Jupiter and specify the configuration used for the test context.

This example uses the separate annotations to establish the underlying concepts before exploring the more concise approach in the next example.

---

## Testing Without Spring vs Testing With Spring

Not every test needs the Spring TestContext Framework.

### Plain Unit Test

A plain unit test creates or receives the object being tested without starting a Spring application context.

```java
@Test
void shouldReturnGreeting() {
    GreetingService greetingService = new GreetingService();

    assertEquals("Hello, Spring!", greetingService.greet("Spring"));
}
```

This is appropriate when the behavior can be tested independently of Spring.

### Spring-Managed Test

A Spring-managed test loads the application context and receives its dependencies through Spring.

```java
@Autowired
private GreetingService greetingService;
```

This is useful when verifying bean configuration, dependency injection, or interactions between Spring-managed components.

The choice depends on what the test needs to verify.

---

## Running the Example

From the repository root:

```bash
mvn -pl 05-spring-testing/spring-testcontext-framework -am test
```

This builds the example module and any required Maven modules, then executes its tests.

---

## Running the Tests

To run the tests for this module from the repository root:

```bash
mvn -pl 05-spring-testing/spring-testcontext-framework test
```

Both tests should pass:

- `shouldLoadApplicationContext`
- `shouldInjectGreetingService`

The tests verify that Spring loads the application context, injects the service, and that the service returns the expected greeting.

---

## Key Takeaways

- The Spring TestContext Framework provides infrastructure for tests that require a Spring application context.
- `SpringExtension` integrates Spring's testing infrastructure with JUnit Jupiter.
- `@ContextConfiguration` specifies how the test application context should be loaded.
- Spring can inject dependencies directly into test classes.
- Java-based configuration can define the beans used by a test.
- The framework manages the application context lifecycle for tests.
- Compatible tests can reuse cached application contexts.
- Plain unit tests are preferable when Spring infrastructure is not required.
- `@SpringJUnitConfig` provides a concise alternative to declaring `@ExtendWith(SpringExtension.class)` and `@ContextConfiguration` separately.

---

## What This Example Does Not Cover

This example intentionally focuses on the foundations of the Spring TestContext Framework.

It does not cover:

- `@SpringJUnitConfig`.
- `@SpringBootTest`.
- MVC testing with `MockMvc`.
- Spring Boot test slices.
- Transactional testing.
- Spring Security testing.
- Testcontainers.
- Advanced application context caching and customization.
- Context hierarchies.
- Test execution listeners.

These topics can be explored in dedicated examples throughout the Spring Testing module.

---

## Next Example

The next example will explore **@SpringJUnitConfig**, demonstrating how this composed annotation simplifies configuring Spring-enabled JUnit Jupiter tests.
