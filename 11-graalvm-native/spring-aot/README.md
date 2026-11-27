# Spring AOT

This example demonstrates **Ahead-of-Time (AOT) processing in Spring Boot**.

It focuses on how Spring analyzes an application during the build process and generates code that can be used to initialize the application context more efficiently at runtime.

The example uses a simple Spring Boot application containing a REST controller and a service. It demonstrates how Spring AOT generates bean definitions, bean instantiation instructions, and application context initialization code for these components and the supporting Spring Boot infrastructure.

## What This Example Demonstrates

- What Ahead-of-Time (AOT) processing means in Spring.
- The difference between runtime processing and build-time processing.
- How Spring Boot performs AOT processing.
- The role of the `process-aot` Maven goal.
- How Spring generates bean definitions.
- How Spring generates bean instantiation instructions.
- How constructor dependencies are represented in generated code.
- The role of `ApplicationContextInitializer` in restoring an AOT-processed application context.
- How generated bean definitions are registered with the `BeanFactory`.
- How Spring Boot auto-configuration participates in AOT processing.
- Inspecting generated Java source files and compiled classes.
- Testing the application and its REST controller.

---

## What Is Ahead-of-Time Processing?

Ahead-of-Time (AOT) processing is a build-time optimization technique in which Spring analyzes an application and generates code that represents parts of the application context configuration.

Traditionally, Spring performs considerable work at runtime, including:

- Discovering application components.
- Processing configuration classes.
- Evaluating auto-configuration conditions.
- Creating bean definitions.
- Preparing dependency injection.
- Registering application infrastructure.

With AOT processing, Spring performs supported analysis and preparation during the build process.

The generated code can then be used when the application starts, reducing the amount of dynamic processing required at runtime.

Conceptually:

### Without AOT Processing

```text
Application Startup
        |
        v
Spring Framework
        |
        v
Component Scanning
        |
        v
Configuration Processing
        |
        v
Auto-Configuration
        |
        v
Bean Definitions
        |
        v
Application Context
```

### With AOT Processing

```text
             Build Time
                 |
                 v
          Spring AOT Engine
                 |
                 v
       Application Analysis
                 |
                 v
       Generated Java Sources
                 |
                 v
       Generated Class Files
                 |
                 v
            Runtime
                 |
                 v
       AOT Application Context
```

The important distinction is that Spring prepares certain application context operations during the build process instead of relying entirely on runtime discovery and processing.

---

## AOT Processing vs Native Image Compilation

AOT processing and native image compilation are related concepts, but they are not the same thing.

### Spring AOT Processing

Spring AOT analyzes the application and generates Java source code and other artifacts required for AOT-optimized application startup.

It can be used independently of native image compilation.

### GraalVM Native Image Compilation

GraalVM Native Image uses ahead-of-time compilation to produce a native executable from an application and its dependencies.

Spring AOT provides generated infrastructure that helps Spring applications work with the constraints of native images.

The relationship can be represented as:

```text
Spring Application
        |
        v
Spring AOT Processing
        |
        v
Generated AOT Artifacts
        |
        +----------------------+
        |                      |
        v                      v
AOT-Processed JVM       GraalVM Native Image
Execution               Compilation
        |                      |
        v                      v
JVM Application         Native Executable
```

This example focuses on Spring AOT processing rather than GraalVM native image compilation.

---

## Application Structure

The example uses a simple Spring Boot application with the following structure, the application contains:

| Component                  | Responsibility                           |
|----------------------------|------------------------------------------|
| `SpringAotApplication`     | Starts the Spring Boot application       |
| `GreetingController`       | Exposes the greeting REST endpoint       |
| `GreetingService`          | Provides greeting messages               |
| `application.yml`          | Configures the application               |
| `SpringAotApplicationTest` | Tests application context initialization |
| `GreetingControllerTest`   | Tests the REST controller                |

The application is intentionally small so that the generated AOT code can be inspected without unnecessary complexity.

---

## Application Components

### SpringAotApplication

The application entry point is a standard Spring Boot application:

```java
@SpringBootApplication
public class SpringAotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAotApplication.class, args);
    }
}
```

`@SpringBootApplication` enables Spring Boot auto-configuration and component scanning.

During AOT processing, Spring analyzes the application configuration and discovers the components required to construct the application context.

### GreetingService

The service provides the greeting message:

```java
@Service
public class GreetingService {

    public String greet(String name) {
        return "Hello, " + name + "! Welcome to Spring AOT.";
    }
}
```

The service is registered as a Spring bean through `@Service`.

During AOT processing, Spring generates a bean definition for this component.

### GreetingController

The controller exposes an HTTP endpoint:

```java
@RestController
public class GreetingController {

    private final GreetingService greetingService;

    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/api/aot/greeting")
    public String greeting(@RequestParam(defaultValue = "Spring AOT") String name) {
        return greetingService.greet(name);
    }
}
```

The controller uses constructor injection to receive `GreetingService`.

This dependency is important when inspecting the generated AOT code because Spring generates instructions for instantiating the controller and supplying its dependency.

---

## Maven Configuration

The example uses the Spring Boot Maven Plugin to execute AOT processing.

The plugin is configured inside an `aot` Maven profile:

```xml
<profiles>
    <profile>
        <id>aot</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                    <version>${spring-boot.version}</version>
                    <executions>
                        <execution>
                            <id>process-aot</id>
                            <phase>process-classes</phase>
                            <goals>
                                <goal>process-aot</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

### Understanding the Configuration

#### `aot` Profile

```xml
<id>aot</id>
```

The profile separates AOT processing from the normal Maven build.

This allows the example to be built and tested normally without automatically executing AOT processing.

#### Spring Boot Maven Plugin

```xml
<artifactId>spring-boot-maven-plugin</artifactId>
```

The Spring Boot Maven Plugin provides goals for working with Spring Boot applications, including AOT processing.

#### `process-aot` Goal

```xml
<goal>process-aot</goal>
```

This goal invokes Spring's AOT engine.

It analyzes the application and generates the necessary AOT source files and resources.

#### `process-classes` Phase

```xml
<phase>process-classes</phase>
```

This binds AOT processing to Maven's `process-classes` lifecycle phase when the `aot` profile is activated.

The generated artifacts are written under:

```text
target/spring-aot/main/
```

---

## Generated Bean Definitions

Spring generates bean definition classes for application components.

These classes contain instructions for creating the corresponding Spring beans.

### GreetingService__BeanDefinitions

The generated service bean definition looks like:

```java
public class GreetingService__BeanDefinitions {

    public static BeanDefinition getGreetingServiceBeanDefinition() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(GreetingService.class);

        beanDefinition.setInstanceSupplier(GreetingService::new);

        return beanDefinition;
    }
}
```

Spring has determined that `GreetingService` does not require constructor dependencies.

It therefore generates a bean instance supplier using:

```java
GreetingService::new
```

This provides an explicit instruction for creating the service.

### GreetingController__BeanDefinitions

The controller has a constructor dependency on `GreetingService`.

Its generated bean definition looks like:

```java
public class GreetingController__BeanDefinitions {

    private static BeanInstanceSupplier<GreetingController> getGreetingControllerInstanceSupplier() {

        return BeanInstanceSupplier.<GreetingController>forConstructor(GreetingService.class)
                .withGenerator((registeredBean, args) -> new GreetingController(args.get(0)));
    }

    public static BeanDefinition getGreetingControllerBeanDefinition() {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(GreetingController.class);

        beanDefinition.setInstanceSupplier(getGreetingControllerInstanceSupplier());

        return beanDefinition;
    }
}
```

Spring has determined that:

- `GreetingController` requires `GreetingService`.
- The controller uses constructor injection.
- The constructor requires a `GreetingService` argument.
- The controller can be instantiated using the generated supplier.

The generated code represents the dependency explicitly:

```text
GreetingController
        |
        v
GreetingService
```

This demonstrates how Spring AOT prepares bean instantiation instructions during the build process.

---

## ApplicationContextInitializer

Spring generates an `ApplicationContextInitializer` to restore the application context using the generated registration code.

The generated class implements:

```java
ApplicationContextInitializer<GenericApplicationContext>
```

Its initialization method looks like:

```java
@Override
public void initialize(GenericApplicationContext applicationContext) {

    DefaultListableBeanFactory beanFactory = applicationContext.getDefaultListableBeanFactory();
    beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
    beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);

    new SpringAotApplication__BeanFactoryRegistrations().registerBeanDefinitions(beanFactory);
    new SpringAotApplication__BeanFactoryRegistrations().registerAliases(beanFactory);
}
```

The initializer performs several operations:

1. Obtains the application's `DefaultListableBeanFactory`.
2. Configures the autowire candidate resolver.
3. Configures the dependency comparator.
4. Registers the generated bean definitions.
5. Registers the generated bean aliases.

The important operation is:

```java
registerBeanDefinitions(beanFactory);
```

This connects the generated bean definitions to the application context.

Instead of relying entirely on runtime component discovery to register application beans, the generated registration code supplies bean definitions prepared during AOT processing.

---

## BeanFactoryRegistrations

The generated `SpringAotApplication__BeanFactoryRegistrations` class connects the generated bean definitions to the `BeanFactory`.

For example:

```java
public void registerBeanDefinitions(DefaultListableBeanFactory beanFactory) {

    beanFactory.registerBeanDefinition("springAotApplication",
            SpringAotApplication__BeanDefinitions.getSpringAotApplicationBeanDefinition());

    beanFactory.registerBeanDefinition("greetingController",
            GreetingController__BeanDefinitions.getGreetingControllerBeanDefinition());

    beanFactory.registerBeanDefinition("greetingService",
            GreetingService__BeanDefinitions.getGreetingServiceBeanDefinition());
}
```

The generated class also registers definitions for the Spring Boot infrastructure required by the application.

Examples include:

| Bean                            | Responsibility                                  |
|---------------------------------|-------------------------------------------------|
| `dispatcherServlet`             | Handles incoming HTTP requests                  |
| `requestMappingHandlerMapping`  | Maps HTTP requests to controller methods        |
| `requestMappingHandlerAdapter`  | Invokes controller methods                      |
| `jacksonJsonMapper`             | Provides JSON serialization and deserialization |
| `tomcatServletWebServerFactory` | Configures the embedded Tomcat server           |
| `applicationAvailability`       | Tracks application availability                 |

This demonstrates that AOT processing is not limited to application-defined components.

Spring also generates registration code for the relevant framework and auto-configuration infrastructure.

### Bean Aliases

The generated registration class can also register aliases:

```java
public void registerAliases(DefaultListableBeanFactory beanFactory) {

    beanFactory.registerAlias("applicationTaskExecutor", "bootstrapExecutor");
}
```

An alias provides another name for an existing bean.

It does not create an additional bean instance.

---

## Complete AOT Initialization Flow

The generated classes work together to restore the AOT-processed application context.

Conceptually:

```text
SpringAotApplication
        |
        v
ApplicationContextInitializer
        |
        v
BeanFactoryRegistrations
        |
        +-----------------------------+
        |                             |
        v                             v
GreetingService__BeanDefinitions    GreetingController__BeanDefinitions
        |                             |
        v                             v
GreetingService Bean                GreetingController Bean
                                      |
                                      v
                                GreetingService
```

The registration class also registers the Spring Boot infrastructure required by the application.

This demonstrates the relationship between generated bean definitions, dependency instantiation, and application context initialization.

---

## Spring AOT and Auto-Configuration

Spring Boot auto-configuration provides application infrastructure based on the dependencies and configuration available to the application.

For example, because this application uses Spring MVC, Spring Boot configures the relevant web infrastructure.

During AOT processing, Spring analyzes the applicable auto-configurations and generates corresponding registration code.

The generated `SpringAotApplication__BeanFactoryRegistrations` class contains references to classes such as:

```java
WebMvcAutoConfiguration__BeanDefinitions
DispatcherServletAutoConfiguration__BeanDefinitions
TomcatServletWebServerAutoConfiguration__BeanDefinitions
JacksonAutoConfiguration__BeanDefinitions
```

This illustrates that Spring AOT also prepares the application infrastructure required by Spring Boot.

AOT processing does not mean that every possible auto-configuration is included. Spring evaluates the application's configuration and conditions to determine the relevant infrastructure.

---

## Running the Example

From the repository root:

```bash
mvn -pl 11-graalvm-native/spring-aot spring-boot:run
```

The application starts on port `8091`.

### Greeting Endpoint

Send a GET request:

```bash
curl "http://localhost:8091/api/aot/greeting?name=Mujuzi"
```

Expected response:

```text
Hello, Mujuzi! Welcome to Spring AOT.
```

### Default Greeting

When no name is supplied:

```bash
curl "http://localhost:8091/api/aot/greeting"
```

Expected response:

```text
Hello, Spring AOT! Welcome to Spring AOT.
```

---

## Running the Tests

The example uses Spring Boot's testing support and MockMvc.

### Application Context Test

`SpringAotApplicationTest` verifies that the application context starts and that the main application components are available.

```java
@SpringBootTest
class SpringAotApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldLoadApplicationContext() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBean(GreetingController.class)).isNotNull();
        assertThat(applicationContext.getBean(GreetingService.class)).isNotNull();
    }
}
```

This test verifies that Spring can create the application context and register the required application beans.

### Controller Test

`GreetingControllerTest` uses `@WebMvcTest` to test the REST controller independently of the complete application context.

The service is mocked using `@MockitoBean`.

```java
@WebMvcTest(GreetingController.class)
class GreetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GreetingService greetingService;

    @Test
    void shouldReturnGreeting() throws Exception {
        given(greetingService.greet("Mujuzi")).willReturn("Hello, Mujuzi! Welcome to Spring AOT.");

        mockMvc.perform(get("/api/aot/greeting").param("name", "Mujuzi"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Mujuzi! Welcome to Spring AOT."));
    }

    @Test
    void shouldUseDefaultNameWhenNameIsNotProvided() throws Exception {

        given(greetingService.greet("Spring AOT")).willReturn("Hello, Spring AOT! Welcome to Spring AOT.");

        mockMvc.perform(get("/api/aot/greeting"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Spring AOT! Welcome to Spring AOT."));
    }
}
```

The tests cover:

- Application context initialization.
- Registration of the controller and service beans.
- Successful greeting requests.
- Request parameter handling.
- Default request parameter behavior.

### Run Tests

From the repository root:

```bash
mvn -pl 11-graalvm-native/spring-aot test
```

---

## Running Spring AOT Processing

AOT processing is activated through the Maven profile.

From the repository root:

```bash
mvn -pl 11-graalvm-native/spring-aot -Paot clean process-classes
```

This performs the following operations:

1. Cleans the previous build output.
2. Compiles the application.
3. Activates the `aot` Maven profile.
4. Executes the Spring Boot `process-aot` goal.
5. Generates AOT source files, compiled classes, and resources.

The generated output is available under:

```text
11-graalvm-native/spring-aot/target/spring-aot/main/
```

---

## Inspecting Generated AOT Files

After processing, inspect the generated source directory:

```bash
find 11-graalvm-native/spring-aot/target/spring-aot/main/sources \
    -type f -name "*.java"
```

Inspect the generated application context initializer:

```bash
cat 11-graalvm-native/spring-aot/target/spring-aot/main/sources/com/springbyexample/v2/springaot/SpringAotApplication__ApplicationContextInitializer.java
```

Inspect the generated bean registrations:

```bash
cat 11-graalvm-native/spring-aot/target/spring-aot/main/sources/com/springbyexample/v2/springaot/SpringAotApplication__BeanFactoryRegistrations.java
```

Inspect the controller bean definition:

```bash
cat 11-graalvm-native/spring-aot/target/spring-aot/main/sources/com/springbyexample/v2/springaot/controller/GreetingController__BeanDefinitions.java
```

Inspect the service bean definition:

```bash
cat 11-graalvm-native/spring-aot/target/spring-aot/main/sources/com/springbyexample/v2/springaot/service/GreetingService__BeanDefinitions.java
```

### Verify Generated Directories

Check whether the expected directories exist:

```bash
test -d 11-graalvm-native/spring-aot/target/spring-aot/main/sources \
    && echo "AOT sources generated"

test -d 11-graalvm-native/spring-aot/target/spring-aot/main/classes \
    && echo "AOT classes generated"
```

The presence of generated sources and compiled classes confirms that the AOT processing step produced its output.

---

## AOT Processing vs Normal Application Execution

It is important to distinguish between running the normal application and executing the AOT processing goal.

| Operation                   | Purpose                                                   |
|-----------------------------|-----------------------------------------------------------|
| `spring-boot:run`           | Starts the regular Spring Boot application                |
| `mvn test`                  | Runs the application tests                                |
| `mvn -Paot process-classes` | Generates AOT artifacts                                   |
| AOT-enabled JVM execution   | Starts the application using generated AOT infrastructure |
| Native image compilation    | Produces a platform-specific native executable            |

Generating AOT artifacts does not, by itself, prove that the packaged application is configured to use them.

AOT-enabled JVM execution requires the generated artifacts to be included and configured correctly in the runtime application.

Similarly, generating AOT artifacts does not mean that a native executable has been compiled.

---

## Key Takeaways

- Spring AOT performs supported application analysis and code generation during the build process.
- AOT processing can be used independently of GraalVM native image compilation.
- The Spring Boot Maven Plugin provides the `process-aot` goal.
- The generated output includes Java sources, compiled classes, and resources.
- Spring generates bean definitions for application components.
- Generated bean instance suppliers represent how Spring creates application beans.
- Constructor dependencies are represented explicitly in generated code.
- `ApplicationContextInitializer` helps restore the AOT-processed application context.
- `BeanFactoryRegistrations` connects generated bean definitions to the `BeanFactory`.
- Spring AOT also generates registration code for relevant Spring Boot auto-configuration infrastructure.
- AOT processing does not remove the Spring container or dependency injection.
- Generated AOT artifacts are an important part of Spring's support for GraalVM native images.
- AOT processing, AOT-enabled JVM execution, and native image compilation are separate stages.

---

## Next Example

The next example will explore **Native Image Constraints**, including the restrictions imposed by GraalVM Native Image and how those restrictions influence application design and runtime behavior.
