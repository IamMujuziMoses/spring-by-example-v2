# SecurityContext

This example demonstrates how Spring Security stores the authentication information for the current request in the **`SecurityContext`** and exposes it through the **`SecurityContextHolder`**.

The example builds on the previous authentication, password encoding, and authorization examples by showing how the currently authenticated user's security information can be accessed after authentication has completed.

## What This Example Demonstrates

- `SecurityContext`
- `SecurityContextHolder`
- `Authentication`
- Accessing the current `Authentication`
- Injecting `Authentication` into a controller method
- Accessing `Authentication` through `SecurityContextHolder`
- Reading the authenticated username
- Reading granted authorities
- Spring Security's authentication context during a request
- Testing the `SecurityContext` with MockMvc

---

## What Is the SecurityContext?

The `SecurityContext` contains the security information associated with the current request.

The most important object stored in the context is the current `Authentication`.

Conceptually:

```text
SecurityContext
      │
      └── Authentication
              ├── Principal
              ├── Authorities
              └── Authentication State
```

After successful authentication, Spring Security makes the resulting `Authentication` available through the `SecurityContext`.

---

## SecurityContextHolder

The `SecurityContextHolder` provides access to the current `SecurityContext`.

For example:

```java
SecurityContext securityContext = SecurityContextHolder.getContext();

Authentication authentication = securityContext.getAuthentication();
```

The `Authentication` can then be used to access information about the current authenticated user.

For example:

```java
String username = authentication.getName();
```

and:

```java
Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
```

---

## Authentication

The `Authentication` object represents the current authentication state.

It can provide information such as:

- The authenticated user's name
- The user's authorities
- Whether the request has been authenticated
- The principal associated with the authentication

For example:

```java
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

String username = authentication.getName();
```

The example authenticates the following user:

```text
Username: user
Password: password
Role: USER
```

After successful authentication, the resulting `Authentication` becomes available through the `SecurityContext`.

---

## SecurityContext and Authentication Flow

The authentication flow can be visualized as:

```text
HTTP Request
     │
     │ Username + Password
     ▼
Authentication
     │
     ▼
AuthenticationManager
     │
     ▼
AuthenticationProvider
     │
     ▼
Successful Authentication
     │
     ▼
SecurityContext
     │
     ▼
SecurityContextHolder
     │
     ▼
Application Code
```

Application code can then access the current authentication.

---

## Accessing Authentication Through a Controller Parameter

Spring MVC can provide the current `Authentication` directly as a controller method parameter:

```java
@GetMapping("/authentication")
public String authentication(Authentication authentication) {
    return "Authenticated as: " + authentication.getName();
}
```

For an authenticated request from `user`, the endpoint returns:

```text
Authenticated as: user
```

This is a convenient way to access the current authentication when it is needed directly by a controller method.

---

## Accessing Authentication Through SecurityContextHolder

The same authentication can be accessed explicitly through the `SecurityContextHolder`:

```java
@GetMapping("/security-context")
public String securityContext() {
    SecurityContext securityContext = SecurityContextHolder.getContext();

    Authentication authentication = securityContext.getAuthentication();

    return "Authenticated as: " + authentication.getName();
}
```

The flow is:

```text
SecurityContextHolder
        │
        ▼
SecurityContext
        │
        ▼
Authentication
        │
        ▼
authentication.getName()
```

For the authenticated `user`, this returns:

```text
Authenticated as: user
```

---

## Authentication Details

The example also exposes the username and authorities associated with the current authentication:

```java
@GetMapping("/details")
public String details() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).sorted()
            .collect(Collectors.joining(", "));

    return "Name: " + authentication.getName() + ", Authorities: [" + authorities + "]";
}
```

For example, the authentication includes:

```text
ROLE_USER
```

Spring Security 7 can also expose authentication factor authorities. With HTTP Basic authentication, the authentication can therefore include:

```text
FACTOR_PASSWORD
```

The exact set of authorities is determined by the authentication mechanism and Spring Security configuration.

The important concept is that `Authentication#getAuthorities()` represents the authorities associated with the current authentication.

---

## Security Configuration

The security configuration requires authentication for all endpoints except `/public`:

```java
http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/public").permitAll()
                .anyRequest().authenticated())
        .httpBasic(withDefaults());
```

The application uses an in-memory user:

```java
UserDetails user = User.withUsername("user").password(passwordEncoder.encode("password")).roles("USER").build();
```

The password is encoded using:

```java
@Bean
PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

The focus of this example is the `SecurityContext`, so authentication and password encoding are used as supporting infrastructure.

---

## Public Endpoint

The `/public` endpoint does not require authentication:

```java
@GetMapping("/public")
public String publicEndpoint() {
    return "This endpoint is public.";
}
```

It can therefore be accessed without credentials.

```text
GET /public
     │
     ▼
  permitAll
     │
     ▼
    200
```

---

## Protected Endpoints

The following endpoints require authentication:

```text
/authentication
/security-context
/details
```

An unauthenticated request receives:

```text
401 Unauthorized
```

An authenticated request can access the current `Authentication` through either the controller method parameter or the `SecurityContextHolder`.

---

## Method Parameter vs SecurityContextHolder

There are two approaches demonstrated by this example.

### Controller Method Parameter

```java
@GetMapping("/authentication")
public String authentication(Authentication authentication) {
    return "Authenticated as: " + authentication.getName();
}
```

This is concise and convenient when the controller only needs the current authentication.

### SecurityContextHolder

```java
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
```

This provides explicit access to the `SecurityContext`.

It is useful when application code needs to access the security context directly rather than receiving the authentication as a controller method parameter.

Both approaches access the current authentication associated with the request.

---

## SecurityContextHolder and the Current Request

The `SecurityContextHolder` is designed to provide the security context associated with the current execution.

Conceptually:

```text
Current Request
      │
      ▼
SecurityContextHolder
      │
      ▼
SecurityContext
      │
      ▼
Authentication
```

This allows application code to determine the identity and authorities associated with the current authenticated request.

---

## Authentication Information

The `Authentication` object can expose several pieces of security information.

For example:

```java
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

String name = authentication.getName();

Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

boolean authenticated = authentication.isAuthenticated();
```

The example focuses primarily on the username and authorities.

---

## Test Coverage

The tests verify:

- The public endpoint is accessible without authentication.
- The authentication endpoint exposes the current authenticated user.
- The security context exposes the current authentication.
- Authentication details contain the expected username.
- Authentication details contain the expected user role.
- Authentication-factor authorities can be present.
- Protected endpoints reject unauthenticated requests with `401 Unauthorized`.

---

## SecurityContext vs SecurityContextHolder

These two classes have different responsibilities.

### SecurityContext

`SecurityContext` represents the security information for the current execution.

It contains the current `Authentication`.

```text
SecurityContext
      │
      └── Authentication
```

### SecurityContextHolder

`SecurityContextHolder` provides access to the current `SecurityContext`.

```text
SecurityContextHolder
      │
      ▼
SecurityContext
      │
      ▼
Authentication
```

A useful way to remember the relationship is:

> `SecurityContextHolder` provides access to the `SecurityContext`, while the `SecurityContext` contains the current `Authentication`.

---

## Authentication Flow in This Example

The complete flow is:

```text
Client
  │
  │ GET /security-context
  │
  │ Basic Authentication
  ▼
Spring Security Filter Chain
  │
  ▼
Authentication
  │
  ▼
Successful Authentication
  │
  ▼
SecurityContext
  │
  ▼
SecurityContextHolder
  │
  ▼
SecurityContextController
  │
  ▼
Current Authentication
```

---

## Key Takeaways

1. **`SecurityContext` contains the current security information.**
2. **`Authentication` represents the current authentication.**
3. **`SecurityContextHolder` provides access to the current `SecurityContext`.**
4. The authenticated user's name can be obtained from `Authentication#getName()`.
5. User roles and other authorities can be obtained from `Authentication#getAuthorities()`.
6. Spring MVC can inject the current `Authentication` into a controller method.
7. Application code can also access the current authentication through `SecurityContextHolder`.
8. The `SecurityContext` is populated as part of the Spring Security authentication process.
9. The complete authority collection can contain both application roles and authentication-factor authorities.
10. Authentication must occur before protected application code can access an authenticated user's security information.

---

## What This Example Does Not Cover

This example intentionally focuses on accessing the current security context.

It does not cover:

- Method-level security
- `@PreAuthorize`
- `@PostAuthorize`
- `@Secured`
- `@RolesAllowed`
- Custom authorization managers
- OAuth2
- JWT
- Persistent security contexts
- Custom `SecurityContextRepository`
- Security context propagation across asynchronous execution

These topics are outside the scope of this example and are covered by later examples where appropriate.

---

## Running the Example

From the `security-context` directory:

```bash
mvn spring-boot:run
```

The application can then be accessed at:

```text
http://localhost:8080/public
http://localhost:8080/authentication
http://localhost:8080/security-context
http://localhost:8080/details
```

Example credentials:

```text
Username: user
Password: password
```

---

## Running the Tests

```bash
mvn test
```

All SecurityContext scenarios should pass.

---

## Next Example

The next example will demonstrate **Method Security**, showing how Spring Security can apply authorization directly to service methods using annotations such as `@PreAuthorize`.