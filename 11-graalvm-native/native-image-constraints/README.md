# Native Image Constraints

This example demonstrates **Native Image constraints** when compiling Spring Boot applications with GraalVM Native Image.

It focuses on the limitations of dynamic class loading and reflection in native executables, why applications that work on a regular JVM may behave differently when compiled into a Native Image, and why GraalVM needs information about application behavior at build time.

The example uses dynamically selected message implementations to demonstrate how runtime class loading and reflective access can introduce challenges for native compilation.

## What This Example Demonstrates

- What GraalVM Native Image is.
- The closed-world assumption used by Native Image.
- The difference between JVM execution and Native Image execution.
- Dynamic class loading using `Class.forName()`.
- Reflective object instantiation using `getDeclaredConstructor().newInstance()`.
- Reflective method invocation using `getMethod().invoke()`.
- Why dynamically accessed classes and methods may require explicit configuration.
- How Spring AOT processing fits into native compilation.
- Testing dynamic class loading on the JVM.
- Understanding why successful JVM tests do not guarantee successful native execution.

---

## What Is GraalVM Native Image?

GraalVM Native Image is a technology that compiles Java applications into standalone native executables.

Unlike traditional Java applications, which execute bytecode on a Java Virtual Machine (JVM), a native executable contains machine code generated ahead of time.

The compilation process analyses the application and determines which classes, methods, and resources are reachable.

A simplified comparison:

### Traditional JVM Execution

```text
Java Source Code
       │
       ▼
Compilation
       │
       ▼
Java Bytecode
       │
       ▼
JVM
       │
       ▼
Application Execution
```

### GraalVM Native Image Execution

```text
Java Source Code
       │
       ▼
Compilation
       │
       ▼
Java Bytecode
       │
       ▼
Native Image Analysis
       │
       ▼
Native Compilation
       │
       ▼
Native Executable
       │
       ▼
Application Execution
```

Native Image compilation performs significant analysis at build time rather than relying on the same dynamic capabilities available to a traditional JVM application.

---

## The Closed-World Assumption

One of the most important concepts in GraalVM Native Image is the **closed-world assumption**.

GraalVM analyses the application during native compilation to determine which application components are reachable.

It uses this information to determine what should be included in the resulting executable.

This allows GraalVM to optimize the application and produce a smaller, standalone executable.

However, it introduces a challenge when application behavior depends on information that is only available at runtime.

For example:

```java
Class<?> messageClass = Class.forName(className);
```

When the class name is constructed dynamically, GraalVM may not be able to determine which class will be loaded.

Consequently, the dynamically accessed class may not be included in the native executable unless the required information is provided through configuration or runtime hints.

Conceptually:

```text
Application Code
       │
       ▼
Native Image Analysis
       │
       ▼
Reachable Classes and Methods
       │
       ▼
Native Executable
```

Dynamically accessed components that cannot be discovered through static analysis may require additional metadata.

---

## Dynamic Class Loading

Dynamic class loading allows an application to load a class at runtime using its name.

Java provides `Class.forName()` for this purpose.

For example:

```java
Class<?> messageClass = Class.forName("com.example.message.GreetingMessage");
```

This tells the JVM to locate and load the specified class at runtime.

Dynamic class loading is useful when the application needs to select implementations based on runtime information.

Examples include:

- Plugin systems.
- Configurable implementations.
- Framework integrations.
- Dynamically selected strategies.
- Applications that load classes based on configuration.

However, this behavior can introduce challenges when compiling an application into a Native Image.

GraalVM cannot always determine which classes will be loaded dynamically.

As a result, those classes may need to be explicitly registered for runtime access.

---

## Example Implementation

This example demonstrates dynamic class loading through two message implementations.

The application allows a caller to select a message implementation using an HTTP request parameter.

### Message Implementations

The example contains two message classes.

**`GreetingMessage.java`**

```java
public class GreetingMessage {

    public String getMessage() {
        return "Hello! Welcome to GraalVM Native Image Constraints.";
    }
}
```

**`WelcomeMessage.java`**

```java
public class WelcomeMessage {

    public String getMessage() {
        return "Welcome! This example demonstrates dynamic class loading.";
    }
}
```

Both classes expose the same method, `getMessage()`, but return different messages.

The application selects which implementation to load at runtime.

---

## Dynamic Loading with Reflection

The `ConstraintDemoService` demonstrates how an application can load and instantiate classes dynamically.

```java
@Service
public class ConstraintDemoService {

    private static final String MESSAGE_PACKAGE = "com.springbyexample.v2.nativeimageconstraints.message.";

    public String loadMessage(String type) {
        if (!"Greeting".equals(type) && !"Welcome".equals(type)) {
            throw new IllegalArgumentException("Unsupported message type: " + type);
        }

        String className = MESSAGE_PACKAGE + type + "Message";

        try {
            Class<?> messageClass = Class.forName(className);
            Object message = messageClass.getDeclaredConstructor().newInstance();

            return (String) messageClass.getMethod("getMessage").invoke(message);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to load message implementation: " + className, exception);
        }
    }
}
```

The service performs several operations that are important to understand.

### 1. Constructing the Class Name

```java
String className = MESSAGE_PACKAGE + type + "Message";
```

The class name is constructed using the supplied message type.

For example:

```text
type = Greeting

className = com.springbyexample.v2.nativeimageconstraints.message.GreetingMessage
```

The application does not directly reference the message implementation in this operation.

Instead, it constructs the fully qualified class name at runtime.

### 2. Loading the Class

```java
Class<?> messageClass = Class.forName(className);
```

`Class.forName()` loads the class using its fully qualified name.

This is dynamic class loading because the class is selected using a string rather than a direct class reference.

### 3. Creating an Instance

```java
Object message = messageClass.getDeclaredConstructor().newInstance();
```

This operation obtains the class's no-argument constructor and invokes it to create an instance.

The constructor is accessed reflectively.

### 4. Invoking a Method

```java
return (String) messageClass.getMethod("getMessage").invoke(message);
```

The service retrieves the `getMessage()` method and invokes it on the dynamically created object.

This is another reflective operation.

### Why This Matters for Native Image

The service combines three dynamic operations:

| Operation          | Java API                     | Purpose                                      |
|--------------------|------------------------------|----------------------------------------------|
| Class loading      | `Class.forName()`            | Loads a class using its name.                |
| Constructor access | `getDeclaredConstructor()`   | Retrieves a constructor reflectively.        |
| Method invocation  | `getMethod()` and `invoke()` | Retrieves and invokes a method reflectively. |

These operations are valid Java and work on a regular JVM.

However, GraalVM Native Image needs to know about reflective access that cannot be determined through static analysis.

Without the appropriate metadata, the application may fail when attempting to perform these operations in a native executable.

---

## Exposing the Functionality Through REST

The example exposes the dynamic loading functionality through a REST endpoint.

**`ConstraintController.java`**

```java
@RestController
public class ConstraintController {

    private final ConstraintDemoService constraintDemoService;

    public ConstraintController(ConstraintDemoService constraintDemoService) {
        this.constraintDemoService = constraintDemoService;
    }

    @GetMapping("/api/native/constraints/message")
    public String getMessage(@RequestParam(defaultValue = "Greeting") String type) {
        return constraintDemoService.loadMessage(type);
    }
}
```

The controller accepts a `type` request parameter and delegates message selection to `ConstraintDemoService`.

The default message type is `Greeting`.

This makes it possible to exercise the dynamic loading behavior through HTTP requests.

---

## Understanding the Application Flow

The application follows this simplified flow:

```text
HTTP Request
     │
     ▼
ConstraintController
     │
     ▼
ConstraintDemoService
     │
     ▼
Construct Class Name
     │
     ▼
Class.forName()
     │
     ▼
Load Class
     │
     ▼
Reflective Constructor Invocation
     │
     ▼
Reflective Method Invocation
     │
     ▼
Return Message
```

The important part is that the implementation is selected dynamically rather than through a direct Java object reference.

---

## JVM Execution vs Native Image Execution

The same application can behave differently depending on how it is executed.

### JVM Execution

On a regular JVM, the application can dynamically load classes from the application classpath.

For example:

```java
Class<?> messageClass = Class.forName(className);
```

The JVM can locate the class at runtime, provided it is available on the classpath and accessible.

### Native Image Execution

During native compilation, GraalVM analyses the application and determines which components are reachable.

A dynamically constructed class name may not be discoverable through static analysis.

Consequently, the class or reflective members may not be available for runtime access unless the necessary metadata is supplied.

This means that an application passing all JVM tests does not necessarily guarantee that it will work correctly as a native executable.

---

## Spring AOT and Native Image

Spring AOT and GraalVM Native Image are related but distinct technologies.

Spring AOT performs build-time processing of a Spring application.

It generates application-specific code and metadata that help prepare the application for efficient execution, including native compilation.

GraalVM Native Image then performs its own analysis and compilation to produce the native executable.

A simplified relationship:

```text
Spring Boot Application
         │
         ▼
Spring AOT Processing
         │
         ▼
Generated Application Code
         │
         ▼
GraalVM Native Image Analysis
         │
         ▼
Native Compilation
         │
         ▼
Native Executable
```

Spring AOT does not automatically resolve every dynamic access in an application.

Developers may still need to provide explicit runtime hints for reflection, resources, serialization, and proxies.

This is why understanding Native Image constraints is important when developing Spring applications intended for native compilation.

---

## Running the Example

From the repository root:

```bash
mvn -pl 11-graalvm-native/native-image-constraints spring-boot:run
```

The application starts on port `8092`.

### Greeting Message

```bash
curl "http://localhost:8092/api/native/constraints/message?type=Greeting"
```

Expected response:

```text
Hello! Welcome to GraalVM Native Image Constraints.
```

### Welcome Message

```bash
curl "http://localhost:8092/api/native/constraints/message?type=Welcome"
```

Expected response:

```text
Welcome! This example demonstrates dynamic class loading.
```

### Default Message

```bash
curl "http://localhost:8092/api/native/constraints/message"
```

Expected response:

```text
Hello! Welcome to GraalVM Native Image Constraints.
```

### Unsupported Message Type

```bash
curl "http://localhost:8092/api/native/constraints/message?type=Unknown"
```

The service rejects the unsupported message type with an `IllegalArgumentException`, which results in an HTTP 500 response with the current controller implementation.

---

## Running the Tests

Run all tests from the repository root:

```bash
mvn -pl 11-graalvm-native/native-image-constraints test
```

Run only the service tests:

```bash
mvn -pl 11-graalvm-native/native-image-constraints \
    -Dtest=ConstraintDemoServiceTest \
    test
```

Run only the controller tests:

```bash
mvn -pl 11-graalvm-native/native-image-constraints \
    -Dtest=ConstraintControllerTest \
    test
```

---

## Running Spring AOT Processing

The example includes an `aot` Maven profile that executes Spring Boot's `process-aot` goal.

Run:

```bash
mvn -pl 11-graalvm-native/native-image-constraints -Paot clean process-classes
```

Inspect the generated AOT sources:

```bash
find 11-graalvm-native/native-image-constraints/target/spring-aot/main/sources \
    -type f -name "*.java"
```

This demonstrates Spring's build-time application processing.

It does not compile the application into a GraalVM Native Image.

Native compilation and the runtime behavior of the dynamically loaded classes are separate verification steps.

---

## Important Distinction: AOT Processing vs Native Compilation

These two operations serve different purposes.

| Operation                        | Responsibility                                                  |
|----------------------------------|-----------------------------------------------------------------|
| Spring AOT processing            | Generates application-specific code and metadata at build time. |
| GraalVM Native Image compilation | Analyses and compiles the application into a native executable. |

Successfully generating Spring AOT sources does not guarantee that all dynamic application behavior is supported by the resulting native executable.

The native build must also account for the application's dynamic access requirements.

---

## Key Takeaways

- GraalVM Native Image compiles Java applications into standalone native executables.
- Native Image uses a closed-world assumption and performs reachability analysis at build time.
- Dynamic class loading can introduce challenges because class names may only be known at runtime.
- `Class.forName()` loads classes dynamically.
- `getDeclaredConstructor().newInstance()` creates objects through reflection.
- `getMethod().invoke()` invokes methods reflectively.
- Dynamically accessed classes and members may require explicit runtime metadata.
- Successful JVM execution does not guarantee successful native execution.
- Spring AOT prepares Spring applications for build-time processing but does not eliminate every Native Image constraint.
- Native Image compatibility must be verified through native compilation and execution.

---

## Next Example

The next example will explore **Reflection in GraalVM Native Image**, including why reflective access requires additional metadata, how to register classes and members for reflection, and how Spring Runtime Hints can make reflective operations compatible with native compilation.
