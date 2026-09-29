# Spring Security Testing

Spring Security provides dedicated testing support through `spring-security-test` for testing authentication, authorization, CSRF protection, and method-level security.

This example focuses on testing secured Spring MVC applications with MockMvc and Spring Security Test request post processors.

The example demonstrates `user()`, `anonymous()`, `csrf()`, `jwt()`, role-based authorization, authentication testing, method security testing, and the difference between `401 Unauthorized` and `403 Forbidden`.

## Learning Objectives

By completing this example, you will understand:

- What `spring-security-test` provides
- How to test Spring Security with MockMvc
- How to create authenticated users with `user()`
- How to test anonymous requests with `anonymous()`
- How to test role-based authorization
- How to test CSRF protection with `csrf()`
- How to test method-level security
- How to create a mock JWT authentication with `jwt()`
- How to test the authenticated user exposed to a controller
- The difference between `401 Unauthorized` and `403 Forbidden`
- How Spring Security Test populates the security context
- Why security tests do not always need to perform a real authentication flow

---

## What Is Spring Security Test?

`spring-security-test` is the Spring Security testing module that provides utilities for testing secured applications.

Instead of manually constructing authentication objects and security contexts, tests can use request post processors such as:

```text
user()
anonymous()
csrf()
jwt()
```

Conceptually:

```text
MockMvc Request
      │
      ▼
Spring Security Test
      │
      ├── user()
      ├── anonymous()
      ├── csrf()
      └── jwt()
      │
      ▼
SecurityContext
      │
      ▼
Spring Security
      │
      ▼
Controller
```

This allows tests to focus on security behavior without requiring every test to perform a complete login or authentication flow.

---

## MockMvc Security Testing Flow

This example uses MockMvc to send requests through the application's Spring Security filter chain.

The general flow is:

```text
Test
 │
 ▼
MockMvc
 │
 ▼
Security Test Request Post Processor
 │
 ├── user()
 ├── anonymous()
 ├── csrf()
 └── jwt()
 │
 ▼
SecurityContext
 │
 ▼
SecurityFilterChain
 │
 ▼
Authorization
 │
 ▼
Controller
```

This makes it possible to test security behavior at the HTTP layer while controlling the authentication state directly from the test.

---

## Dependencies

The example uses:

- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-oauth2-resource-server`
- `spring-boot-starter-oauth2-client`
- `spring-security-test`
- `spring-boot-webmvc-test`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

The OAuth2 Resource Server dependency is included because Spring Security Test's `jwt()` request post processor depends on JWT-related resource-server classes.

---

## Security Configuration

The example uses a `SecurityFilterChain` to protect the application's endpoints:

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/public").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(httpBasic -> {});

        return http.build();
    }
}
```

The configuration defines the following access rules:

```text
/public                  → Public
/user                    → Authenticated users
/admin                   → ADMIN role
/transfer                → Authenticated users + CSRF
/profile                 → Authenticated users
/user-operation          → USER role
/admin-operation         → ADMIN role
/authenticated-operation → Authenticated users
```

CSRF protection remains enabled so that it can also be tested with `csrf()`.

---

## Customizing the Mock JWT

The JWT request post processor can also be customized.

For example, a subject can be provided:

```java
mockMvc.perform(get("/user").with(jwt().jwt(jwt -> jwt.subject("user")))).andExpect(status().isOk());
```

This is useful when a test needs to verify behavior based on JWT claims.

JWT testing here focuses on creating the authentication context.

It does not replace integration testing of actual JWT validation.

---

## Authentication vs Authorization

Security testing makes the difference between authentication and authorization particularly visible.

### Authentication

Authentication answers:

> Who is making the request?

For example:

```text
user/password
      │
      ▼
Authenticated user
```

### Authorization

Authorization answers:

> Is this authenticated user allowed to perform this operation?

For example:

```text
Authenticated user
      │
      ▼
Requires ADMIN role
      │
      ▼
User has USER role
      │
      ▼
403 Forbidden
```

---

## 401 Unauthorized

A request that requires authentication but does not contain an authenticated user can result in `401 Unauthorized`.

For example:

```java
mockMvc.perform(get("/user")).andExpect(status().isUnauthorized());
```

The request has not established an authenticated security context.

Conceptually:

```text
Request
  │
  ▼
Authentication required
  │
  ▼
No authenticated user
  │
  ▼
401 Unauthorized
```

---

## 403 Forbidden

A request can also be authenticated but lack the required authority.

For example:

```java
mockMvc.perform(get("/admin").with(user("user").roles("USER"))).andExpect(status().isForbidden());
```

The user is authenticated, but the user does not have the `ADMIN` role.

Conceptually:

```text
Request
  │
  ▼
Authenticated
  │
  ▼
Requires ADMIN
  │
  ▼
User has USER
  │
  ▼
403 Forbidden
```

The distinction can therefore be summarized as:

```text
401 → Authentication is missing or invalid

403 → Authentication exists, but access is not permitted
```

---

## Security Test Utilities

The main Spring Security Test utilities demonstrated by this example are:

| Utility       | Purpose                             |
|---------------|-------------------------------------|
| `user()`      | Creates a mock authenticated user   |
| `anonymous()` | Creates an anonymous authentication |
| `csrf()`      | Adds a valid CSRF token             |
| `jwt()`       | Creates a mock JWT authentication   |

These utilities allow tests to express the required security state directly.

For example:

```java
mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")));
```

The security state is part of the test itself.

---

## Why Use Spring Security Test?

Without Spring Security Test, tests may require considerably more setup around authentication and the security context.

Spring Security Test provides a concise way to establish security state:

```text
user()
anonymous()
csrf()
jwt()
```

This keeps tests focused on the behavior being verified.

Instead of implementing a complete login flow for every authorization test, the test can establish the required security context directly.

---

## Complete Testing Flow

A typical authorization test in this example follows this pattern:

```text
Test
 │
 ▼
MockMvc
 │
 ▼
.with(user(...))
 │
 ▼
SecurityContext
 │
 ▼
SecurityFilterChain
 │
 ▼
Authorization Rules
 │
 ├── Allowed
 │     │
 │     ▼
 │   Controller
 │
 └── Denied
       │
       ▼
    401 / 403
```

For method security:

```text
MockMvc
 │
 ▼
Controller
 │
 ▼
Service Method
 │
 ▼
@PreAuthorize
 │
 ▼
Authorization Decision
 │
 ├── Allowed
 │
 └── Forbidden
```

---

## Test Coverage

The example covers:

| Test                                      | Expected Result    |
|-------------------------------------------|--------------------|
| Public endpoint without authentication    | `200 OK`           |
| Protected endpoint without authentication | `401 Unauthorized` |
| Protected endpoint with mock user         | `200 OK`           |
| Protected endpoint with anonymous user    | `401 Unauthorized` |
| Admin endpoint with USER role             | `403 Forbidden`    |
| Admin endpoint with ADMIN role            | `200 OK`           |
| POST without CSRF token                   | `403 Forbidden`    |
| POST with CSRF token                      | `200 OK`           |
| Profile with mock user                    | `200 OK`           |
| USER method with USER role                | `200 OK`           |
| ADMIN method with USER role               | `403 Forbidden`    |
| ADMIN method with ADMIN role              | `200 OK`           |
| Authenticated method with mock user       | `200 OK`           |
| Protected endpoint with mock JWT          | `200 OK`           |

---

## Running the Tests

From the project root:

```bash
mvn test
```

Or from this module:

```bash
mvn test
```

To run the complete verification lifecycle:

```bash
mvn clean verify
```

---

## Key Concepts

### `spring-security-test`

Provides testing support for Spring Security applications.

### `MockMvc`

Allows tests to send HTTP requests through the Spring MVC application and security filter chain.

### `user()`

Creates a mock authenticated user for a request.

### `anonymous()`

Creates an anonymous authentication for a request.

### `csrf()`

Adds a valid CSRF token to a request.

### `jwt()`

Creates a mock JWT authentication for a request.

### `@EnableMethodSecurity`

Enables method-level security such as `@PreAuthorize`.

### `401 Unauthorized`

Indicates that authentication is required but the request is not authenticated.

### `403 Forbidden`

Indicates that the request is authenticated but does not have sufficient permission.

---

## Key Takeaways

1. `spring-security-test` provides utilities specifically designed for testing Spring Security.
2. MockMvc can test security behavior through the application's filter chain.
3. `user()` creates a mock authenticated user.
4. `anonymous()` explicitly creates an anonymous request.
5. `csrf()` makes it easy to test CSRF-protected requests.
6. `jwt()` creates a mock JWT authentication without performing real JWT authentication.
7. Roles can be supplied directly from tests.
8. Method-level authorization can be tested through MockMvc.
9. `401 Unauthorized` represents an authentication problem.
10. `403 Forbidden` represents an authorization problem.
11. Security tests can establish the required security context without performing a complete authentication flow.
12. Mock authentication tests and real authentication integration tests serve different purposes.

The central idea is:

```text
Security Test
     │
     ▼
Establish Security Context
     │
     ▼
Execute Request
     │
     ▼
Spring Security
     │
     ▼
Authorization Decision
     │
     ├── Allowed
     │
     └── Denied
```

---

## Related Examples

This example builds on the security concepts demonstrated throughout Module 01:

- Basic Spring Security
- SecurityFilterChain
- Authentication
- UserDetailsService
- Password Encoding
- Authorization
- SecurityContext
- Method Security
- CSRF Protection
- Session Management
- OAuth2 Login
- JWT

This example completes the planned Spring Security topics for Module 01.

---

## Next

**Module 02 - Spring Data**

With Spring Security complete, the next module will explore Spring Data and its core data-access abstractions.

> Small examples, extensive coverage.
