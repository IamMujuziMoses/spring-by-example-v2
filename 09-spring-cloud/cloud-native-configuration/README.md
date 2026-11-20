# Cloud-Native Configuration

This example demonstrates **cloud-native configuration management** using Spring Boot, Spring Cloud Kubernetes, and Kubernetes ConfigMaps and Secrets.

It focuses on how a Spring Boot application retrieves external configuration from Kubernetes, how configuration values are mapped into application properties, and how sensitive configuration values can be supplied through Kubernetes Secrets.

The example uses a local Kubernetes cluster created with Kind (Kubernetes in Docker) to demonstrate configuration management without requiring a cloud-hosted Kubernetes environment.

## What This Example Demonstrates

- What cloud-native configuration management means.
- Externalized configuration in Spring Boot.
- Kubernetes ConfigMaps.
- Kubernetes Secrets.
- Spring Cloud Kubernetes configuration integration.
- Loading configuration from Kubernetes into a Spring Boot application.
- Binding external configuration using `@ConfigurationProperties`.
- Accessing configuration through application components.
- Separating application configuration from application code.
- Kubernetes ServiceAccounts and RBAC.
- Exposing effective application configuration through a REST endpoint.
- Avoiding exposure of sensitive configuration values.
- Testing configuration property binding.
- Testing REST controller behavior with MockMvc.
- Integration testing against a running Kubernetes deployment.
- Managing Kubernetes-dependent tests separately from ordinary tests.

---

## What Is Cloud-Native Configuration?

Cloud-native configuration is the practice of managing application configuration independently of application code and deployment artifacts.

Instead of embedding environment-specific values directly into an application, configuration is supplied externally by the deployment environment.

For example, an application might need:

- Application name.
- Deployment environment.
- Feature flags.
- Database connection details.
- API credentials.
- External service endpoints.

These values may differ between development, staging, and production environments.

A traditional approach might embed these values in application configuration files or package them into the application artifact.

A cloud-native approach separates the configuration from the application:

```text
Application Code
       |
       v
Application Artifact
       |
       v
Deployment Environment
       |
       v
External Configuration
       |
       v
Running Application
```

This allows the same application artifact to be deployed into different environments with different configuration values.

---

## Why Externalize Configuration?

Consider an application deployed in three environments:

| Property         | Development       | Staging       | Production       |
|------------------|-------------------|---------------|------------------|
| Application name | Demo              | Demo          | Demo             |
| Environment      | development       | staging       | production       |
| Feature enabled  | false             | true          | true             |
| API token        | Development token | Staging token | Production token |

The application should not need to be rebuilt for each environment.

Instead, the deployment environment should provide the appropriate configuration.

This offers several advantages:

- The same application artifact can be reused across environments.
- Configuration can be changed independently of application code.
- Sensitive values can be managed separately from ordinary configuration.
- Deployment configuration can be managed using infrastructure-as-code practices.
- Environment-specific configuration becomes easier to maintain.

---

## Kubernetes Configuration Resources

Kubernetes provides several resources for managing application configuration.

This example focuses on two of them:

1. ConfigMaps.
2. Secrets.

### ConfigMaps

A Kubernetes ConfigMap stores non-confidential configuration data as key-value pairs.

It is useful for configuration values that do not contain sensitive information.

For example:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: cloud-native-config-client
  namespace: cloud-native-configuration
data:
  app.name: Cloud Native Configuration Demo
  app.environment: staging
  app.feature-enabled: "true"
```

This ConfigMap defines three application properties:

| Property              | Value                           |
|-----------------------|---------------------------------|
| `app.name`            | Cloud Native Configuration Demo |
| `app.environment`     | staging                         |
| `app.feature-enabled` | true                            |

Spring Cloud Kubernetes can retrieve these values and make them available to Spring Boot's configuration environment.

The application does not need to know how the ConfigMap is stored or retrieved.

### Secrets

A Kubernetes Secret stores sensitive configuration data, such as credentials, passwords, and API tokens.

For example:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: cloud-native-config-client
  namespace: cloud-native-configuration
type: Opaque
stringData:
  app.api-token: example-api-token
```

The Secret supplies the following property:

```properties
app.api-token=example-api-token
```

Kubernetes Secrets are intended for sensitive data, but they are not automatically encrypted merely because they are represented as Secret resources.

Production deployments should configure encryption at rest, restrict access through RBAC, and avoid committing real secret values to source control.

This example uses demonstration credentials only.

---

## ConfigMaps vs Secrets

ConfigMaps and Secrets serve different configuration purposes.

| Feature             | ConfigMap                                    | Secret                             |
|---------------------|----------------------------------------------|------------------------------------|
| Purpose             | Non-sensitive configuration                  | Sensitive configuration            |
| Typical values      | Application name, environment, feature flags | Passwords, API tokens, credentials |
| Kubernetes resource | `ConfigMap`                                  | `Secret`                           |
| Sensitive data      | Not intended for sensitive data              | Intended for sensitive data        |
| Access control      | Kubernetes RBAC                              | Kubernetes RBAC                    |

A useful rule is:

- Use ConfigMaps for ordinary application configuration.
- Use Secrets for sensitive configuration.

---

## Spring Cloud Kubernetes

Spring Cloud Kubernetes provides integration between Spring applications and Kubernetes resources.

It allows Spring Boot applications to retrieve configuration from Kubernetes ConfigMaps and Secrets.

This example uses the Spring Cloud Kubernetes Fabric8 configuration starter:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes-fabric8-config</artifactId>
</dependency>
```

The Fabric8 implementation communicates with the Kubernetes API to retrieve the required configuration resources.

The application can then consume those values through Spring Boot's normal configuration mechanisms.

### How Configuration Is Loaded

The configuration loading process can be represented as:

```text
Kubernetes API
      |
      +-------------------+
      |                   |
      v                   v
  ConfigMap             Secret
      |                   |
      v                   v
Spring Cloud Kubernetes Configuration Integration
      |
      v
Spring Boot Environment
      |
      v
Configuration Properties
      |
      v
Application Components
```

Spring Cloud Kubernetes retrieves the external configuration and makes it available to the Spring Boot application.

The application consumes the values without directly querying Kubernetes.

---

## Application Configuration

The application uses `application.yml` to configure Spring Cloud Kubernetes.

```yaml
spring:
  application:
    name: cloud-native-config-client

  config:
    import: "optional:kubernetes:"

  cloud:
    kubernetes:
      secrets:
        enabled: true
        enable-api: true

server:
  port: 8080
```

### Understanding the Configuration

#### Application Name

```yaml
spring:
  application:
    name: cloud-native-config-client
```

Defines the Spring Boot application name.

#### Configuration Import

```yaml
spring:
  config:
    import: "optional:kubernetes:"
```

This tells Spring Boot to import configuration from Kubernetes.

The `optional:` prefix means that the application does not have to fail simply because the optional Kubernetes configuration source is unavailable.

It is useful when the same application needs to support environments where Kubernetes configuration is not available.

#### Enabling Secret Integration

```yaml
spring:
  cloud:
    kubernetes:
      secrets:
        enabled: true
        enable-api: true
```

Enables Kubernetes Secret integration and access through the Kubernetes API.

The application requires appropriate Kubernetes permissions to retrieve the resources.

---

## Configuration Properties Binding

Spring Boot provides `@ConfigurationProperties` for binding related configuration values into strongly typed Java objects.

Instead of injecting individual properties throughout the application, configuration can be grouped into a dedicated class.

This example uses an `AppProperties` record.

```java
@ConfigurationProperties(prefix = "app")
public record AppProperties(String name, String environment, boolean featureEnabled, String apiToken) {
}
```

### Understanding `@ConfigurationProperties`

The annotation:

```java
@ConfigurationProperties(prefix = "app")
```

tells Spring Boot to bind properties beginning with `app` to this record.

For example:

```yaml
app:
  name: Cloud Native Configuration Demo
  environment: staging
  feature-enabled: true
  api-token: example-api-token
```

These properties are mapped to:

| Configuration property | Java field       |
|------------------------|------------------|
| `app.name`             | `name`           |
| `app.environment`      | `environment`    |
| `app.feature-enabled`  | `featureEnabled` |
| `app.api-token`        | `apiToken`       |

Spring Boot handles relaxed binding, allowing kebab-case configuration names to map to camelCase Java fields.

### Configuration Properties Scanning

The main application class enables configuration properties scanning:

```java
@SpringBootApplication
@ConfigurationPropertiesScan
public class ConfigClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigClientApplication.class, args);
    }
}
```

`@ConfigurationPropertiesScan` discovers classes annotated with `@ConfigurationProperties` and registers them as Spring-managed configuration properties beans.

This makes `AppProperties` available for dependency injection.

---

## Exposing Configuration Through a REST Endpoint

The example exposes the effective application configuration through:

```http
GET /api/configuration
```

The endpoint allows us to observe which configuration values have been loaded into the application.

### Configuration Response

The response is represented by a record:

```java
public record ConfigurationResponse(String name, String environment, boolean featureEnabled, boolean apiTokenConfigured) {
}
```

Notice that the response does not contain the actual API token.

Instead, it only reports whether a token has been configured.

### Configuration Controller

```java
@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private final AppProperties appProperties;

    public ConfigurationController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping
    public ConfigurationResponse getConfiguration() {
        return new ConfigurationResponse(appProperties.name(), appProperties.environment(), appProperties.featureEnabled(),
                appProperties.apiToken() != null && !appProperties.apiToken().isBlank());
    }
}
```

The controller receives `AppProperties` through constructor injection.

It reads the effective configuration and constructs a response.

### Why Not Return the Actual API Token?

Sensitive configuration should not be exposed through application endpoints.

Returning an API token could allow anyone with access to the endpoint to retrieve credentials.

Instead, this example exposes:

```json
{
  "name": "Cloud Native Configuration Demo",
  "environment": "staging",
  "featureEnabled": true,
  "apiTokenConfigured": true
}
```

This demonstrates that the application successfully loaded the Secret without revealing its contents.

---

## Kubernetes Architecture

The example uses the following architecture:

```text
                         Kubernetes Cluster
                                  |
                  +---------------+---------------+
                  |                               |
                  v                               v
              ConfigMap                         Secret
                  |                               |
                  +---------------+---------------+
                                  |
                                  v
                         Kubernetes API
                                  |
                                  v
                         Spring Cloud Kubernetes
                                  |
                                  v
                         Spring Boot Application
                                  |
                                  v
                         Configuration Properties
                                  |
                                  v
                         Configuration Controller
                                  |
                                  v
                         REST Endpoint
                         /api/configuration
```

The application runs inside Kubernetes and retrieves configuration from the Kubernetes API.

A Kubernetes Service exposes the application inside the cluster.

A local port-forward makes the service accessible from the development machine.

---

## Kubernetes Namespace

The example uses a dedicated namespace:

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: cloud-native-configuration
```

Namespaces provide a way to organize and isolate Kubernetes resources within a cluster.

The ConfigMap, Secret, ServiceAccount, RBAC resources, Deployment, and Service belong to this namespace.

Using a dedicated namespace also helps restrict the scope of permissions granted to the application.

---

## ServiceAccount and RBAC

The application needs permission to retrieve ConfigMaps and Secrets from Kubernetes.

Kubernetes uses ServiceAccounts and Role-Based Access Control (RBAC) to manage application permissions.

### ServiceAccount

The example defines a ServiceAccount:

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: cloud-native-config-client
  namespace: cloud-native-configuration
```

The application runs using this ServiceAccount.

### Role

A Kubernetes Role grants the required permissions:

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: cloud-native-config-reader
  namespace: cloud-native-configuration
rules:
  - apiGroups: [""]
    resources:
      - configmaps
      - secrets
    verbs:
      - get
      - list
      - watch
```

The permissions allow the application to retrieve and observe the required configuration resources.

### RoleBinding

A RoleBinding associates the Role with the ServiceAccount:

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: cloud-native-config-reader
  namespace: cloud-native-configuration
subjects:
  - kind: ServiceAccount
    name: cloud-native-config-client
    namespace: cloud-native-configuration
roleRef:
  kind: Role
  name: cloud-native-config-reader
  apiGroup: rbac.authorization.k8s.io
```

The resulting relationship is:

```text
ServiceAccount
      |
      v
RoleBinding
      |
      v
Role
      |
      v
Permissions
      |
      v
ConfigMaps and Secrets
```

### Security Considerations

The example grants `get`, `list`, and `watch` permissions for ConfigMaps and Secrets within the dedicated namespace.

These permissions should be reviewed carefully in production.

In particular, `list` and `watch` permissions on Secrets can expose more information than access to a single named Secret.

Production deployments should follow the principle of least privilege and restrict access to only the resources the application requires.

---

## Running the Example with Kind

Kind allows Kubernetes clusters to run locally using Docker containers.

This makes it useful for learning and testing Kubernetes integrations without requiring a remote Kubernetes environment.

### Create a Kind Cluster

Create a cluster named `spring-cloud-examples`:

```bash
kind create cluster --name spring-cloud-examples
```

Verify the cluster:

```bash
kubectl cluster-info
```

Check the available nodes:

```bash
kubectl get nodes
```

### Build the Application

From the repository root:

```bash
mvn -pl 09-spring-cloud/cloud-native-configuration/config-client -am clean package -DskipTests
```

This builds the Spring Boot application.

The executable JAR is produced with the `exec` classifier:

```text
config-client/target/config-client-1.0-SNAPSHOT-exec.jar
```

### Build the Docker Image

From the repository root:

```bash
docker build -t cloud-native-config-client:1.0-SNAPSHOT 09-spring-cloud/cloud-native-configuration/config-client
```

The Docker image contains the executable Spring Boot application.

### Load the Image into Kind

Kind uses its own container image environment.

Load the locally built image into the cluster:

```bash
kind load docker-image cloud-native-config-client:1.0-SNAPSHOT --name spring-cloud-examples
```

This makes the application image available to Kubernetes without requiring an external image registry.

---

## Deploying Kubernetes Resources

The Kubernetes manifests are located in:

```text
09-spring-cloud/cloud-native-configuration/kubernetes/
```

Apply the namespace and configuration resources before deploying the application.

For example:

```bash
kubectl apply -f 09-spring-cloud/cloud-native-configuration/kubernetes/namespace.yaml
kubectl apply -f 09-spring-cloud/cloud-native-configuration/kubernetes/configmap.yaml
kubectl apply -f 09-spring-cloud/cloud-native-configuration/kubernetes/secret.yaml
kubectl apply -f 09-spring-cloud/cloud-native-configuration/kubernetes/service-account.yaml
kubectl apply -f 09-spring-cloud/cloud-native-configuration/kubernetes/role.yaml
kubectl apply -f 09-spring-cloud/cloud-native-configuration/kubernetes/role-binding.yaml
```

Then deploy the application:

```bash
kubectl apply -f 09-spring-cloud/cloud-native-configuration/kubernetes/deployment.yaml
kubectl apply -f 09-spring-cloud/cloud-native-configuration/kubernetes/service.yaml
```

Check the deployed resources:

```bash
kubectl get all -n cloud-native-configuration
```

Check the application pod:

```bash
kubectl get pods -n cloud-native-configuration
```

Inspect application logs:

```bash
kubectl logs -n cloud-native-configuration deployment/cloud-native-config-client
```

---

## Accessing the Application

The application is exposed through a Kubernetes ClusterIP Service.

To access it locally, use port-forwarding:

```bash
kubectl port-forward -n cloud-native-configuration service/cloud-native-config-client 8080:8080
```

The application is now accessible at:

```text
http://localhost:8080
```

### Retrieve the Effective Configuration

Send a request to the configuration endpoint:

```bash
curl http://localhost:8080/api/configuration
```

Example response:

```json
{
  "name": "Cloud Native Configuration Demo",
  "environment": "staging",
  "featureEnabled": true,
  "apiTokenConfigured": true
}
```

The response confirms that the application has successfully loaded its configuration from Kubernetes.

---

## Updating Configuration

One of the main advantages of externalized configuration is that configuration values can be changed independently of the application artifact.

This example demonstrates updating the ConfigMap without rebuilding the Docker image.

### Update the ConfigMap

For example, change the environment from `kubernetes` to `staging`:

```bash
kubectl patch configmap cloud-native-config-client -n cloud-native-configuration --type merge \
  -p '{"data":{"app.environment":"staging"}}'
```

The ConfigMap now contains the updated value.

### Restart the Application

The example uses the Kubernetes configuration import mechanism without demonstrating automatic refresh of application properties.

Restart the deployment so the application loads the updated configuration:

```bash
kubectl rollout restart deployment/cloud-native-config-client -n cloud-native-configuration
```

Wait for the rollout:

```bash
kubectl rollout status deployment/cloud-native-config-client -n cloud-native-configuration
```

Retrieve the configuration again:

```bash
curl http://localhost:8080/api/configuration
```

The response should now contain:

```json
{
  "name": "Cloud Native Configuration Demo",
  "environment": "staging",
  "featureEnabled": true,
  "apiTokenConfigured": true
}
```

The important point is that the application image did not need to be rebuilt.

Only the external configuration changed.

### Updating a Secret

The Secret can also be updated independently.

For example:

```bash
kubectl patch secret cloud-native-config-client -n cloud-native-configuration --type merge \
  -p '{"stringData":{"app.api-token":"updated-example-token"}}'
```

Restart the application to load the updated Secret:

```bash
kubectl rollout restart deployment/cloud-native-config-client -n cloud-native-configuration
```

The endpoint continues to report whether the API token is configured without exposing its value.

---

## Configuration Reloading vs Application Restart

An important distinction is that externalized configuration does not automatically mean that every configuration change is immediately applied to a running application.

The behavior depends on how the configuration is consumed and which refresh mechanisms are enabled.

This example demonstrates configuration loading during application startup.

After changing a ConfigMap or Secret, the application is restarted to load the new values.

Other Spring Cloud Kubernetes capabilities can support configuration change detection and refresh workflows, but those are outside the scope of this example.

---

## Testing Cloud-Native Configuration

The example keeps all test classes in a dedicated module, this separates production application code from the testing infrastructure.

The `cnc-integration-tests` module depends on `config-client` and contains the tests for its configuration binding, REST behavior, and Kubernetes integration.

### AppPropertiesTest

This test verifies that Spring Boot correctly binds external configuration properties to `AppProperties`.

It uses `ApplicationContextRunner` to create a lightweight application context.

The test covers:

- Binding application configuration properties.
- Converting feature flags into boolean values.
- Handling a missing API token.

The test does not require Kubernetes.

### ConfigurationControllerIT

This test verifies the REST controller behavior using Spring's MockMvc.

It uses standalone MockMvc setup to exercise the actual controller without starting the full Spring Boot application.

The test covers:

- Returning the application name.
- Returning the deployment environment.
- Returning the feature flag.
- Reporting whether an API token is configured.
- Ensuring the actual API token is not included in the response.

The test does not require Kubernetes.

### KubernetesConfigurationIT

This test verifies the application running in Kubernetes through its HTTP endpoint.

It sends a request to:

```http
GET /api/configuration
```

It verifies that the deployed application has successfully loaded the expected values from the Kubernetes ConfigMap and Secret.

Unlike the other tests, this test requires:

- A running Kind cluster.
- The Kubernetes resources to be deployed.
- The application pod to be running.
- A working port-forward to the application service.

---

## Managing Kubernetes-Dependent Tests

Kubernetes integration tests should not prevent ordinary Maven builds from running when a Kubernetes environment is unavailable.

This example uses JUnit's `@EnabledIfSystemProperty` to control execution of the Kubernetes-dependent test.

```java
@EnabledIfSystemProperty(named = "skipCloudNativeConfigurationITs", matches = "false")
class KubernetesConfigurationIT {
    // Kubernetes integration tests
}
```

The Maven property is configured in the root `pom.xml`:

```xml
<properties>
    <skipCloudNativeConfigurationITs>true</skipCloudNativeConfigurationITs>
</properties>
```

The Failsafe plugin uses the same property:

```xml
<configuration>
    <skipITs>${skipCloudNativeConfigurationITs}</skipITs>
</configuration>
```

This gives the project two execution modes.

### Ordinary Build

```bash
mvn -pl :cnc-integration-tests -am clean verify
```

The Kubernetes-dependent test is skipped.

The configuration binding and controller tests still execute.

### Kubernetes-Enabled Build

```bash
mvn -pl :cnc-integration-tests -am clean verify -DskipCloudNativeConfigurationITs=false
```

The Kubernetes integration test is enabled.

The Kind cluster and application must already be running and accessible.

### Test Execution Summary

| Test class                  | Ordinary build | Kubernetes-enabled build |
|-----------------------------|----------------|--------------------------|
| `AppPropertiesTest`         | Runs           | Runs                     |
| `ConfigurationControllerIT` | Runs           | Runs                     |
| `KubernetesConfigurationIT` | Skipped        | Runs                     |

The `@EnabledIfSystemProperty` annotation and Maven Failsafe configuration work together to make the Kubernetes test explicitly opt-in.

---

### Module Responsibilities

| Module                  | Responsibility                                                           |
|-------------------------|--------------------------------------------------------------------------|
| `config-client`         | Spring Boot application that consumes Kubernetes configuration           |
| `cnc-integration-tests` | Configuration binding, REST controller, and Kubernetes integration tests |
| `kubernetes`            | Kubernetes manifests and deployment configuration                        |

The executable Spring Boot JAR is packaged separately from the ordinary dependency JAR so that the application can be deployed while its classes remain available to the integration-test module.

---

## Running the Tests

Run all non-Kubernetes tests from the repository root:

```bash
mvn -pl :cnc-integration-tests -am clean verify
```

Run all tests, including the Kubernetes integration test:

```bash
mvn -pl :cnc-integration-tests -am clean verify -DskipCloudNativeConfigurationITs=false
```

The Kubernetes-enabled command requires the deployed application to be reachable at the configured base URL.

The default URL is:

```text
http://localhost:8080
```

It can be overridden through the Maven property:

```bash
mvn -pl :cnc-integration-tests -am clean verify -DskipCloudNativeConfigurationITs=false \
  -DcloudNativeConfigurationBaseUrl=http://localhost:8080
```

---

## Key Takeaways

- Cloud-native configuration separates application configuration from application code.
- Externalized configuration allows the same application artifact to be deployed across different environments.
- Kubernetes ConfigMaps store non-sensitive configuration.
- Kubernetes Secrets are intended for sensitive configuration.
- Spring Cloud Kubernetes integrates Kubernetes configuration resources with Spring Boot.
- Spring Boot's `@ConfigurationProperties` provides strongly typed configuration binding.
- `@ConfigurationPropertiesScan` discovers and registers configuration properties classes.
- Application components can consume external configuration through dependency injection.
- Sensitive configuration should never be exposed through application endpoints.
- Kubernetes ServiceAccounts and RBAC control access to configuration resources.
- A dedicated Kubernetes namespace helps scope application permissions.
- Configuration changes do not necessarily take effect immediately in a running application.
- Restarting the application allows it to reload configuration supplied at startup.
- Spring Cloud Kubernetes supports additional configuration refresh capabilities.
- Kubernetes-dependent integration tests can be controlled independently of ordinary Maven builds.
- Keeping tests in a dedicated module separates testing concerns from production application code.

---

## Next Example

This example completes the planned **Spring Cloud** examples in Module 09.
