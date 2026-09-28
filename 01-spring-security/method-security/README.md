# Method Security

This example demonstrates **method-level authorization** with Spring Security.

It shows how Spring Security can protect individual service methods using `@PreAuthorize`, rather than relying only on URL-based authorization.

## What This Example Demonstrates

- `@EnableMethodSecurity`
- `@PreAuthorize`
- Method-level authorization
- Role-based method authorization
- Authentication-based method authorization
- Service-layer security
- Difference between request-level and method-level authorization
- Testing method security with MockMvc

---

## What Is Method Security?

Method security allows Spring Security to apply authorization rules directly to methods.

For example:

```java
@PreAuthorize("hasRole('ADMIN')")
public String adminOperation() {
    return "Admin operation executed.";
}
```

The method can only be executed when the authenticated user has the required authority.

This allows security rules to be placed close to the operation they protect.

---

## Request-Level vs Method-Level Security

Spring Security supports authorization at different levels.

### Request-Level Authorization

Request-level authorization protects HTTP requests based on their URL or request characteristics.

For example:

```java
http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());
```

The rule is applied to the HTTP request before it reaches the controller.

The flow is approximately:

```text
HTTP Request
     │
     ▼
Security Filter Chain
     │
     ▼
Request Authorization
     │
     ▼
Controller
```

### Method-Level Authorization

Method security protects individual methods.

For example:

```java
@PreAuthorize("hasRole('ADMIN')")
public String adminOperation() {
    return "Admin operation executed.";
}
```

The flow becomes:

```text
HTTP Request
     │
     ▼
Security Filter Chain
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
     ├── Authorized ──→ Method executes
     │
     └── Unauthorized → Access denied
```

This is particularly useful when the authorization rule belongs to the business operation rather than to a specific URL.

---

## Enabling Method Security

Method security is enabled with:

```java
@EnableMethodSecurity
```

For example:

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
}
```

Without `@EnableMethodSecurity`, annotations such as `@PreAuthorize` will not be processed.

---

## `@PreAuthorize`

`@PreAuthorize` evaluates an authorization expression before a method is executed.

For example:

```java
@PreAuthorize("hasRole('USER')")
public String userOperation() {
    return "User operation executed.";
}
```

Spring Security evaluates the expression before allowing the method invocation to proceed.

---

## Role-Based Method Security

The example contains a method requiring the `USER` role:

```java
@PreAuthorize("hasRole('USER')")
public String userOperation() {
    return "User operation executed.";
}
```

It also contains a method requiring the `ADMIN` role:

```java
@PreAuthorize("hasRole('ADMIN')")
public String adminOperation() {
    return "Admin operation executed.";
}
```

The configured users are:

```text
user
    password: password
    roles: USER

admin
    password: password
    roles: USER, ADMIN
```

Therefore:

```text
User
 │
 ├── userOperation()        ✓
 └── adminOperation()       ✗

Admin
 │
 ├── userOperation()        ✓
 └── adminOperation()       ✓
```

---

## Authentication-Based Method Security

Method security can also check whether a user is authenticated.

This example uses:

```java
@PreAuthorize("isAuthenticated()")
public String authenticatedOperation() {
    return "Authenticated operation executed.";
}
```

Any authenticated user can execute this method.

---

## Service-Layer Security

The authorization annotations are placed on the service methods:

```java
@Service
public class MethodSecurityService {

    @PreAuthorize("hasRole('USER')")
    public String userOperation() {
        return "User operation executed.";
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String adminOperation() {
        return "Admin operation executed.";
    }

    @PreAuthorize("isAuthenticated()")
    public String authenticatedOperation() {
        return "Authenticated operation executed.";
    }
}
```

The controller delegates to the service:

```java
@RestController
public class MethodSecurityController {

    private final MethodSecurityService methodSecurityService;

    public MethodSecurityController(MethodSecurityService methodSecurityService) {
        this.methodSecurityService = methodSecurityService;
    }

    @GetMapping("/user")
    public String userOperation() {
        return methodSecurityService.userOperation();
    }

    @GetMapping("/admin")
    public String adminOperation() {
        return methodSecurityService.adminOperation();
    }

    @GetMapping("/authenticated")
    public String authenticatedOperation() {
        return methodSecurityService.authenticatedOperation();
    }
}
```

This keeps the authorization rule associated with the operation itself rather than only with the HTTP endpoint.

---

## Method Security Flow

The overall flow can be simplified as:

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
Controller
     │
     ▼
Service Method
     │
     ▼
Method Security Interceptor
     │
     ▼
@PreAuthorize Expression
     │
     ├── Expression evaluates to true
     │          │
     │          ▼
     │     Method executes
     │
     └── Expression evaluates to false
                │
                ▼
          Access denied
```

The important point is that method security is evaluated when the secured method is invoked.

---

## HTTP Endpoints

The example exposes three endpoints:

| Endpoint         | Required Access    |
|------------------|--------------------|
| `/user`          | `ROLE_USER`        |
| `/admin`         | `ROLE_ADMIN`       |
| `/authenticated` | Authenticated user |

### `/user`

Requires the `USER` role.

```text
user   → 200 OK
admin  → 200 OK
```

### `/admin`

Requires the `ADMIN` role.

```text
user   → 403 Forbidden
admin  → 200 OK
```

### `/authenticated`

Requires authentication.

```text
anonymous → 401 Unauthorized
user      → 200 OK
admin     → 200 OK
```

---

## 401 vs 403

This example demonstrates an important distinction.

### 401 Unauthorized

The request has not been authenticated.

```text
No credentials
     ↓
Authentication required
     ↓
401 Unauthorized
```

### 403 Forbidden

The user is authenticated but does not have the required authority.

```text
Authenticated user
        ↓
Missing required role
        ↓
403 Forbidden
```

For example, when `user` accesses `/admin`:

```text
user
 │
 ├── authenticated ✓
 │
 ├── ROLE_USER ✓
 │
 └── ROLE_ADMIN ✗
          │
          ▼
     403 Forbidden
```

---

## Testing

The example uses Spring Security's testing support together with MockMvc.

The tests verify:

- A user can access a method requiring `USER`.
- A regular user cannot access a method requiring `ADMIN`.
- An admin can access a method requiring `ADMIN`.
- An authenticated user can access an authenticated-only method.
- An unauthenticated request is rejected.

For example:

```java
@Test
void user_shouldNotAccessAdminMethod() throws Exception {
    mockMvc.perform(get("/admin").with(httpBasic("user", "password"))).andExpect(status().isForbidden());
}
```

The test demonstrates that authentication alone is not sufficient when the method requires a specific role.

---

## Key Concepts

### `@EnableMethodSecurity`

Enables method-level security annotations.

```java
@EnableMethodSecurity
```

### `@PreAuthorize`

Evaluates an authorization expression before method execution.

```java
@PreAuthorize("hasRole('ADMIN')")
```

### `hasRole()`

Checks whether the authenticated user has the specified role.

```java
@PreAuthorize("hasRole('USER')")
```

When using `hasRole("USER")`, Spring Security checks for the `ROLE_USER` authority.

### `isAuthenticated()`

Checks whether the current user has been authenticated.

```java
@PreAuthorize("isAuthenticated()")
```

---

## What This Example Does Not Cover

This example intentionally focuses on the fundamentals of method security.

It does not cover:

- `@PostAuthorize`
- `@PreFilter`
- `@PostFilter`
- `@Secured`
- `@RolesAllowed`
- Custom authorization expressions
- Custom authorization managers
- Complex method parameters
- Object-level authorization

These concepts can be explored separately if they become useful to the project.

---

## Running the Example

From the module directory:

```bash
mvn spring-boot:run
```

The application starts with HTTP Basic authentication enabled.

Example request:

```bash
curl -u user:password http://localhost:8080/user
```

Admin request:

```bash
curl -u admin:password http://localhost:8080/admin
```

A regular user attempting the admin operation:

```bash
curl -u user:password http://localhost:8080/admin
```

returns:

```text
403 Forbidden
```

---

## Running the Tests

Run:

```bash
mvn test
```

The tests demonstrate both successful and rejected method-level authorization.

---

## Key Takeaways

- Method security protects individual methods.
- `@EnableMethodSecurity` enables method-level security.
- `@PreAuthorize` evaluates authorization before method execution.
- Method security can be applied to service-layer operations.
- `hasRole()` can enforce role-based access.
- `isAuthenticated()` can require authentication without requiring a specific role.
- Request-level and method-level authorization solve different problems.
- Authentication and authorization are separate concerns.
- `401 Unauthorized` indicates that authentication is required.
- `403 Forbidden` indicates that the authenticated user lacks the required access.
- Method-level authorization can keep security rules close to the business operation they protect.

---

## Next Example

The next example will explore **CSRF Protection** in Spring Security, including CSRF tokens, request validation, 
and testing CSRF-protected requests.
