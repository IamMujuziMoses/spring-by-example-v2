# Native Executable

This example demonstrates how to build a **Spring Boot application as a GraalVM native executable**.

It focuses on how Spring Boot's Ahead-of-Time (AOT) processing prepares an application for native compilation, how GraalVM Native Image produces an executable, and how to verify that the resulting executable starts and serves HTTP requests.

The example uses a small REST application to demonstrate the native executable build process without introducing unnecessary application complexity.

## What This Example Demonstrates

- What GraalVM Native Image is.
- The difference between JVM applications and native executables.
- How Spring Boot applications can be compiled into native executables.
- Spring Ahead-of-Time (AOT) processing.
- How the GraalVM Native Build Tools Maven plugin integrates with Maven.
- How to configure a native build using a Maven profile.
- How to build a native executable.
- How to run and verify a native executable.
- How to test the application using Spring Boot testing support.
- How to perform a smoke test against the native executable.

---

## What Is a Native Executable?

A traditional Spring Boot application runs on the Java Virtual Machine (JVM).

The application is compiled into Java bytecode, usually packaged as a JAR file, and executed by a JVM.

For example:

```bash
java -jar target/native-executable.jar
```

A GraalVM native executable takes a different approach.

Instead of executing Java bytecode on a JVM at runtime, GraalVM Native Image analyzes the application and its dependencies and compiles the application into a platform-specific native executable.

Conceptually:

```text
Traditional JVM Application

Java Source Code
       │
       ▼
Java Bytecode
       │
       ▼
Executable JAR
       │
       ▼
Java Virtual Machine
       │
       ▼
Running Application
```

A native executable follows this process:

```text
Native Application

Java Source Code
       │
       ▼
Java Bytecode
       │
       ▼
Spring AOT Processing
       │
       ▼
GraalVM Native Image
       │
       ▼
Native Executable
       │
       ▼
Running Application
```

The native executable contains the application and the runtime components required to execute it without requiring a separate JVM installation.

Native executables are particularly useful when application startup time, memory consumption, and deployment characteristics are important.

---

## What Is GraalVM Native Image?

**GraalVM Native Image** is a technology that compiles Java applications ahead of time into standalone native executables.

It analyzes application bytecode, dependencies, and reachable application code to determine what needs to be included in the final executable.

Unlike a traditional JVM application, a native executable does not rely on a JVM to execute Java bytecode at runtime.

Some potential benefits include:

- Faster application startup.
- Reduced memory consumption in many workloads.
- Smaller runtime deployment requirements.
- Convenient deployment as a standalone executable.
- Suitability for environments where fast startup is important, such as serverless platforms and containers.

However, native compilation also introduces constraints.

For example, some Java features that rely heavily on runtime reflection, dynamic class loading, or dynamically discovered resources may require additional configuration.

These constraints are explored in more detail in the subsequent examples in this module.

---

## Spring Boot and Native Executables

Spring applications traditionally rely on runtime mechanisms such as:

- Dependency injection.
- Component scanning.
- Reflection.
- Auto-configuration.
- Dynamic proxy creation.
- Resource loading.

These mechanisms can make it difficult for a native image compiler to determine everything an application needs at runtime.

Spring Boot addresses this through **Ahead-of-Time (AOT) processing**.

Spring AOT analyzes the application during the build process and generates additional code and metadata that help prepare the application for native compilation.

The resulting application can then be processed by GraalVM Native Image.

The simplified process is:

```text
Spring Boot Application
          │
          ▼
Spring AOT Processing
          │
          ▼
Generated AOT Code and Metadata
          │
          ▼
GraalVM Native Image
          │
          ▼
Native Executable
```

AOT processing moves certain work that would normally happen at runtime into the build process.

This helps make Spring applications more suitable for native compilation.

---

## The Application

This example contains a simple Spring Boot REST application.

The application exposes a greeting endpoint:

```text
GET /api/native/greeting
```

The endpoint returns:

```json
{
  "message": "Hello from Spring Boot!"
}
```

The application is intentionally small because the primary objective is to demonstrate the native executable build process rather than application functionality.

### Application Entry Point

The `NativeExecutableApplication` class is the application's entry point.

```java
@SpringBootApplication
public class NativeExecutableApplication {

    public static void main(String[] args) {
        SpringApplication.run(NativeExecutableApplication.class, args);
    }
}
```

The `@SpringBootApplication` annotation enables Spring Boot auto-configuration and component scanning.

The `main()` method starts the Spring application.

This is the same entry point used when running the application on the JVM or compiling it into a native executable.

### Greeting Controller

The `GreetingController` exposes the REST endpoint:

```java
@RestController
@RequestMapping("/api/native")
public class GreetingController {

    @GetMapping("/greeting")
    public GreetingResponse greeting() {
        return new GreetingResponse("Hello from Spring Boot!");
    }
}
```

The controller handles HTTP GET requests to `/api/native/greeting`.

It returns a `GreetingResponse` object, which Spring MVC serializes into JSON.

### Greeting Response

The `GreetingResponse` record represents the endpoint response:

```java
public record GreetingResponse(String message) {
}
```

Using a record provides a concise way to define an immutable response object.

---

## Application Configuration

The application uses the following configuration:

```yaml
spring:
  application:
    name: native-executable

server:
  port: 8090
```

The configuration defines:

- The application name as `native-executable`.
- The embedded web server port as `8090`.

The same configuration is used when running the application on the JVM and as a native executable.

---

## Maven Configuration

The example uses a dedicated Maven profile named `native`.

The native profile separates native compilation from the ordinary JVM build.

This allows the application to be built and tested normally without requiring GraalVM Native Image for every Maven invocation.

The profile configures two Maven plugins:

1. Spring Boot Maven Plugin.
2. GraalVM Native Build Tools Maven Plugin.

### Spring Boot Maven Plugin

The Spring Boot Maven Plugin provides the `process-aot` goal.

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <version>${spring-boot.version}</version>
    <executions>
        <execution>
            <id>process-aot</id>
            <goals>
                <goal>process-aot</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

The `process-aot` goal invokes Spring's AOT engine.

It analyzes the application and generates the code and metadata needed for the native compilation process.

### GraalVM Native Build Tools Plugin

The GraalVM Native Build Tools Maven Plugin integrates GraalVM Native Image with Maven.

A simplified configuration looks like:

```xml
<plugin>
    <groupId>org.graalvm.buildtools</groupId>
    <artifactId>native-maven-plugin</artifactId>
    <extensions>true</extensions>
    <executions>
        <execution>
            <id>build-native</id>
            <goals>
                <goal>compile-no-fork</goal>
            </goals>
            <phase>package</phase>
        </execution>
    </executions>
    <configuration>
        <imageName>${project.artifactId}</imageName>
        <mainClass>
            com.springbyexample.v2.nativeexecutable.NativeExecutableApplication
        </mainClass>
        <buildArgs>
            <buildArg>--no-fallback</buildArg>
        </buildArgs>
    </configuration>
</plugin>
```

The important configuration elements are:

| Configuration     | Purpose                                                                                  |
|-------------------|------------------------------------------------------------------------------------------|
| `extensions`      | Enables the Native Build Tools Maven extension and its build integration.                |
| `compile-no-fork` | Compiles the application into a native executable without forking another Maven process. |
| `phase`           | Associates native compilation with the Maven `package` phase.                            |
| `imageName`       | Defines the generated executable's name.                                                 |
| `mainClass`       | Identifies the application's main entry point.                                           |
| `buildArgs`       | Supplies additional arguments to GraalVM Native Image.                                   |

The `--no-fallback` argument prevents GraalVM from producing a fallback JVM-based image when native compilation cannot be completed.

The actual plugin configuration is defined in the example's Maven POM.

---

## The Native Maven Profile

The native build configuration is placed inside a Maven profile:

```xml
<profiles>
    <profile>
        <id>native</id>
        <build>
            <!-- Native compilation plugins -->
        </build>
    </profile>
</profiles>
```

This makes native compilation optional.

Without activating the profile, the project can be built and tested as a regular Spring Boot application.

To activate the profile, use:

```bash
-Pnative
```

This keeps the native build requirements separate from the ordinary development workflow.

---

## Building the Native Executable

Before building a native executable, ensure that the required tools are installed.

### Prerequisites

- JDK 21.
- GraalVM JDK compatible with the project's Java version.
- GraalVM Native Image installed.
- Required native build tools for the operating system.
- Maven.

Verify the Java installation:

```bash
java -version
```

Verify the Native Image installation:

```bash
native-image --version
```

The Java runtime used for the build should be a compatible GraalVM distribution.

### Build the Application

From the repository root, execute:

```bash
mvn -pl 11-graalvm-native/native-executable -Pnative clean package
```

This command:

1. Selects the `native-executable` module.
2. Activates the `native` Maven profile.
3. Cleans previous build artifacts.
4. Compiles the application.
5. Executes Spring AOT processing.
6. Invokes GraalVM Native Image.
7. Produces the native executable.

Native compilation can take considerably longer than an ordinary Maven build, particularly during the first build.

### Expected Output

After a successful native build, the executable should be available under:

```text
11-graalvm-native/native-executable/target/
```

The configured executable name is:

```text
native-executable
```

The exact generated artifact name and location depend on the Native Build Tools configuration and the operating system.

---

## Running the Native Executable

After a successful native build, execute the generated binary.

On macOS or Linux:

```bash
./11-graalvm-native/native-executable/target/native-executable
```

The application starts using its native executable rather than running through the JVM.

The embedded web server listens on port `8090`.

On Windows, run the generated executable using its `.exe` filename.

---

## Verifying the Native Executable

Once the application has started, send an HTTP request to the greeting endpoint:

```bash
curl -i http://localhost:8090/api/native/greeting
```

Expected response:

```http
HTTP/1.1 200
Content-Type: application/json
```

```json
{
  "message": "Hello from Spring Boot!"
}
```

This confirms that the native executable:

- Starts successfully.
- Initializes the Spring application context.
- Starts the embedded web server.
- Registers the REST controller.
- Handles HTTP requests.
- Serializes the response into JSON.

---

## Native Executable Smoke Test

The JUnit tests verify application context initialization and REST endpoint behavior on the JVM.

However, JVM-based tests alone do not prove that the application can be compiled into and executed as a GraalVM native executable.

For this reason, this example includes a separate native executable smoke-test procedure.

### 1. Build the Native Executable

From the repository root:

```bash
mvn -pl 11-graalvm-native/native-executable -Pnative clean package
```

### 2. Start the Native Executable

Run the generated executable:

```bash
./11-graalvm-native/native-executable/target/native-executable
```

### 3. Send an HTTP Request

Open another terminal and execute:

```bash
curl -i http://localhost:8090/api/native/greeting
```

### 4. Verify the Response

The endpoint should return HTTP `200 OK` with the following JSON response:

```json
{
  "message": "Hello from Spring Boot!"
}
```

### What This Verifies

The smoke test verifies that:

- GraalVM successfully compiles the application into a native executable.
- The native executable starts successfully.
- The Spring application initializes in the native runtime environment.
- The embedded web server starts.
- The greeting endpoint is accessible.
- The application returns the expected JSON response.

This complements the JVM-based JUnit tests by verifying the application in its native runtime environment.

The smoke test is a manual verification procedure, not an additional JUnit test.

---

## Running the Application on the JVM

The application can also be run normally without activating the native profile.

From the repository root:

```bash
mvn -pl 11-graalvm-native/native-executable spring-boot:run
```

The application starts on port `8090`.

Verify the endpoint:

```bash
curl http://localhost:8090/api/native/greeting
```

Expected response:

```json
{
  "message": "Hello from Spring Boot!"
}
```

This provides a convenient way to develop and test the application without performing native compilation.

---

## Running the Tests

Run the tests from the module directory:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 11-graalvm-native/native-executable test
```

The tests run on the JVM and do not require native compilation.

To build and verify the native executable, activate the native profile:

```bash
mvn -pl 11-graalvm-native/native-executable -Pnative clean package
```

---

## JVM Application vs Native Executable

The main difference between a traditional Spring Boot application and a native executable is when compilation and runtime initialization occur.

| Aspect             | JVM Application                                      | Native Executable                                                |
|--------------------|------------------------------------------------------|------------------------------------------------------------------|
| Compilation        | Java source is compiled into bytecode.               | Java application is compiled ahead of time into a native binary. |
| Runtime            | Requires a JVM.                                      | Does not require a separate JVM.                                 |
| Startup            | Includes JVM startup and application initialization. | Often has faster startup.                                        |
| Memory             | Includes JVM runtime overhead.                       | Often has lower memory consumption.                              |
| Build              | Usually a relatively straightforward Maven build.    | Requires AOT processing and native compilation.                  |
| Portability        | Java bytecode can run on compatible JVM platforms.   | Executable is platform-specific.                                 |
| Runtime reflection | Generally supported by the JVM.                      | May require explicit metadata or runtime hints.                  |

Native executables are not automatically better for every application.

The choice depends on application requirements, deployment environments, build complexity, and runtime characteristics.

---

## Important Native Image Considerations

Native compilation introduces constraints that developers should understand.

### Reflection

Native Image performs static analysis to determine which application code is reachable.

Classes, constructors, methods, and fields accessed dynamically through reflection may require additional configuration.

Spring provides mechanisms such as runtime hints to describe certain reflective requirements.

### Dynamic Class Loading

Applications that dynamically load classes at runtime may require additional configuration or architectural adjustments.

### Resource Access

Resources such as configuration files, templates, and other files accessed dynamically may need to be registered for inclusion in the native executable.

### Platform-Specific Executables

A native executable is generally tied to the operating system and architecture for which it was compiled.

For example, a native executable built on macOS for Intel architecture is not automatically executable on Linux or Windows.

Cross-platform native builds require an appropriate build environment.

These topics are explored in subsequent examples in this module.

---

## Key Takeaways

- GraalVM Native Image compiles Java applications into platform-specific native executables.
- Traditional Spring Boot applications normally run on the JVM.
- Native executables can run without a separate JVM installation.
- Spring AOT prepares Spring applications for native compilation.
- The Spring Boot Maven Plugin provides the `process-aot` goal.
- GraalVM Native Build Tools integrates native compilation with Maven.
- A dedicated Maven profile keeps native compilation separate from ordinary JVM builds.
- Native compilation requires GraalVM Native Image and the appropriate platform build tools.
- Native executables can provide faster startup and lower memory consumption in many workloads.
- Native Image introduces constraints around reflection, dynamic class loading, and resource access.
- JVM-based tests verify application behavior but do not replace native executable verification.
- A native executable smoke test verifies that the compiled application actually starts and handles requests.

---

## What This Example Does Not Cover

This example intentionally focuses on building and running a basic Spring Boot native executable.

It does not cover:

- Spring AOT processing in detail.
- Native Image reachability analysis.
- Native Image constraints in depth.
- Reflection configuration.
- Resource configuration.
- Spring Runtime Hints.
- Custom build-time processing.
- Native-specific automated testing.
- Containerized native applications.
- Native executable performance benchmarking.

These topics are explored in separate examples throughout this module.

---

## Next Example

The next example will explore **Spring AOT**, including how Spring analyzes an application at build time, generates AOT-processed code, and prepares the application for native compilation.
