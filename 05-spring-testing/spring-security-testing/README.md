# Spring Security Testing

This example demonstrates **Spring Security testing** using Spring Boot, Spring Security Test, and MockMvc.

It focuses on how to test authentication and authorization, how to simulate authenticated users, and how to verify that security rules allow or reject requests according to the configured access policies.

## What This Example Demonstrates

- Testing Spring Security with MockMvc.
- Testing public and protected endpoints.
- Authentication using HTTP Basic credentials.
- Testing unauthenticated requests.
- Testing invalid credentials.
- Testing role-based authorization.
- Using `@WithMockUser` to establish authentication for a test.
- Using the `user()` and `httpBasic()` request post-processors.
- Distinguishing `401 Unauthorized` from `403 Forbidden`.
- Testing the configured Spring Security filter chain.

---

## What Is Spring Security Testing?

Spring Security testing verifies that an application's security configuration behaves as intended.

Applications commonly need to ensure that public resources remain accessible, protected resources require authentication, and restricted operations are available only to users with the appropriate authorities.

Spring Security Test provides test utilities that integrate with Spring's testing infrastructure and MockMvc.

A simplified flow looks like:

```text
Test
 |
 v
MockMvc Request
 |
 v
Spring Security Filter Chain
 |
 v
Authentication
 |
 v
Authorization
 |
 v
Controller or Security Error Response
```

This allows tests to verify security behavior without starting a real HTTP server.

---

## Security Configuration

The example defines three endpoints:

| Endpoint     | Required access |
|--------------|-----------------|
| `/public`    | Public          |
| `/greetings` | `USER` role     |
| `/admin`     | `ADMIN` role    |

The security configuration uses `SecurityFilterChain`:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/public").permitAll()
                    .requestMatchers("/admin").hasRole("ADMIN")
                    .requestMatchers("/greetings").hasRole("USER")
                    .anyRequest().authenticated()
            ).httpBasic(Customizer.withDefaults());

    return http.build();
}
```

The `permitAll()` rule allows unauthenticated requests to the public endpoint.

The `hasRole()` rules require the specified roles. For example, `hasRole("ADMIN")` checks for the `ROLE_ADMIN` authority.

The `anyRequest().authenticated()` rule requires authentication for any other request that does not match an earlier rule.

The example enables HTTP Basic authentication so tests can submit credentials directly.

---

## Authentication and Authorization

Authentication and authorization are related but distinct concepts.

### Authentication

Authentication establishes the identity of the user making a request.

In this example, HTTP Basic authentication supplies a username and password:

```java
mockMvc.perform(get("/greetings")
        .with(httpBasic("user", "password")))
        .andExpect(status().isOk());
```

The `httpBasic()` request post-processor adds HTTP Basic authentication credentials to the request.

Spring Security processes those credentials through its configured authentication mechanism.

### Authorization

Authorization determines whether an authenticated user has permission to access a resource.

For example, the `/admin` endpoint requires the `ADMIN` role.

A user with only the `USER` role should not be allowed to access it.

```text
Request
   |
   v
Authentication
   |
   v
Authorization
   |
   +---- Allowed ----> Controller
   |
   +---- Denied -----> Security Error Response
```

---

## Testing Public Endpoints

The `/public` endpoint is configured with `permitAll()`.

The test verifies that the endpoint is accessible without authentication:

```java
mockMvc.perform(get("/public"))
        .andExpect(status().isOk())
        .andExpect(content().string("Hello, everyone!"));
```

This confirms that the configured security rules permit unauthenticated access.

A public endpoint can still pass through the Spring Security filter chain. `permitAll()` allows access; it does not bypass the entire filter chain.

---

## Testing Unauthenticated Requests

The `/greetings` endpoint requires the `USER` role.

An unauthenticated request is rejected:

```java
mockMvc.perform(get("/greetings")).andExpect(status().isUnauthorized());
```

Because HTTP Basic authentication is enabled, the request receives `401 Unauthorized` when valid authentication is not supplied.

This verifies that the security configuration protects the endpoint.

---

## Testing Authentication with HTTP Basic

The example defines an in-memory user with the `USER` role.

A test can supply the user's credentials using:

```java
mockMvc.perform(get("/greetings")
        .with(httpBasic("user", "password")))
        .andExpect(status().isOk())
        .andExpect(content().string("Hello, authenticated user!"));
```

This exercises the HTTP Basic authentication mechanism configured in the application.

The credentials are checked against the configured `UserDetailsService`, and the password is verified using the configured `PasswordEncoder`.

The example uses BCrypt for password encoding.

The in-memory users and credentials are intended for demonstration and testing, not production deployment.

---

## Testing Invalid Credentials

Tests should also verify that incorrect credentials do not grant access.

```java
mockMvc.perform(get("/greetings")
        .with(httpBasic("user", "wrong-password")))
        .andExpect(status().isUnauthorized());
```

The test confirms that invalid credentials are rejected.

Testing unsuccessful authentication is important because security tests should verify both expected access and expected denial.

---

## Testing Role-Based Authorization

Authentication alone does not guarantee access to every endpoint.

The `/admin` endpoint requires the `ADMIN` role.

A user with only the `USER` role should receive `403 Forbidden`:

```java
mockMvc.perform(get("/admin")
        .with(user("user").roles("USER")))
        .andExpect(status().isForbidden());
```

The `user()` request post-processor supplies a test authentication with the specified username and roles.

In this case, the request has an authenticated user, but that user does not have the required authority.

The request therefore fails authorization.

---

## Using `@WithMockUser`

Spring Security Test provides the `@WithMockUser` annotation to establish an authenticated user for a test.

```java
@Test
@WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
void shouldAllowAdministratorAccessToAdminEndpoint() throws Exception {
    mockMvc.perform(get("/admin"))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello, administrator!"));
}
```

The annotation supplies a mock authenticated principal with the specified username and roles.

This is useful when a test needs to focus on authorization behavior without performing authentication using actual credentials.

### `@WithMockUser` vs `httpBasic()`

| Feature                                        | `@WithMockUser` | `httpBasic()`        |
|------------------------------------------------|-----------------|----------------------|
| Supplies authentication for a test             | Yes             | Supplies credentials |
| Exercises credential authentication            | No              | Yes                  |
| Uses the configured password verification flow | No              | Yes                  |
| Useful for role-based authorization tests      | Yes             | Yes                  |
| Requires a real HTTP server                    | No              | No                   |

`@WithMockUser` and `user()` establish authentication directly for testing. They should not be confused with a real login request.

By contrast, `httpBasic()` supplies credentials that Spring Security processes through HTTP Basic authentication.

Choose the approach that matches the behavior being tested.

---

## Understanding HTTP Status Codes

Two important status codes appear in security testing.

### `401 Unauthorized`

This status indicates that the request lacks valid authentication.

Examples include:

- A request to a protected endpoint without credentials.
- A request containing invalid HTTP Basic credentials.

### `403 Forbidden`

This status indicates that the request is not permitted under the applicable authorization rules.

For example, an authenticated user with only the `USER` role cannot access an endpoint requiring `ADMIN`.

The distinction helps tests identify whether a failure is caused by authentication or authorization.

---

## Why Use `@SpringBootTest` and MockMvc?

This example uses:

```java
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTestingTest {
    // Security tests
}
```

`@SpringBootTest` loads the application context, including the security configuration and authentication components.

`@AutoConfigureMockMvc` configures MockMvc for testing requests against the application without starting a real HTTP server.

This is important because the tests need to exercise the actual configured Spring Security filter chain rather than only testing controller methods directly.

---

## Security Testing Flow

The example covers several request scenarios:

```text
Public Request
      |
      v
Access Allowed
      |
      v
HTTP 200 OK
```

```text
Unauthenticated Protected Request
      |
      v
Authentication Required
      |
      v
HTTP 401 Unauthorized
```

```text
Authenticated User Request
      |
      v
Authentication Successful
      |
      v
Authorization Granted
      |
      v
HTTP 200 OK
```

```text
User Without Required Role
      |
      v
Authentication Established
      |
      v
Authorization Denied
      |
      v
HTTP 403 Forbidden
```

These scenarios verify that authentication and authorization are enforced at the request boundary.

---

## Running the Example

From the repository root:

```bash
mvn -pl 05-spring-testing/spring-security-testing spring-boot:run
```

The application starts with HTTP Basic authentication enabled.

The example's in-memory users are intended for demonstration only.

---

## Running the Tests

Run the tests from the module:

```bash
mvn -pl 05-spring-testing/spring-security-testing test
```

To build the module and its required reactor dependencies:

```bash
mvn -pl 05-spring-testing/spring-security-testing -am verify
```

---

## Key Takeaways

- Spring Security Test provides utilities for testing authentication and authorization.
- MockMvc can test security behavior without starting a real HTTP server.
- `@SpringBootTest` and `@AutoConfigureMockMvc` load and exercise the configured security infrastructure.
- HTTP Basic authentication can be tested with `httpBasic()`.
- `@WithMockUser` and `user()` establish authentication directly for testing.
- Authentication determines who the user is; authorization determines what the user can access.
- Public endpoints can be configured with `permitAll()`.
- Role-based restrictions can be tested with `hasRole()`.
- `401 Unauthorized` and `403 Forbidden` represent different failure conditions.
- Tests should cover both successful access and rejected requests.

---

## What This Example Does Not Cover

This example intentionally focuses on basic authentication and authorization testing.

It does not cover:

- Form login.
- CSRF protection and CSRF testing.
- Method-level security.
- OAuth2 and OpenID Connect.
- JWT and resource server testing.
- Session management and session fixation.
- Custom authentication providers.
- Remember-me authentication.
- Testing custom security filters.
- Integration with external identity providers.

These topics can be explored in separate examples as needed.

---

## Related Examples

- [Spring MVC Testing](../spring-mvc-testing/README.md)
- [Spring Test Slices](../spring-test-slices/README.md)
- [MockMvc](../mockmvc/README.md)
- [Spring Boot Test](../spring-boot-test/README.md)

---

## Next Example

**Integration Testing**

The next example will explore another Spring Testing topic, building on the fundamentals of integration testing, authentication, and authorization.
