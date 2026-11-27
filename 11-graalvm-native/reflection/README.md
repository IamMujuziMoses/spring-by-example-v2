# Reflection

This example demonstrates **reflection and runtime hints in Spring Boot applications designed for GraalVM Native Image**.

It focuses on how Java reflection can be used to dynamically load classes, instantiate objects, and invoke methods at runtime, and how Spring's Runtime Hints API makes reflective access available when building a native executable.

The example also demonstrates how to test reflection-based functionality and verify that the required runtime hints have been registered.

## What This Example Demonstrates

- What Java reflection is and how it works.
- Dynamically loading classes using `Class.forName()`.
- Creating objects using `getDeclaredConstructor().newInstance()`.
- Invoking methods using `getMethod().invoke()`.
- Why reflection can cause problems when compiling applications into GraalVM Native Images.
- The role of Spring's `RuntimeHints` API.
- Implementing `RuntimeHintsRegistrar`.
- Registering reflective access using `MemberCategory`.
- Importing runtime hints with `@ImportRuntimeHints`.
- Testing reflection-based functionality.
- Verifying registered reflection hints using `RuntimeHintsPredicates`.

---

## What Is Reflection?

Reflection is a Java capability that allows an application to inspect and interact with classes, constructors, methods, and fields at runtime.

Instead of referencing a class directly at compile time, an application can discover and load it dynamically.

For example, an application can load a class by its fully qualified name:

```java
Class<?> messageClass = Class.forName("com.springbyexample.v2.reflection.message.GreetingMessage");
```

It can then create an instance of that class:

```java
Object message = messageClass.getDeclaredConstructor().newInstance();
```

Finally, it can invoke a method dynamically:

```java
String result = (String) messageClass.getMethod("getMessage").invoke(message);
```

This approach is useful when the class to instantiate or method to invoke is determined at runtime.

Reflection is commonly used by frameworks, libraries, dependency injection containers, serialization libraries, and object mapping tools.

---

## Why Use Reflection?

Reflection provides flexibility when an application needs to work with classes without directly depending on their concrete implementations.

Consider an application that supports different message types:

- `GreetingMessage`
- `WelcomeMessage`

Without reflection, the application could instantiate each implementation directly:

```java
if ("Greeting".equals(type)) {
    return new GreetingMessage().getMessage();
}

if ("Welcome".equals(type)) {
    return new WelcomeMessage().getMessage();
}
```

With reflection, the application can construct the class name dynamically and instantiate the corresponding implementation.

```java
String className = MESSAGE_PACKAGE + type + "Message";

Class<?> messageClass = Class.forName(className);
Object message = messageClass.getDeclaredConstructor().newInstance();

return (String) messageClass.getMethod("getMessage").invoke(message);
```

This demonstrates how reflection can reduce direct dependencies on concrete implementations.

However, reflection also introduces runtime dependencies that may not be obvious to static analysis tools.

---

## Reflection in This Example

The example contains two message implementations:

- `GreetingMessage`
- `WelcomeMessage`

Both expose a `getMessage()` method.

The application accepts a message type and uses reflection to determine which implementation to load.

The service constructs the fully qualified class name using the supplied type:

```java
private static final String MESSAGE_PACKAGE = "com.springbyexample.v2.reflection.message.";

String className = MESSAGE_PACKAGE + type + "Message";
```

For example, when the requested type is `Greeting`, the resulting class name is:

```text
com.springbyexample.v2.reflection.message.GreetingMessage
```

When the requested type is `Welcome`, the resulting class name is:

```text
com.springbyexample.v2.reflection.message.WelcomeMessage
```

The service then loads the class, creates an instance, and invokes its `getMessage()` method.

---

## How the Reflection Process Works

The reflection process in this example consists of three main operations.

### 1. Loading a Class

The application uses:

```java
Class<?> messageClass = Class.forName(className);
```

`Class.forName()` loads the class using its fully qualified name.

It returns a `Class` object representing the loaded class.

The application can then use this object to inspect and interact with the class.

### 2. Creating an Instance

The application uses:

```java
Object message = messageClass.getDeclaredConstructor().newInstance();
```

This performs two operations:

- `getDeclaredConstructor()` obtains the class's declared no-argument constructor.
- `newInstance()` invokes the constructor to create an object.

The message implementations therefore need an accessible no-argument constructor for this example.

### 3. Invoking a Method

The application uses:

```java
return (String) messageClass.getMethod("getMessage").invoke(message);
```

This also performs two operations:

- `getMethod("getMessage")` obtains the public method named `getMessage`.
- `invoke(message)` invokes that method on the newly created object.

The result is cast to `String` because both message implementations return a string.

---

## The Reflection Flow

The complete process looks like this:

```text
HTTP Request
     │
     ▼
ReflectionController
     │
     ▼
ReflectionService
     │
     ▼
Construct Class Name
     │
     ▼
Class.forName()
     │
     ▼
Load Message Class
     │
     ▼
getDeclaredConstructor()
     │
     ▼
Create Object
     │
     ▼
getMethod("getMessage")
     │
     ▼
invoke()
     │
     ▼
Return Message
```

This illustrates how the application discovers and interacts with an implementation dynamically rather than directly instantiating it.

---

## Reflection and GraalVM Native Image

Reflection works naturally on the JVM because Java applications execute with runtime metadata that supports dynamic class loading and reflective access.

GraalVM Native Image uses a different execution model.

During native image compilation, GraalVM analyzes the application ahead of time to determine which classes, methods, constructors, and other resources are required.

This analysis relies heavily on information available through static analysis.

Consider:

```java
Class<?> messageClass = Class.forName(className);
```

The value of `className` is constructed dynamically.

A static analysis tool may not be able to determine which classes will be loaded at runtime.

Consequently, the required classes or reflective members may not be included in the native executable.

The application might work correctly on the JVM but fail when executed as a native image.

This is one of the important differences between traditional JVM execution and native image execution.

### JVM Execution

```text
Application Starts
      │
      ▼
Runtime Class Loading
      │
      ▼
Reflection
      │
      ▼
Dynamic Class Instantiation
      │
      ▼
Method Invocation
```

### Native Image Execution

```text
Application Source
      │
      ▼
Ahead-of-Time Analysis
      │
      ▼
Native Image Compilation
      │
      ▼
Only Discovered or Configured Metadata
      │
      ▼
Native Executable
```

If reflection is required but cannot be discovered during analysis, the application must provide the necessary metadata.

This is where Spring Runtime Hints become useful.

---

## What Are Runtime Hints?

Spring Runtime Hints provide a way to declare runtime requirements that may not be discoverable through static analysis.

They describe resources and runtime access that an application needs, including:

- Reflective access to classes.
- Constructor invocation.
- Method invocation.
- Field access.
- Resource access.
- Proxy generation and other supported runtime requirements.

For this example, runtime hints are used to register the constructors and methods accessed through reflection.

Instead of relying entirely on static analysis to discover reflective usage, the application explicitly declares the required access.

Runtime hints are particularly important when preparing Spring applications for GraalVM Native Image compilation.

---

## Implementing RuntimeHintsRegistrar

The example defines a class named `GreetingRuntimeHints` that implements Spring's `RuntimeHintsRegistrar` interface.

```java
public class GreetingRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection().registerType(GreetingMessage.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS);

        hints.reflection().registerType(WelcomeMessage.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS);
    }
}
```

The `registerHints()` method receives two arguments:

- `RuntimeHints hints`: The registry used to declare runtime requirements.
- `ClassLoader classLoader`: The class loader associated with the registration process.

The example registers reflection requirements for both message implementations.

### Registering a Type

The following code registers reflective access for `GreetingMessage`:

```java
hints.reflection().registerType(GreetingMessage.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
        MemberCategory.INVOKE_PUBLIC_METHODS);
```

It declares that the application needs to:

- Invoke declared constructors.
- Invoke public methods.

The same registration is performed for `WelcomeMessage`.

### Understanding MemberCategory

`MemberCategory` specifies which members of a type require reflective access.

This example uses:

| Member Category                | Purpose                                                              |
|--------------------------------|----------------------------------------------------------------------|
| `INVOKE_DECLARED_CONSTRUCTORS` | Allows constructors declared by the type to be invoked reflectively. |
| `INVOKE_PUBLIC_METHODS`        | Allows public methods to be invoked reflectively.                    |

These categories are selected based on how the reflection code accesses the message implementations.

Registering only the required categories is preferable to unnecessarily exposing all reflective members.

---

## Importing Runtime Hints

Registering a `RuntimeHintsRegistrar` implementation does not automatically associate it with the application.

Spring needs to know that the registrar should be used.

This example uses `@ImportRuntimeHints` on the main application class:

```java
@SpringBootApplication
@ImportRuntimeHints(GreetingRuntimeHints.class)
public class ReflectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReflectionApplication.class, args);
    }
}
```

The annotation imports the runtime hints registrar into the application's AOT processing.

When Spring processes the application's runtime hints, it invokes the registrar so that the declared reflection requirements can be included in the generated runtime metadata.

This connects the application's reflection requirements with Spring's AOT infrastructure.

---

## Runtime Hints Registration Flow

The relationship between the application and its runtime hints looks like this:

```text
ReflectionApplication
        │
        ▼
@ImportRuntimeHints
        │
        ▼
GreetingRuntimeHints
        │
        ▼
RuntimeHintsRegistrar
        │
        ▼
registerHints()
        │
        ▼
Reflection Hints
        │
        ▼
GraalVM Native Image Metadata
```

The runtime hints describe the reflective access required by the application.

They do not replace reflection or change how the application invokes methods. They provide additional information for ahead-of-time processing and native image compilation.

---

## Runtime Hints vs Reflection

Reflection and runtime hints serve different purposes.

| Reflection                                                 | Runtime Hints                                                                       |
|------------------------------------------------------------|-------------------------------------------------------------------------------------|
| Provides dynamic access to classes and members at runtime. | Declares runtime requirements for ahead-of-time processing.                         |
| Can load classes dynamically.                              | Describes classes and members that need reflective access.                          |
| Can instantiate objects dynamically.                       | Registers the required constructor access.                                          |
| Can invoke methods dynamically.                            | Registers the required method access.                                               |
| Works naturally on the JVM.                                | Helps make reflection-based functionality compatible with native image compilation. |

The two mechanisms complement each other.

Reflection performs the dynamic operation, while runtime hints describe the reflective access that must be available in a native executable.

---

## Testing Reflection Functionality

The example uses Spring Boot's testing support and JUnit 5 to verify the reflection implementation.

The service tests cover:

- Loading and invoking `GreetingMessage`.
- Loading and invoking `WelcomeMessage`.
- Rejecting unsupported message types.
- Handling a class that cannot be found.
- Handling a class without a no-argument constructor.
- Handling a class without the expected public method.
- Wrapping reflective failures in an `IllegalStateException`.

### Testing Successful Reflection

The tests verify that the service returns the expected message for each supported type.

For example:

```java
assertThat(reflectionService.getMessage("Greeting")).isEqualTo("Hello! Welcome to Spring Reflection.");
```

And:

```java
assertThat(reflectionService.getMessage("Welcome")).isEqualTo("Welcome! This example demonstrates reflection in Spring.");
```

These tests verify the complete reflection process: loading the class, instantiating it, and invoking its method.

### Testing Unsupported Types

The service only accepts the supported message types.

An unsupported type should result in an `IllegalArgumentException`.

```java
assertThatThrownBy(() -> reflectionService.getMessage("Unknown")).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unsupported message type: Unknown");
```

This ensures that arbitrary message types cannot be passed through the public service method.

### Testing Reflective Exceptions

Reflection operations can fail for several reasons.

For example:

- `ClassNotFoundException` occurs when the requested class cannot be found.
- `NoSuchMethodException` occurs when the requested constructor or method does not exist.
- `IllegalAccessException` can occur when reflective access is not permitted.

The example wraps reflective failures in an `IllegalStateException` to provide a consistent application-level exception.

The tests verify that the original reflective exception is preserved as the cause.

This is important because preserving the cause makes the underlying problem easier to diagnose.

---

## Testing Runtime Hints

The example includes `GreetingRuntimeHintsTest` to verify that the expected reflection hints have been registered.

The test creates a `RuntimeHints` instance and invokes the registrar:

```java
RuntimeHints hints = new RuntimeHints();

new GreetingRuntimeHints().registerHints(hints, getClass().getClassLoader());
```

It then verifies the registered type:

```java
assertThat(RuntimeHintsPredicates.reflection()
        .onType(GreetingMessage.class)
        .test(hints))
        .isTrue();
```

The test also verifies constructor and method access:

```java
Constructor<GreetingMessage> constructor =
        GreetingMessage.class.getDeclaredConstructor();

Method method = GreetingMessage.class.getMethod("getMessage");

assertThat(RuntimeHintsPredicates.reflection()
        .onConstructor(constructor)
        .test(hints))
        .isTrue();

assertThat(RuntimeHintsPredicates.reflection()
        .onMethod(method)
        .test(hints))
        .isTrue();
```

Equivalent assertions are performed for `WelcomeMessage`.

These tests verify that the registrar declares the reflective access required by the service.

They do not execute a native image or prove that a native executable will work. They verify the Spring runtime hint registrations themselves.

---

## Testing the Application Context

The example also includes an application context test.

It verifies that Spring Boot can start the application and discover the required components.

The test checks that:

- The application context loads successfully.
- `ReflectionService` is available as a Spring bean.
- `ReflectionController` is available as a Spring bean.

This verifies the normal Spring application configuration independently of the reflection-specific tests.

---

## Testing the REST Controller

The example exposes the reflection functionality through a REST endpoint:

```text
GET /api/native/reflection/message
```

The endpoint accepts a `type` parameter.

### Greeting Message

```bash
curl "http://localhost:8093/api/native/reflection/message?type=Greeting"
```

Expected response:

```text
Hello! Welcome to Spring Reflection.
```

### Welcome Message

```bash
curl "http://localhost:8093/api/native/reflection/message?type=Welcome"
```

Expected response:

```text
Welcome! This example demonstrates reflection in Spring.
```

### Default Message

When no type is supplied, the controller uses `Greeting` as the default.

```bash
curl "http://localhost:8093/api/native/reflection/message"
```

Expected response:

```text
Hello! Welcome to Spring Reflection.
```

---

## Spring AOT and Runtime Hints

Spring AOT processing prepares an application for ahead-of-time execution by analyzing the application context and generating code and metadata.

Runtime hints complement this process by declaring requirements that cannot necessarily be discovered through static analysis.

For this example, the AOT processing can discover the registered `GreetingRuntimeHints` implementation and incorporate its reflection declarations into the generated native image metadata.

The AOT processing command is:

```bash
mvn -pl 11-graalvm-native/reflection -Paot clean process-classes
```

This demonstrates Spring's AOT processing for the application.

It is important to distinguish AOT processing from native image compilation:

- AOT processing generates application-specific code and metadata ahead of time.
- Native image compilation uses the generated information and other build inputs to produce a native executable.

Running the AOT command alone does not produce a native executable.

---

## Running the Example

From the repository root:

```bash
mvn -pl 11-graalvm-native/reflection spring-boot:run
```

The application starts on port `8093`.

The reflection endpoint is available at:

```text
http://localhost:8093/api/native/reflection/message
```

---

## Running the Tests

Run the tests from the module:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 11-graalvm-native/reflection test
```

To run only the reflection service tests:

```bash
mvn -pl 11-graalvm-native/reflection -Dtest=ReflectionServiceTest test
```

To run only the runtime hints tests:

```bash
mvn -pl 11-graalvm-native/reflection -Dtest=GreetingRuntimeHintsTest test
```

---

## Running Spring AOT Processing

To execute Spring AOT processing:

```bash
mvn -pl 11-graalvm-native/reflection -Paot clean process-classes
```

This runs the Spring Boot `process-aot` goal and generates AOT-related sources and metadata.

The generated output can be inspected under the module's `target` directory.

---

## Key Takeaways

- Reflection allows Java applications to inspect and interact with classes dynamically.
- `Class.forName()` loads a class using its fully qualified name.
- `getDeclaredConstructor().newInstance()` creates an object reflectively.
- `getMethod().invoke()` invokes a method dynamically.
- Reflection can introduce runtime dependencies that are difficult for static analysis to discover.
- GraalVM Native Image requires the necessary reflective metadata to be available during compilation.
- Spring Runtime Hints provide a mechanism for declaring runtime requirements.
- `RuntimeHintsRegistrar` allows applications to register runtime hints programmatically.
- `MemberCategory` specifies the reflective access required by a type.
- `@ImportRuntimeHints` connects a runtime hints registrar to the Spring application.
- `RuntimeHintsPredicates` can be used to verify registered hints in tests.
- Spring AOT processing and native image compilation are related but distinct steps.
- Runtime hints complement reflection; they do not replace the reflection implementation.

---

## Next Example

The next example will explore **Resource Configuration** in GraalVM Native Image, including how applications access classpath resources and how resource hints make those resources available in native executables.
