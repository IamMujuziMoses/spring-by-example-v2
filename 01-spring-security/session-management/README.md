# Session Management

This example demonstrates **session management** in Spring Security.

It focuses on how Spring Security associates authentication with an HTTP session, how authenticated requests can reuse the same session, and how session creation policies control session behavior.

## What This Example Demonstrates

- What session management means in Spring Security.
- HTTP sessions and authenticated requests.
- `SessionCreationPolicy`.
- The `IF_REQUIRED` session creation policy.
- Reusing an authenticated HTTP session.
- Accessing the current `HttpSession`.
- The relationship between authentication, `SecurityContext`, and the HTTP session.
- Testing session behavior with `MockMvc`.

---

## What Is Session Management?

Session management controls how an application creates, maintains, and uses HTTP sessions for authenticated users.

In a session-based application, authentication can be associated with an HTTP session so that subsequent requests do not need to authenticate from scratch.

A simplified flow looks like:

```text
Login
  │
  ▼
Authentication
  │
  ▼
SecurityContext
  │
  ▼
HTTP Session
  │
  ▼
Subsequent Request
  │
  ▼
SecurityContext restored
```

This allows an authenticated user to interact with the application across multiple requests.

---

## Session Management and SecurityContext

Spring Security uses the `SecurityContext` to hold the current authentication.

In a session-based application, the security context can be persisted so that the authentication is available across subsequent requests associated with the same HTTP session.

Conceptually:

```text
HTTP Request
     │
     ▼
SecurityContext
     │
     ▼
Authentication
     │
     ▼
HTTP Session
```

On a subsequent request:

```text
HTTP Request
     │
     ▼
HTTP Session
     │
     ▼
SecurityContext
     │
     ▼
Authentication
```

This allows the application to recognize the authenticated user across requests.

---

## SessionCreationPolicy

Spring Security provides `SessionCreationPolicy` to control how sessions are created and used.

The main policies are:

### `ALWAYS`

Spring Security always creates an HTTP session.

```java
.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
```

### `IF_REQUIRED`

A session is created when required.

```java
.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
```

This is the default session creation policy.

### `NEVER`

Spring Security does not create a session, but it can use an existing session.

```java
.sessionCreationPolicy(SessionCreationPolicy.NEVER)
```

### `STATELESS`

Spring Security does not create or use an HTTP session to obtain the `SecurityContext`.

```java
.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
```

This is commonly used when building stateless APIs where each request carries its own authentication information.

---

## Session Policy Used in This Example

This example explicitly uses:

```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
```

The `IF_REQUIRED` policy allows Spring Security to create a session when one is needed.

The example therefore demonstrates session-based security without forcing a session to be created for every request.

---

## Form Login

The example uses form-based login:

```java
.formLogin(form -> form.permitAll())
```

Form login provides a natural way to demonstrate session-based authentication.

The conceptual flow is:

```text
Login Form
    │
    ▼
Credentials
    │
    ▼
Authentication
    │
    ▼
SecurityContext
    │
    ▼
HTTP Session
```

Once authentication has been established, subsequent requests can use the authenticated session.

---

## Accessing the Session

The controller receives the current `HttpSession`:

```java
@GetMapping("/session")
public String session(Authentication authentication, HttpSession session) {
    return "Authenticated as: " + authentication.getName() + ", Session ID: " + session.getId();
}
```

This demonstrates that application code can access the current session when session-based behavior is appropriate.

The controller also receives the authenticated `Authentication` object.

This makes the relationship between the authenticated user and the HTTP session visible in the example.

---

## Reusing a Session

The tests demonstrate that an authenticated session can be reused across requests.

The first request obtains the session:

```java
MvcResult firstRequest = mockMvc.perform(...).andExpect(status().isOk()).andReturn();

HttpSession session = firstRequest.getRequest().getSession(false);
```

The session is then supplied to another request:

```java
mockMvc.perform(get("/session")
        .session((MockHttpSession) session)
        .with(user("user").roles("USER")))
        .andExpect(status().isOk());
```

The test verifies that both requests use the same session ID.

Conceptually:

```text
First Request
     │
     ▼
Session A
     │
     │
     ▼
Second Request
     │
     ▼
Session A
```

---

## Why `getSession(false)`?

The example uses:

```java
request.getSession(false)
```

The `false` argument means:

> Return the existing session if one exists, but do not create a new session.

This is useful when testing whether a session actually exists rather than accidentally creating one while performing the assertion.

---

## Session-Based vs Stateless Security

Session-based security and stateless security use different approaches to maintaining authentication.

### Session-Based

```text
Request
  │
  ▼
HTTP Session
  │
  ▼
SecurityContext
  │
  ▼
Authentication
```

The session can maintain authentication between requests.

### Stateless

```text
Request
  │
  ▼
Authentication Credentials
  │
  ▼
Authentication
```

Each request contains the information necessary to authenticate the request.

A common example is a stateless API using bearer tokens.

---

## `IF_REQUIRED` vs `STATELESS`

The difference can be summarized as:

| Policy        | Session Behavior                                           |
|---------------|------------------------------------------------------------|
| `ALWAYS`      | Always creates a session                                   |
| `IF_REQUIRED` | Creates a session when required                            |
| `NEVER`       | Does not create a session but can use an existing one      |
| `STATELESS`   | Does not create or use a session for the `SecurityContext` |

The choice depends on the application's authentication architecture.

---

## Testing Session Management

The example uses Spring Boot's test support and `MockMvc`.

### Public Endpoint

The public endpoint can be accessed without authentication:

```java
mockMvc.perform(get("/public")).andExpect(status().isOk());
```

### Protected Endpoint

The `/session` endpoint requires authentication.

Without authentication, Spring Security redirects the request to the login page because form login is enabled.

### Authenticated Request

An authenticated request can access the protected endpoint:

```java
mockMvc.perform(get("/session").with(user("user").roles("USER"))).andExpect(status().isOk());
```

### Reusing the Same Session

The test captures the session from the first request and supplies it to the second request.

The session IDs are then compared to verify that the same session is being used.

---

## Important Testing Detail

The test uses:

```java
.with(user("user").roles("USER"))
```

to establish the authenticated user for the test request.

This is a Spring Security Test mechanism for supplying authentication to the request.

The session itself is separately captured and reused between requests.

Therefore, the test demonstrates two related concepts:

```text
Authentication
      +
HTTP Session
      │
      ▼
Subsequent Request
```

The `with(user(...))` request post-processor should not be confused with performing an actual login through the application's login endpoint. It is a testing mechanism for establishing authentication directly on the request.

---

## Session Management Flow

A simplified view of the example is:

```text
HTTP Request
     │
     ▼
Security Filter Chain
     │
     ▼
Authentication
     │
     ▼
SecurityContext
     │
     ▼
HTTP Session
     │
     ▼
Subsequent Request
     │
     ▼
SecurityContext
     │
     ▼
Authenticated Application Code
```

---

## Session Management vs Authentication

Session management does not replace authentication.

Authentication establishes the user's identity:

```text
Username + Password
        ↓
Authentication
```

Session management determines how that authenticated state is maintained across requests:

```text
Authentication
      ↓
SecurityContext
      ↓
HTTP Session
      ↓
Subsequent Requests
```

These are related but distinct responsibilities.

---

## Running the Example

From the repository root:

```bash
mvn -pl 01-spring-security/session-management spring-boot:run
```

The application starts with form-based authentication enabled.

---

## Running the Tests

Run the tests from the module:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 01-spring-security/session-management test
```

---

## Key Takeaways

- Session management controls how Spring Security interacts with HTTP sessions.
- `SessionCreationPolicy` controls session creation and usage.
- `IF_REQUIRED` creates a session when one is needed.
- Authentication can be associated with an HTTP session.
- The `SecurityContext` contains the current authentication.
- Subsequent requests can reuse the authenticated session.
- `getSession(false)` checks for an existing session without creating one.
- `STATELESS` is appropriate for security architectures that should not use HTTP sessions for authentication.
- Session management and authentication are related but separate concepts.
- Spring Security Test provides mechanisms for testing authenticated requests and session behavior.

---

## What This Example Does Not Cover

This example intentionally focuses on basic session management.

It does not cover:

- Session fixation protection.
- Concurrent session control.
- Maximum sessions per user.
- Session invalidation.
- Remember-me authentication.
- Distributed session management.
- Spring Session.
- Custom session repositories.

These topics can be explored separately when needed.

---

## Next Example

The next example will explore **OAuth2** in Spring Security, including OAuth2 concepts, authorization flows, and Spring Security's OAuth2 support.