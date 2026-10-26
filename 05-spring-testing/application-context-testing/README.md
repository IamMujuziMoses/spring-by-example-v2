# Application Context Testing

This example demonstrates **application context testing** in Spring Boot.

It focuses on how to test Spring configuration in isolation, verify bean registration, and use `ApplicationContextRunner` to test how configuration properties affect the Spring application context.

The example uses a conditional `GreetingService` bean to demonstrate how Spring configuration responds when a property is missing, enabled, or explicitly disabled.

## What This Example Demonstrates

- What application context testing means.
- The Spring application context and bean registration.
- Testing configuration classes in isolation.
- `ApplicationContextRunner`.
- Registering configuration with `withUserConfiguration(...)`.
- Testing conditional bean registration.
- `@Configuration` and `@Bean`.
- `@ConditionalOnProperty`.
- Using `withPropertyValues(...)` to test different configuration scenarios.
- Verifying beans with AssertJ.
- Testing configuration without starting a full Spring Boot application.

---

## What Is Application Context Testing?

The Spring application context is the container responsible for creating, configuring, and managing application beans.

When Spring starts an application context, it processes configuration classes, registers bean definitions, resolves dependencies, and creates beans when required.

Application context testing verifies that this configuration behaves as expected.

For example, an application might register a service only when a particular configuration property is enabled.

A simplified flow looks like:

```text
Configuration Class
        |
        v
Spring Processes Configuration
        |
        v
Configuration Conditions Evaluated
        |
        v
Bean Definitions Registered
        |
        v
Application Context
        |
        v
Assertions Verify Expected Beans
```

Application context tests help identify configuration problems without necessarily starting the entire application.

---

## The Application Context

Spring's application context provides access to the beans managed by Spring.

For example, a configuration class can register a service:

```java
@Configuration(proxyBeanMethods = false)
public class GreetingConfiguration {

    @Bean
    public GreetingService greetingService() {
        return new GreetingService();
    }
}
```

When this configuration is processed, Spring registers a `GreetingService` bean in the application context.

Application code can then obtain the service through dependency injection or by looking it up in the context.

Conceptually:

```text
GreetingConfiguration
        |
        v
@Bean Method
        |
        v
GreetingService Bean
        |
        v
Application Context
        |
        v
Dependency Injection
```

This example uses a configuration class to make bean registration explicit and easy to test.

---

## The GreetingService

The example contains a simple `GreetingService` that generates greeting messages:

```java
public class GreetingService {

    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
```

Calling the service with the name `Moses` returns:

```text
Hello, Moses!
```

The service itself does not depend on Spring. Spring manages it when the configuration registers it as a bean.

This separation keeps the example focused on configuration and application context testing rather than business logic.

---

## Configuring the GreetingService

The `GreetingConfiguration` class registers the service conditionally:

```java
@Configuration(proxyBeanMethods = false)
public class GreetingConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "greeting", name = "enabled", havingValue = "true", matchIfMissing = true)
    public GreetingService greetingService() {
        return new GreetingService();
    }
}
```

The configuration uses three important annotations.

### `@Configuration`

`@Configuration` identifies a class as a source of bean definitions.

Spring processes the class to discover the beans it declares.

The example uses:

```java
@Configuration(proxyBeanMethods = false)
```

Setting `proxyBeanMethods` to `false` avoids generating a configuration-class proxy for inter-bean method calls. This is appropriate here because the configuration methods do not call one another to obtain managed beans.

### `@Bean`

`@Bean` tells Spring that the method produces an object to register in the application context.

In this example, the method returns a new `GreetingService`.

### `@ConditionalOnProperty`

`@ConditionalOnProperty` controls whether the bean definition should be registered based on an environment property.

The example uses:

```java
@ConditionalOnProperty(prefix = "greeting", name = "enabled", havingValue = "true", matchIfMissing = true)
```

The full property name is:

```text
greeting.enabled
```

The `havingValue = "true"` setting means that the condition matches when the property is explicitly set to `true`.

The `matchIfMissing = true` setting means that the condition also matches when the property is absent.

---

## Understanding `greeting.enabled`

The property determines whether Spring registers the `GreetingService` bean.

| Property value | Is the bean registered? |
|----------------|-------------------------|
| Missing        | Yes                     |
| `true`         | Yes                     |
| `false`        | No                      |

### When the property is missing

Because `matchIfMissing = true`, Spring registers the bean even if `greeting.enabled` has not been configured.

This makes the greeting service enabled by default.

### When the property is explicitly enabled

When the property is set to:

```properties
greeting.enabled=true
```

the condition matches, and Spring registers the bean.

### When the property is explicitly disabled

When the property is set to:

```properties
greeting.enabled=false
```

the condition does not match, and Spring does not register the `GreetingService` bean through this configuration.

If another bean requires `GreetingService` as a mandatory dependency, the application context may fail to start unless that dependency is also conditional or otherwise provided.

### Requiring explicit enablement

If the service should only be registered when the property is explicitly set to `true`, change the condition to:

```java
@ConditionalOnProperty(prefix = "greeting", name = "enabled", havingValue = "true")
```

The default for `matchIfMissing` is `false`, so omitting it means that a missing property does not satisfy the condition.

This example intentionally uses `matchIfMissing = true` so that the tests can demonstrate both default behavior and explicit configuration.

---

## Why Use `ApplicationContextRunner`?

A full `@SpringBootTest` can be useful when the behavior depends on the complete application, but it may load much more infrastructure than a focused configuration test needs.

`ApplicationContextRunner` is a better fit when the primary question is:

> Given this configuration and these properties, which beans should Spring register?

It offers several advantages:

- **Isolation:** tests only the configuration relevant to the scenario.
- **Speed:** avoids starting the entire application unnecessarily.
- **Clarity:** each test can specify its own property values.
- **Repeatability:** each runner invocation gets a separately managed context.
- **Failure visibility:** assertions can verify both successful startup and expected bean registration.

It is not a replacement for every kind of Spring test. Applications still benefit from full-context integration tests when interactions between multiple components or application infrastructure need verification.

---

## Application Context Testing vs `@SpringBootTest`

Both approaches can test Spring-managed components, but they serve different purposes.

| Feature                  | `ApplicationContextRunner`               | `@SpringBootTest`                                                    |
|--------------------------|------------------------------------------|----------------------------------------------------------------------|
| Primary purpose          | Focused configuration testing            | Testing the application with a Spring Boot context                   |
| Configuration scope      | Explicitly supplied configuration        | Usually the application's primary configuration                      |
| Property scenarios       | Easy to vary per runner invocation       | Commonly configured through test properties or other test mechanisms |
| Full application startup | Not required                             | Loads a Spring Boot application context                              |
| Typical use              | Conditional beans and auto-configuration | Integration tests and application-level behavior                     |

Choose the approach that matches the question the test is intended to answer.

---

## Application Context Testing Flow

A simplified view of this example is:

```text
ApplicationContextRunner
        |
        v
Load GreetingConfiguration
        |
        v
Set Property Values
        |
        v
Evaluate @ConditionalOnProperty
        |
        +-- Property Missing
        |       |
        |       v
        |   Register GreetingService
        |
        +-- Property = true
        |       |
        |       v
        |   Register GreetingService
        |
        +-- Property = false
                |
                v
            Do Not Register GreetingService
        |
        v
Assert Expected Context State
```

The test verifies that the context reflects the configuration conditions for each scenario.

---

## Running the Tests

From the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 05-spring-testing/application-context-testing test
```

To build the module along with its required Maven reactor projects, run:

```bash
mvn -pl 05-spring-testing/application-context-testing -am clean test
```

This example does not require Docker or a running database.

---

## Key Takeaways

- The Spring application context manages application beans and their dependencies.
- Application context testing verifies how Spring configuration behaves.
- `ApplicationContextRunner` creates isolated contexts for focused tests.
- `withUserConfiguration(...)` specifies the configuration under test.
- `withPropertyValues(...)` makes it easy to test different configuration scenarios.
- `@ConditionalOnProperty` controls configuration based on environment properties.
- `matchIfMissing = true` allows a condition to match when the property is absent.
- An explicitly disabled conditional bean is not registered through the corresponding configuration.
- AssertJ provides useful assertions for verifying context startup and bean registration.
- Focused context tests complement, rather than replace, full application integration tests.

---

## What This Example Does Not Cover

This example intentionally focuses on isolated configuration testing.

It does not cover:

- Custom application context initializers.
- Complex auto-configuration ordering.
- `@ConfigurationProperties` validation.
- Full application integration testing.
- Web application context testing.
- Context caching behavior.
- Testing external services or databases.
- Bean lifecycle callbacks.
- Complex conditional configuration involving multiple dependencies.

These topics can be explored in separate examples.

---

## Next Example

The next example will explore **Context caching**, including how Spring's TestContext Framework reuses application contexts between tests and how context configuration affects test execution time.
