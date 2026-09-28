# CSRF Protection

This example demonstrates **Cross-Site Request Forgery (CSRF) protection** in Spring Security.

It focuses on how Spring Security protects state-changing requests, how CSRF tokens are validated, and how CSRF-protected requests can be tested with Spring Security Test.

## What This Example Demonstrates

- What CSRF is.
- Why CSRF protection is enabled by default in Spring Security.
- The role of `CsrfFilter`.
- CSRF tokens.
- Protecting state-changing HTTP requests.
- Requests rejected when a CSRF token is missing.
- Requests accepted when a valid CSRF token is supplied.
- Testing CSRF protection with `MockMvc`.
- Using Spring Security Test's `csrf()` request post-processor.
- The difference between authentication and CSRF protection.
- Why an unauthenticated request can receive `403 Forbidden` when CSRF validation fails first.

---

## What Is CSRF?

**Cross-Site Request Forgery (CSRF)** is an attack where a malicious website attempts to make a user's browser send an unwanted request to another application where the user is already authenticated.

For example, imagine a user is authenticated to a banking application.

A malicious website could attempt to cause the user's browser to submit:

```text
POST /transfer
```

The browser may automatically include authentication-related information such as session cookies.

Without CSRF protection, the application could potentially mistake the request for an intentional action from the user.

CSRF protection adds another requirement: the request must contain a valid CSRF token that an attacker should not be able to obtain.

---

## Why Spring Security Protects Against CSRF

Spring Security enables CSRF protection by default.

This is particularly important for applications where authentication information is automatically included with requests, such as applications using session cookies.

CSRF protection helps ensure that state-changing requests originate from a trusted application context rather than simply relying on the browser's authentication information.

---

## CSRF Tokens

A CSRF token is a value associated with the user's security context that must be submitted with a protected request.

Conceptually:

```text
Client
  │
  │ Request + CSRF Token
  ▼
Spring Security
  │
  ▼
CsrfFilter
  │
  ├── Valid token ──────► Continue request
  │
  └── Missing/Invalid ──► 403 Forbidden
```

An attacker may be able to cause a browser to send a request, but should not be able to obtain the valid CSRF token required by the application.

---

## CSRF Protection in This Example

The security configuration intentionally does **not** disable CSRF protection:

```java
http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated()).httpBasic(withDefaults());
```

There is deliberately no:

```java
csrf(AbstractHttpConfigurer::disable)
```

This allows Spring Security's default CSRF protection to remain active.

---

## Protected Endpoint

The example provides a state-changing endpoint:

```text
POST /transfer
```

The controller returns:

```text
Transfer completed.
```

The endpoint requires authentication and is also protected by CSRF validation.

---

## What Happens Without a CSRF Token?

An authenticated request without a CSRF token is rejected:

```java
mockMvc.perform(post("/transfer").with(httpBasic("user", "password"))).andExpect(status().isForbidden());
```

The user is authenticated, but the request does not contain the required CSRF token.

The result is:

```text
403 Forbidden
```

---

## What Happens With a CSRF Token?

Spring Security Test provides the `csrf()` request post-processor:

```java
mockMvc.perform(post("/transfer")
        .with(httpBasic("user", "password"))
        .with(csrf()))
        .andExpect(status().isOk());
```

The `csrf()` request post-processor adds a valid CSRF token to the test request.

The request can therefore pass CSRF validation and reach the controller.

```text
POST /transfer
      │
      ├── HTTP Basic credentials
      │
      └── CSRF token
             │
             ▼
       Security Filters
             │
             ▼
       CSRF validation
             │
             ▼
        Controller
             │
             ▼
   Transfer completed.
```

---

## Authentication vs CSRF Protection

Authentication and CSRF protection solve different problems.

### Authentication

Authentication determines **who is making the request**.

```text
Username + Password
        ↓
Authentication
        ↓
Authenticated User
```

### CSRF Protection

CSRF protection determines whether a protected request contains the expected CSRF token.

```text
Request
   ↓
CSRF Token
   ↓
CsrfFilter
   ↓
Valid / Invalid
```

A request can therefore have valid authentication but still fail CSRF validation.

---

## Why the Unauthenticated Request Returns 403

An important behavior demonstrated by this example is:

```java
mockMvc.perform(post("/transfer")).andExpect(status().isForbidden());
```

It may seem that the request should return `401 Unauthorized` because no credentials were supplied.

However, the request is a `POST` without a CSRF token.

The CSRF filter can reject the request before authentication becomes the reason for rejection:

```text
POST /transfer
      │
      ▼
Security Filter Chain
      │
      ▼
CsrfFilter
      │
      ├── CSRF token missing
      │
      ▼
403 Forbidden
```

Therefore, the result is:

```text
Expected: 401
Actual:   403
```

This is an important distinction when reasoning about Spring Security's filter chain.

---

## 401 vs 403

Spring Security commonly uses these status codes for different situations:

### 401 Unauthorized

The request requires authentication, but the request has not been successfully authenticated.

### 403 Forbidden

The request is understood but access is denied.

In this example, `403` can occur because CSRF protection rejects a state-changing request before the request proceeds further through the security filter chain.

---

## CSRF Filter

Spring Security's `CsrfFilter` is responsible for processing CSRF protection.

Conceptually:

```text
HTTP Request
     │
     ▼
Security Filter Chain
     │
     ▼
CsrfFilter
     │
     ├── Safe request ─────────────► Continue
     │
     ├── Valid CSRF token ─────────► Continue
     │
     └── Missing/invalid token ────► 403 Forbidden
```

The exact internal filter-chain behavior depends on the application's security configuration, but the important concept is that CSRF validation occurs inside Spring Security's filter chain before the controller handles the request.

---

## Safe vs State-Changing Requests

CSRF protection primarily concerns requests that can change application state.

Typical state-changing operations include:

```text
POST
PUT
PATCH
DELETE
```

For example:

```text
POST /transfer
POST /users
PUT /users/123
PATCH /users/123
DELETE /users/123
```

These operations can potentially modify application state and therefore require protection against CSRF attacks.

---

## Test Cases

The example verifies:

| Scenario                                    | Expected Result |
|---------------------------------------------|----------------:|
| Authenticated request without CSRF token    | `403 Forbidden` |
| Authenticated request with valid CSRF token |        `200 OK` |
| Unauthenticated request without CSRF token  | `403 Forbidden` |

The tests intentionally demonstrate that CSRF validation is independent from authentication.

---

## CSRF vs CORS

CSRF and CORS solve different problems.

**CSRF** protects applications from unwanted authenticated state-changing requests.

**CORS** controls which origins are allowed to make cross-origin browser requests.

They should not be treated as interchangeable security mechanisms.

---

## When Might CSRF Protection Be Disabled?

CSRF protection should not be disabled simply because an application exposes REST endpoints.

The appropriate approach depends on how the application authenticates requests and whether credentials are automatically included by the browser.

For example, a genuinely stateless API using an authentication mechanism where credentials are explicitly supplied with each request may have different CSRF considerations from a browser application using session cookies.

If CSRF protection is disabled, the application should have another appropriate security model for protecting state-changing requests.

In a Spring Security configuration, disabling CSRF would look like:

```java
http.csrf(AbstractHttpConfigurer::disable);
```

This example intentionally does **not** do that because its purpose is to demonstrate CSRF protection.

---

## Running the Example

From the repository root:

```bash
mvn -pl 01-spring-security/csrf-protection spring-boot:run
```

The application starts with the configured Spring Security rules.

---

## Running the Tests

Run the tests from the module:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 01-spring-security/csrf-protection test
```

---

## Key Takeaways

- CSRF stands for Cross-Site Request Forgery.
- Spring Security enables CSRF protection by default.
- CSRF protection is particularly important for browser applications using automatically supplied authentication credentials.
- `CsrfFilter` performs CSRF validation inside the Spring Security filter chain.
- State-changing requests require appropriate CSRF protection.
- A missing CSRF token can result in `403 Forbidden`.
- Spring Security Test provides `csrf()` for testing protected requests.
- Authentication and CSRF protection solve different security problems.
- An unauthenticated request can receive `403` when CSRF validation rejects it first.
- CSRF protection should only be disabled when the application's authentication and request model make that appropriate.

---

## Next Example

The next example will explore **Session Management** in Spring Security, including session creation, session policies, and session-related security behavior.