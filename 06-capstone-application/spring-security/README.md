# Spring Security

This example demonstrates how to secure a Spring Boot REST API using Spring Security. It focuses on authentication, authorization, password encoding, role-based access control, stateless session management, and testing security rules.

The example is part of the **Spring by Example V2** Capstone Application module. It provides a small, self-contained application for understanding Spring Security without introducing database persistence, domain entities, or unrelated business logic.

## What This Example Demonstrates

- Configuring Spring Security using a `SecurityFilterChain` bean.
- Understanding authentication and authorization.
- Implementing HTTP Basic authentication.
- Defining in-memory users with different roles.
- Encoding passwords with `BCryptPasswordEncoder`.
- Restricting endpoints using role-based authorization.
- Configuring stateless session management.
- Allowing public access to selected endpoints.
- Denying access to unconfigured endpoints by default.
- Understanding the difference between `401 Unauthorized` and `403 Forbidden`.
- Testing security rules using MockMvc and Spring Security Test.
- Writing unit-level configuration checks and full application integration tests.

---

## Overview

Spring Security is a framework that provides authentication, authorization, and protection against common security attacks for Spring applications.

Authentication determines **who is making a request**, while authorization determines **what that authenticated user is allowed to do**.

For example, an application might allow anyone to view a public resource, require an authenticated user to view a profile, and restrict administrative reports to users with the `ADMIN` role.

This example implements these rules through a small REST API.

### Application Architecture

The application uses a straightforward structure, the responsibilities are separated as follows:

| Component                       | Responsibility                                                                       |
|---------------------------------|--------------------------------------------------------------------------------------|
| `SpringSecurityApplication`     | Starts the Spring Boot application.                                                  |
| `SecurityDemoController`        | Exposes public, user-protected, and administrator-protected endpoints.               |
| `SecurityConfiguration`         | Configures authentication, authorization, password encoding, and session management. |
| `SecurityDemoControllerTest`    | Tests the API endpoints and their expected responses.                                |
| `SecurityConfigurationTest`     | Verifies password encoding and in-memory user configuration.                         |
| `SpringSecurityIntegrationTest` | Tests security behavior across the complete application.                             |

---

## 1. Spring Security Request Processing

Spring Security integrates with the Servlet application through a chain of servlet filters. Incoming requests pass through the security filters before reaching the controller.

A simplified request flow looks like this:

```text
                    HTTP Request
                         |
                         v
              +----------------------+
              |   Security Filters   |
              +----------------------+
                         |
                         v
              +----------------------+
              | Authentication       |
              | (when required)      |
              +----------------------+
                         |
                         v
              +----------------------+
              | Authorization Rules  |
              +----------------------+
                         |
               +---------+---------+
               |                   |
               v                   v
          Access Granted      Access Denied
               |                   |
               v                   v
         REST Controller     Security Response
               |
               v
          HTTP Response
```

The diagram is conceptual. Spring Security's actual filter chain contains multiple filters, and authentication and authorization are handled by the relevant filters and security components.

The important principle is that requests are evaluated against security rules before protected controller methods are executed.

---

## 2. Authentication and Authorization

Although closely related, authentication and authorization solve different problems.

### Authentication

Authentication verifies the identity of a request.

In this example, HTTP Basic authentication supplies a username and password with the request. Spring Security uses the configured `UserDetailsService` and `PasswordEncoder` to authenticate the credentials.

The application defines two users:

| Username | Password          | Roles           |
|----------|-------------------|-----------------|
| `reader` | `reader-password` | `USER`          |
| `admin`  | `admin-password`  | `USER`, `ADMIN` |

These credentials are for demonstration purposes only and must not be used in a production application.

### Authorization

Authorization determines whether an authenticated user has permission to access a resource.

This example uses role-based access control:

- `USER` can access the user profile.
- `ADMIN` can access administrative reports.
- `ADMIN` also has the `USER` role and can therefore access the user profile.
- Unauthenticated users can access the public endpoint.
- Other requests are denied by default.

An authenticated user does not automatically have permission to access every endpoint. The configured authorization rules determine the permitted operations.

---

## 3. Application Entry Point

`SpringSecurityApplication.java` is the entry point for the Spring Boot application.

```java
@SpringBootApplication
public class SpringSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityApplication.class, args);
    }
}
```

The `@SpringBootApplication` annotation enables Spring Boot auto-configuration and component scanning for the application package.

The application discovers the REST controller and security configuration because they reside within the application's package hierarchy.

---

## 4. Configuring Spring Security

The central part of this example is `SecurityConfiguration.java`.

It defines the security filter chain, password encoder, and in-memory users.

### Security Filter Chain

Modern Spring Security applications commonly configure HTTP security through a `SecurityFilterChain` bean.

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {
    http.sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(HttpMethod.GET, "/api/public")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/user/profile")
                    .hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/admin/reports")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .denyAll())
            .httpBasic(Customizer.withDefaults());

    return http.build();
}
```

Each part of the configuration serves a specific purpose.

### Stateless Session Management

```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

`SessionCreationPolicy.STATELESS` configures Spring Security not to create an HTTP session or use one to obtain the security context for subsequent requests.

Each request to a protected endpoint must provide the credentials needed for authentication.

This approach is common for APIs that use HTTP Basic authentication, bearer tokens, or other request-based authentication mechanisms.

Stateless authentication does not mean that the server has no state of any kind. It means that HTTP session state is not used to maintain the authenticated security context between requests.

### Public Endpoint

```java
.requestMatchers(HttpMethod.GET, "/api/public").permitAll()
```

`permitAll()` allows requests matching this rule without requiring authentication.

The public endpoint is useful for demonstrating how a specific resource can be excluded from authentication requirements while the rest of the API remains protected.

### User and Administrator Access

```java
.requestMatchers(HttpMethod.GET, "/api/user/profile").hasAnyRole("USER", "ADMIN")
```

This rule allows authenticated users with either the `USER` or `ADMIN` role to access the user profile.

The administrator endpoint uses a more restrictive rule:

```java
.requestMatchers(HttpMethod.GET, "/api/admin/reports").hasRole("ADMIN")
```

Only authenticated users with the `ADMIN` role can access this endpoint.

The methods `hasRole("ADMIN")` and `hasAnyRole("USER", "ADMIN")` expect role names without the `ROLE_` prefix. Spring Security applies the conventional prefix when checking the granted authorities.

For example, `hasRole("ADMIN")` checks for the `ROLE_ADMIN` authority.

### Denying Unconfigured Requests

```java
.anyRequest().denyAll()
```

This rule denies requests that do not match an earlier authorization rule.

Using a default-deny policy makes the configuration explicit: an endpoint is accessible only when a rule permits it.

If another endpoint is added to the application, it will remain inaccessible until the security configuration explicitly grants the required access.

This reduces the risk of unintentionally exposing new endpoints.

### HTTP Basic Authentication

```java
.httpBasic(Customizer.withDefaults())
```

This enables HTTP Basic authentication.

Clients send an `Authorization` header containing the username and password encoded using Base64. Base64 is not encryption, so HTTP Basic authentication must be used over HTTPS outside local development and testing environments.

When authentication fails or is missing for a protected endpoint, Spring Security normally returns `401 Unauthorized`.

HTTP Basic is used here because it is simple to demonstrate. A production API may instead use OAuth 2.0 resource-server support, bearer tokens, or another authentication mechanism appropriate to its requirements.

---

## 5. Password Encoding

The example uses BCrypt to encode passwords.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Passwords should not be stored as plain text. A password encoder transforms a raw password into a stored representation that can be checked later.

BCrypt is a password-hashing algorithm designed for password storage. It uses a salt and a configurable computational cost to make password guessing more expensive.

When a user authenticates, Spring Security compares the supplied password with the stored encoded password using the configured encoder.

The application never needs to retrieve the original password from the encoded value.

For example:

```java
PasswordEncoder encoder = new BCryptPasswordEncoder();

String rawPassword = "example-password";
String encodedPassword = encoder.encode(rawPassword);

boolean matches = encoder.matches(rawPassword, encodedPassword);
```

The `matches` method returns `true` when the raw password matches the encoded password.

BCrypt generally produces different encoded strings when the same raw password is encoded more than once because a random salt is used. Tests should therefore verify password matching rather than compare encoded strings for equality.

---

## 6. In-Memory Users

The example defines its users through an `InMemoryUserDetailsManager`.

```java
@Bean
public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    var reader = User.withUsername("reader").password(passwordEncoder.encode("reader-password"))
            .roles("USER").build();

    var admin = User.withUsername("admin").password(passwordEncoder.encode("admin-password"))
            .roles("USER", "ADMIN").build();

    return new InMemoryUserDetailsManager(reader, admin);
}
```

The `UserDetailsService` provides user details to Spring Security during authentication.

`InMemoryUserDetailsManager` stores the configured users in memory, making it suitable for examples, demonstrations, and some testing scenarios.

The `reader` user has only the `USER` role. The `admin` user has both `USER` and `ADMIN`.

The passwords are encoded through the configured `PasswordEncoder` before the user records are built.

This example deliberately avoids database-backed user management so that the focus remains on Spring Security configuration.

In a production application, users would typically be managed through a persistent identity store, an external identity provider, or another appropriate authentication system.

---

## 7. REST API Endpoints

`SecurityDemoController.java` exposes three endpoints.

```java
@RestController
public class SecurityDemoController {

    @GetMapping("/api/public")
    public Map<String, String> publicResource() {
        return Map.of("message", "This resource is publicly accessible.");
    }

    @GetMapping("/api/user/profile")
    public Map<String, String> userProfile() {
        return Map.of("message", "You have accessed the user profile.", "access", "USER");
    }

    @GetMapping("/api/admin/reports")
    public Map<String, String> adminReports() {
        return Map.of("message", "You have accessed the administrator reports.", "access", "ADMIN");
    }
}
```

The controller uses `@RestController` to expose its methods as REST endpoints.

Each method returns a small map that Spring MVC serializes to JSON.

### Endpoint Access Rules

| HTTP Method | Endpoint              | Required Access   | Expected Result          |
|-------------|-----------------------|-------------------|--------------------------|
| `GET`       | `/api/public`         | Public            | `200 OK`                 |
| `GET`       | `/api/user/profile`   | `USER` or `ADMIN` | `200 OK` when authorized |
| `GET`       | `/api/admin/reports`  | `ADMIN`           | `200 OK` when authorized |
| `GET`       | `/api/not-configured` | Denied by default | `403 Forbidden`          |

For protected endpoints, missing or invalid credentials normally produce `401 Unauthorized`. Valid credentials without the required authority produce `403 Forbidden`.

The final row assumes the request is authenticated. An unauthenticated request to an unmatched endpoint can be rejected through the authentication entry point before the default-deny authorization rule produces a response.

---

## 8. Understanding HTTP 401 and 403

Two important HTTP status codes appear throughout the example.

### 401 Unauthorized

Despite its name, `401 Unauthorized` generally indicates that the request has not been successfully authenticated.

Examples include:

- Accessing the user profile without credentials.
- Accessing administrative reports without credentials.
- Supplying an invalid username or password.

### 403 Forbidden

`403 Forbidden` indicates that the request has been authenticated but does not have permission to access the resource.

For example, the `reader` user can authenticate successfully but does not have the `ADMIN` role. Attempting to access `/api/admin/reports` therefore results in `403 Forbidden`.

```text
Request to a protected endpoint
             |
             v
      Credentials valid?
        /          \
      No            Yes
      |              |
      v              v
  401 response   Has required role?
                   /       \
                 No         Yes
                 |           |
                 v           v
             403 response  Controller
                              |
                              v
                           200 response
```

This distinction is important when designing APIs and writing tests because authentication failures and authorization failures represent different security conditions.

---

## 9. Testing the Example

The example includes three test classes that cover different aspects of the application.

### SecurityDemoControllerTest

This test class verifies successful access to the API endpoints.

It uses Spring Boot's test support and MockMvc to execute requests against the application without starting a real HTTP server.

The tests verify that:

- The public endpoint returns a successful response without credentials.
- The `reader` user can access the user profile.
- The `admin` user can access administrative reports.
- The expected JSON response fields are present.

A typical test looks like this:

```java
mockMvc.perform(get("/api/user/profile")
        .with(httpBasic("reader", "reader-password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access").value("USER"));
```

The `httpBasic` request post-processor supplies HTTP Basic credentials to the request. Spring Security then processes those credentials using the application's configured authentication components.

### SecurityConfigurationTest

This test class checks the security configuration's core components.

It verifies that:

- The configured password encoder is a `BCryptPasswordEncoder`.
- A raw password matches its encoded representation.
- A different password does not match.
- The `reader` user has the `ROLE_USER` authority but not `ROLE_ADMIN`.
- The `admin` user has both `ROLE_USER` and `ROLE_ADMIN`.
- The configured passwords match the corresponding stored user details.

These checks help catch mistakes in password encoding and role assignment without requiring every condition to be verified through an HTTP request.

### SpringSecurityIntegrationTest

This test class verifies security behavior across the running Spring application.

It covers scenarios such as:

| Scenario                                          | Expected Status    |
|---------------------------------------------------|--------------------|
| Unauthenticated request to the user profile       | `401 Unauthorized` |
| Unauthenticated request to administrative reports | `401 Unauthorized` |
| Request with invalid credentials                  | `401 Unauthorized` |
| `reader` accessing administrative reports         | `403 Forbidden`    |
| `admin` accessing the user profile                | `200 OK`           |
| Authenticated request to an unconfigured endpoint | `403 Forbidden`    |

Integration tests are valuable because they exercise the security filter chain, authentication mechanism, authorization rules, and controller mappings together.

### MockMvc and HTTP Basic

Spring Security Test provides the `httpBasic` request post-processor, which is useful when the goal is to verify the application's actual HTTP Basic authentication configuration.

```java
mockMvc.perform(get("/api/admin/reports").with(httpBasic("admin", "admin-password"))).andExpect(status().isOk());
```

By contrast, Spring Security Test also provides helpers such as `user("reader")`, which can populate a mock authenticated user for a test.

That can be useful when testing authorization behavior in isolation, but it does not verify the username and password authentication flow in the same way as supplying HTTP Basic credentials.

This example uses HTTP Basic credentials in its request tests so that the configured authentication mechanism is exercised.

---

## 10. Running the Example

Run the commands from the repository root.

### Run the Tests

```bash
./mvnw -pl 06-capstone-application/spring-security test
```

To include the example and any required reactor dependencies, use:

```bash
./mvnw -pl 06-capstone-application/spring-security -am test
```

### Build the Example

```bash
./mvnw -pl 06-capstone-application/spring-security -am package
```

These commands assume the module is registered in the root Maven POM and that the project uses the Maven Wrapper.

### Start the Application

```bash
./mvnw -pl 06-capstone-application/spring-security spring-boot:run
```

The application can then be accessed at `http://localhost:8080`.

### Test the Public Endpoint

```bash
curl -i http://localhost:8080/api/public
```

Expected status:

```text
HTTP/1.1 200
```

Example response:

```json
{
  "message": "This resource is publicly accessible."
}
```

### Test the User Profile

Without credentials:

```bash
curl -i http://localhost:8080/api/user/profile
```

Expected status:

```text
HTTP/1.1 401
```

With valid reader credentials:

```bash
curl -i -u reader:reader-password \
  http://localhost:8080/api/user/profile
```

Expected status:

```text
HTTP/1.1 200
```

Example response:

```json
{
  "message": "You have accessed the user profile.",
  "access": "USER"
}
```

### Test Administrative Access

Using the reader account:

```bash
curl -i -u reader:reader-password \
  http://localhost:8080/api/admin/reports
```

Expected status:

```text
HTTP/1.1 403
```

Using the administrator account:

```bash
curl -i -u admin:admin-password \
  http://localhost:8080/api/admin/reports
```

Expected status:

```text
HTTP/1.1 200
```

Example response:

```json
{
  "message": "You have accessed the administrator reports.",
  "access": "ADMIN"
}
```

### Test an Unconfigured Endpoint

```bash
curl -i -u admin:admin-password \
  http://localhost:8080/api/not-configured
```

Because the request is authenticated but does not match an explicitly permitted rule, the default-deny configuration rejects it.

Expected status:

```text
HTTP/1.1 403
```

---

## 11. Important Security Considerations

This example intentionally keeps the security configuration small. A production application requires additional decisions based on its architecture and threat model.

### Use HTTPS

HTTP Basic credentials are only Base64-encoded in the request header. They are not encrypted by Base64.

Use HTTPS to protect credentials and other request data in transit.

### Protect Credentials

The example's usernames and passwords are hard-coded to keep the demonstration easy to run. They are not suitable for production.

Production credentials should be managed securely and should never be committed to source control.

### Choose an Appropriate Authentication Mechanism

HTTP Basic is useful for learning the authentication flow, but it may not be the right choice for every application.

Depending on the requirements, consider OAuth 2.0, OpenID Connect, or Spring Security's OAuth 2.0 Resource Server support.

### Review CSRF Requirements

Spring Security's CSRF protection remains enabled in this example.

CSRF protection is particularly important for applications that authenticate requests through browser-managed credentials, such as session cookies.

For APIs that do not use browser-automatically-sent credentials, the appropriate CSRF configuration depends on the authentication design and client behavior. Stateless session management alone does not automatically make every application safe from CSRF.

### Use Appropriate User Management

In-memory users are useful for small examples but are not a substitute for production identity management.

Real applications may use a database-backed `UserDetailsService`, an external identity provider, or a centralized identity system.

### Apply Least Privilege

Give each user only the authorities needed to perform their responsibilities.

The example demonstrates this principle by limiting administrative reports to users with the `ADMIN` role.

### Keep Default-Deny Authorization

A default-deny policy helps ensure that new endpoints are not automatically exposed when developers add controller methods.

Review access rules whenever new endpoints or HTTP methods are introduced.

### Consider Method-Level Security

Request-level authorization is useful for protecting API routes. Some applications also need method-level authorization to protect service operations independently of the HTTP endpoint through which they are called.

Spring Security supports method-level security through annotations such as `@PreAuthorize`, when enabled with the appropriate configuration.

---

## Key Takeaways

- Spring Security processes requests through a security filter chain before they reach protected controllers.
- Authentication establishes the identity of a requester; authorization determines which resources that identity can access.
- `SecurityFilterChain` provides a clear way to configure HTTP security.
- HTTP Basic is a simple authentication mechanism that should be protected by HTTPS.
- `PasswordEncoder` and `BCryptPasswordEncoder` support secure password verification.
- `InMemoryUserDetailsManager` is useful for demonstrations and lightweight testing scenarios.
- `hasRole` and `hasAnyRole` make role-based access rules straightforward.
- Stateless session management means the application does not use HTTP sessions to maintain the security context across requests.
- `401 Unauthorized` and `403 Forbidden` represent different authentication and authorization outcomes.
- A default-deny policy prevents unconfigured endpoints from becoming accessible by accident.
- MockMvc and Spring Security Test make it possible to test security behavior without starting a real HTTP server.

---

## Next Steps

After understanding the fundamentals of **Spring Security**, explore how security integrates with the other technologies in the Capstone Application module, including transaction management, Spring Modulith, and application testing.

> The goal is to understand each technology independently before combining them into a cohesive Spring application.
