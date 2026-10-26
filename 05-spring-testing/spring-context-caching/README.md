# Context Caching

This example demonstrates **application context caching** in the Spring TestContext Framework.

It focuses on how Spring caches and reuses application contexts across tests, how test configuration affects context reuse, and how `@DirtiesContext` invalidates a cached context.

## What This Example Demonstrates

- What application context caching means in Spring testing.
- How Spring reuses contexts for compatible test configurations.
- How different configurations result in separate application contexts.
- The role of the Spring TestContext Framework.
- Accessing application contexts through `TestContextManager`.
- Invalidating a cached context with `@DirtiesContext`.
- The performance benefits of context caching.
- Testing context reuse and invalidation.

---

## What Is Application Context Caching?

An application context is the Spring container responsible for creating, configuring, and managing application beans.

Creating an application context can involve component scanning, configuration processing, bean creation, dependency injection, and other initialization work.

During testing, repeatedly creating the same context can make a test suite unnecessarily slow.

The Spring TestContext Framework addresses this by caching application contexts and reusing them across tests when their configuration is compatible.

A simplified flow looks like:

```text
Test Class A
     │
     ▼
Application Context
     │
     ▼
Spring Context Cache
     ▲
     │
Test Class B
```

When both test classes use compatible context configuration metadata, Spring can reuse the same application context instead of creating another one.

---

## Spring TestContext Framework

The Spring TestContext Framework provides support for integrating the Spring application context with testing frameworks such as JUnit Jupiter.

It manages test context initialization, application context loading, test instance preparation, and other testing lifecycle operations.

Context caching is one of its important features.

Conceptually:

```text
JUnit Test
    │
    ▼
TestContext Framework
    │
    ▼
Context Configuration
    │
    ▼
Context Cache
    │
    ├── Cached Context Found
    │       │
    │       ▼
    │   Reuse Context
    │
    └── No Cached Context
            │
            ▼
       Load Context
            │
            ▼
       Cache Context
```

The cache avoids unnecessary context initialization when a compatible context is already available.

---

## Context Reuse

The example defines two test fixture classes that use the same configuration:

```java
@ContextConfiguration(classes = SharedConfiguration.class)
static class SharedContextTestOne {
}

@ContextConfiguration(classes = SharedConfiguration.class)
static class SharedContextTestTwo {
}
```

Both classes reference `SharedConfiguration`.

The test obtains an application context for each fixture:

```java
ApplicationContext firstContext = loadContext(SharedContextTestOne.class);
ApplicationContext secondContext = loadContext(SharedContextTestTwo.class);

assertSame(firstContext, secondContext);
```

The assertion verifies that both fixtures receive the same application context instance.

Conceptually:

```text
SharedContextTestOne
          │
          ▼
   SharedConfiguration
          │
          ▼
    ApplicationContext
          ▲
          │
   SharedConfiguration
          ▲
          │
SharedContextTestTwo
```

Because the configuration metadata is compatible, Spring can reuse the cached context.

This reduces repeated initialization work across a test suite.

---

## How Spring Determines Context Reuse

Spring does not determine context reuse merely by comparing test class names.

It uses context configuration metadata to determine whether an existing cached context can be reused.

The context cache key can incorporate information such as:

- Configuration classes and locations.
- Context initializers and customizers.
- Active profiles.
- Test property sources.
- Parent contexts.
- Web resource configuration.
- Context loader configuration.

The exact cache key depends on the configuration options used by the tests.

For example:

```java
@ContextConfiguration(classes = SharedConfiguration.class)
```

and:

```java
@ContextConfiguration(classes = AlternativeConfiguration.class)
```

declare different configuration classes.

Even if both configurations define beans of the same type, their different configuration metadata can result in separate application contexts.

---

## Different Context Configurations

The example defines an alternative configuration:

```java
@Configuration(proxyBeanMethods = false)
static class AlternativeConfiguration {

    @Bean
    ContextMarker contextMarker() {
        return new ContextMarker();
    }
}
```

A test fixture uses this configuration:

```java
@ContextConfiguration(classes = AlternativeConfiguration.class)
static class AlternativeContextTest {
}
```

The test compares its context with the context associated with `SharedConfiguration`:

```java
ApplicationContext sharedContext = loadContext(SharedContextTestOne.class);
ApplicationContext alternativeContext = loadContext(AlternativeContextTest.class);

assertNotSame(sharedContext, alternativeContext);
```

The assertion verifies that the two contexts are different instances.

Conceptually:

```text
SharedConfiguration
        │
        ▼
  ApplicationContext A


AlternativeConfiguration
        │
        ▼
  ApplicationContext B
```

The two configurations produce separate context cache entries.

This is useful when tests need different bean definitions, profiles, properties, or other configuration settings.

---

## Accessing the Application Context

The example uses `TestContextManager` to access the context associated with a test fixture.

A helper method performs this operation:

```java
private ApplicationContext loadContext(Class<?> testClass) {
    return new TestContextManager(testClass).getTestContext().getApplicationContext();
}
```

`TestContextManager` manages the Spring TestContext lifecycle for the specified test class.

Calling `getApplicationContext()` obtains the context associated with that test context, loading it if necessary.

When a compatible context is already cached, Spring can return the existing instance.

This helper makes context reuse observable without requiring separate JUnit test classes to execute in a particular order.

---

## Understanding `@DirtiesContext`

The `@DirtiesContext` annotation tells Spring that a test has modified or corrupted the application context in a way that makes it unsuitable for reuse.

Spring removes the affected context from the cache. When a subsequent test requires a context with the same configuration, Spring loads a new instance.

For example:

```java
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = DirtyConfiguration.class)
static class DirtyContextTest {
}
```

The `AFTER_CLASS` mode marks the context as dirty after the test class has completed.

The lifecycle is conceptually:

```text
Test Class Starts
        │
        ▼
Application Context Loaded
        │
        ▼
Tests Execute
        │
        ▼
Test Class Completes
        │
        ▼
@DirtiesContext
        │
        ▼
Context Removed From Cache
        │
        ▼
A Later Test Needs the Context
        │
        ▼
New Context Loaded
```

Invalidating a context does not mean that every context in the cache is discarded. Spring invalidates the context associated with the relevant test configuration and follows the applicable context hierarchy rules.

---

## Testing Context Invalidation

The example uses a dedicated fixture with `@DirtiesContext`:

```java
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = DirtyConfiguration.class)
static class DirtyContextTest {
}
```

It explicitly triggers the fixture's after-class lifecycle callback:

```java
TestContextManager dirtyContextManager = new TestContextManager(DirtyContextTest.class);
dirtyContextManager.beforeTestClass();

ApplicationContext originalContext = dirtyContextManager.getTestContext().getApplicationContext();

dirtyContextManager.afterTestClass();
```

The `afterTestClass()` callback allows Spring to process the `AFTER_CLASS` context invalidation.

A second fixture uses the same configuration:

```java
@ContextConfiguration(classes = DirtyConfiguration.class)
static class DirtyContextProbe {
}
```

The test loads its context and compares the instances:

```java
ApplicationContext reloadedContext = loadContext(DirtyContextProbe.class);

assertNotSame(originalContext, reloadedContext);
```

Because the original context was invalidated, the second fixture receives a newly loaded context.

The important distinction is that the configuration remains the same, but the original cached context is no longer eligible for reuse.

---

## When Should You Use `@DirtiesContext`?

`@DirtiesContext` is useful when a test changes the application context in a way that cannot safely be undone.

Examples include:

- Modifying singleton bean state that cannot be reliably restored.
- Changing context-level configuration during a test.
- Leaving the application context in a state that would make later tests unreliable.

However, it should not be used automatically after every test.

Each invalidated context may need to be initialized again, increasing the time required to run the test suite.

Whenever possible, prefer resetting the relevant test data or restoring bean state rather than discarding the entire application context.

---

## Context Caching and Test Performance

Context caching can significantly reduce test execution time when many tests share compatible configurations.

Without effective reuse:

```text
Test A ──► Load Context A
Test B ──► Load Context B
Test C ──► Load Context C
```

With caching:

```text
Test A ──► Load Context A
Test B ──► Reuse Context A
Test C ──► Reuse Context A
```

The actual performance improvement depends on how expensive context initialization is and how many tests can share the same context.

Large Spring Boot contexts can take substantial time to initialize, making effective caching especially valuable.

---

## Context Caching and Test Isolation

Context caching improves performance, but tests must still be isolated from one another.

A shared context can contain singleton beans whose mutable state persists between tests.

For example, a singleton bean that stores data in an instance field may retain that data after a test finishes.

A later test could then produce unexpected results.

Tests should therefore:

- Avoid depending on the execution order of other tests.
- Restore mutable state when appropriate.
- Use independent test data.
- Invalidate the context when its state cannot be safely restored.
- Avoid unnecessary changes to shared singleton beans.

A cached context is reusable infrastructure; it does not automatically reset the state of every bean between tests.

---

## Context Caching vs `@DirtiesContext`

These mechanisms serve different purposes.

| Feature               | Purpose                                                                 |
|-----------------------|-------------------------------------------------------------------------|
| Context caching       | Reuses compatible application contexts to reduce initialization work.   |
| `@DirtiesContext`     | Invalidates a context so that it will not be reused from the cache.     |
| Context configuration | Defines the configuration metadata used to load and identify a context. |
| `TestContextManager`  | Coordinates the Spring TestContext lifecycle for a test class.          |

Context caching is the default performance optimization. `@DirtiesContext` is a targeted invalidation mechanism.

They work together to balance performance with reliable test isolation.

---

## Testing Context Caching

The example uses JUnit Jupiter, Spring TestContext Framework support, and assertions from JUnit.

### Context Reuse

The test loads contexts for two fixtures using the same configuration:

```java
assertSame(firstContext, secondContext);
```

This verifies that the same application context instance is reused.

### Separate Contexts

The test compares contexts associated with different configuration classes:

```java
assertNotSame(sharedContext, alternativeContext);
```

This verifies that incompatible configurations do not share the same context instance in this example.

### Context Invalidation

The test triggers the after-class lifecycle callback for a fixture annotated with `@DirtiesContext` and then loads another fixture using the same configuration.

```java
assertNotSame(originalContext, reloadedContext);
```

This verifies that Spring loads a new context after the original one has been invalidated.

---

## Important Testing Detail

The example uses `TestContextManager` directly to control the lifecycle callbacks for the `@DirtiesContext` fixture.

This is intentional: it makes the invalidation sequence explicit and avoids relying on the execution order of separate JUnit test classes.

In ordinary Spring tests, annotations such as `@SpringJUnitConfig` and `@SpringBootTest` are more typical. Spring integrates the test lifecycle automatically, and compatible contexts can be cached across those test classes.

The example focuses on demonstrating the underlying caching behavior rather than building a full application.

---

## Running the Example

This example does not define a runnable Spring Boot application. Run the tests instead.

From the repository root:

```bash
mvn -pl 05-spring-testing/context-caching -am clean verify
```

Alternatively, run the module tests directly:

```bash
mvn -f 05-spring-testing/context-caching/pom.xml test
```

---

## Key Takeaways

- The Spring TestContext Framework caches application contexts to avoid unnecessary initialization.
- Compatible test configurations can reuse the same application context instance.
- Context reuse depends on configuration metadata, not test class names.
- Different configurations can result in separate cached contexts.
- `TestContextManager` provides access to the test context and its application context.
- `@DirtiesContext` invalidates a context that should not be reused.
- Context invalidation can increase test execution time because a new context may need to be loaded.
- Context caching does not automatically reset mutable state in singleton beans.
- Tests should remain reliable regardless of execution order.
- Spring's context cache is an important performance optimization for larger test suites.

---

## What This Example Does Not Cover

This example intentionally focuses on basic context reuse and invalidation.

It does not cover:

- Spring Boot context caching with `@SpringBootTest`.
- Context hierarchies and parent context invalidation.
- Custom context cache configuration.
- Maximum cache size and cache eviction.
- Cache statistics and debugging context cache misses.
- Parallel test execution and context sharing.
- External resources such as databases and containers.
- Advanced test isolation strategies.

These topics can be explored separately when needed.

---

## Next Example

The next example begins **Module 06 - Capstone Application** 

Bringing together concepts from the preceding Spring modules to build a cohesive, production-oriented application.
