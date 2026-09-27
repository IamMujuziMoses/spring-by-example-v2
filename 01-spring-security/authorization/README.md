# Authorization

This example demonstrates how **Spring Security authorization** controls access to application resources based on the authenticated user's roles and authorities.

The example builds on the previous Spring Security examples by showing how authentication and authorization work together:

```text
Authentication
      │
      │ Who are you?
      ▼
Authenticated User
      │
      │ What are you allowed to access?
      ▼
Authorization
      │
      ▼
Access Decision
```

## What This Example Demonstrates

- Authorization with `authorizeHttpRequests`
- Public endpoints with `permitAll()`
- Authentication requirements with `authenticated()`
- Role-based authorization with `hasRole()`
- Explicit denial with `denyAll()`
- Multiple users with different roles
- The difference between authentication and authorization
- The difference between `401 Unauthorized` and `403 Forbidden`
- Authorization rule ordering
- Testing authorization with MockMvc

---

## Authentication vs Authorization

Authentication and authorization answer different questions.

### Authentication

Authentication answers:

> Who are you?

For example, a user provides:

```text
Username: user
Password: password
```

Spring Security verifies those credentials and establishes an authenticated security context.

### Authorization

Authorization answers:

> What are you allowed to access?

Once the user is authenticated, Spring Security evaluates the authorization rules.

For example:

```text
user
 └── ROLE_USER

admin
 ├── ROLE_USER
 └── ROLE_ADMIN
```

The `user` can access resources requiring `ROLE_USER`, while the `admin` can access resources requiring either `ROLE_USER` or `ROLE_ADMIN`.

---

## Security Configuration

The authorization rules are configured using `authorizeHttpRequests`:

```java
http.authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/public").permitAll()
        .requestMatchers("/admin").hasRole("ADMIN")
        .requestMatchers("/user").hasRole("USER")
        .requestMatchers("/authenticated").authenticated()
        .anyRequest().denyAll());
```

These rules define who can access each endpoint.

### Public Endpoint

```java
.requestMatchers("/public").permitAll()
```

Anyone can access `/public`, including unauthenticated users.

```text
GET /public
      │
      ▼
   permitAll
      │
      ▼
     200
```

### Authenticated Users

```java
.requestMatchers("/authenticated").authenticated()
```

The endpoint requires the user to be authenticated.

The user's specific role does not matter.

```text
GET /authenticated
      │
      ├── Not authenticated → 401
      │
      └── Authenticated → 200
```

### User Role

```java
.requestMatchers("/user").hasRole("USER")
```

The user must have the `USER` role.

The configured user has:

```text
ROLE_USER
```

so they can access `/user`.

### Admin Role

```java
.requestMatchers("/admin").hasRole("ADMIN")
```

Only users with the `ADMIN` role can access `/admin`.

The configured admin user has:

```text
ROLE_USER
ROLE_ADMIN
```

so the admin can access both `/user` and `/admin`.

### Denying Everything Else

```java
.anyRequest().denyAll()
```

Any request that does not match one of the previous rules is explicitly denied.

For example:

```text
GET /unknown
```

does not match any configured endpoint, so an authenticated user receives:

```text
403 Forbidden
```

---

## Roles

The example creates users using:

```java
UserDetails user = User.withUsername("user").password(passwordEncoder.encode("password")).roles("USER").build();
UserDetails admin = User.withUsername("admin").password(passwordEncoder.encode("password")).roles("USER", "ADMIN").build();
```

When using:

```java
.roles("USER")
```

Spring Security creates the authority:

```text
ROLE_USER
```

Likewise:

```java
.roles("ADMIN")
```

creates:

```text
ROLE_ADMIN
```

This is why the authorization rule can use:

```java
.hasRole("ADMIN")
```

rather than:

```java
.hasRole("ROLE_ADMIN")
```

---

## `hasRole()` vs `hasAuthority()`

Spring Security provides both role-based and authority-based authorization methods.

### `hasRole()`

```java
.hasRole("ADMIN")
```

Spring Security applies the standard `ROLE_` prefix and checks for:

```text
ROLE_ADMIN
```

### `hasAuthority()`

```java
.hasAuthority("ROLE_ADMIN")
```

This checks the authority string directly.

For example:

```java
.requestMatchers("/admin").hasAuthority("ROLE_ADMIN")
```

is equivalent to:

```java
.requestMatchers("/admin").hasRole("ADMIN")
```

when the user has the standard `ROLE_` authority.

The example uses `hasRole()` because the users are configured using roles.

---

## Authorization Rules and Ordering

Authorization rules are evaluated in the order they are declared.

For example:

```java
.authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/public").permitAll()
        .requestMatchers("/admin").hasRole("ADMIN")
        .requestMatchers("/user").hasRole("USER")
        .requestMatchers("/authenticated").authenticated()
        .anyRequest().denyAll())
```

The specific endpoint rules appear before the catch-all rule:

```java
.anyRequest().denyAll()
```

The catch-all rule therefore applies only to requests that were not matched earlier.

A general rule placed too early can prevent later, more specific rules from being reached.

---

## HTTP 401 vs 403

Authorization examples are useful for understanding the difference between `401 Unauthorized` and `403 Forbidden`.

### 401 Unauthorized

A `401` response means the request does not have valid authentication.

For example:

```text
GET /authenticated
```

without credentials results in:

```text
401 Unauthorized
```

The user has not successfully authenticated.

### 403 Forbidden

A `403` response means the request is authenticated, but the authenticated user is not authorized to access the resource.

For example:

```text
user → GET /admin
```

The user is authenticated, but only `ROLE_ADMIN` is allowed.

Therefore:

```text
403 Forbidden
```

The distinction can be summarized as:

```text
401 → "You are not authenticated."
403 → "You are authenticated, but you are not allowed here."
```

---

## Password Encoding

The example uses the same password-encoding approach introduced in the previous example:

```java
@Bean
PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

Users are created with encoded passwords:

```java
.password(passwordEncoder.encode("password"))
```

The focus of this example is authorization, so password encoding is used as authentication infrastructure rather than explained in detail.

See the previous **Password Encoding** example for a dedicated explanation of `PasswordEncoder`, `encode()`, and `matches()`.

---

## Controller

The controller exposes four endpoints:

```java
@RestController
public class AuthorizationController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/authenticated")
    public String authenticatedEndpoint() {
        return "This endpoint requires authentication.";
    }

    @GetMapping("/user")
    public String userEndpoint() {
        return "This endpoint requires the USER role.";
    }

    @GetMapping("/admin")
    public String adminEndpoint() {
        return "This endpoint requires the ADMIN role.";
    }
}
```

The controller itself does not contain authorization logic.

Authorization is handled by Spring Security before the request reaches the controller.

---

## Authorization Flow

A request to `/admin` demonstrates the complete process:

```text
HTTP Request
     │
     │ GET /admin
     ▼
Spring Security Filter Chain
     │
     ▼
Authentication
     │
     ├── Not authenticated
     │       │
     │       ▼
     │      401
     │
     └── Authenticated
             │
             ▼
       Authorization
             │
             │ Has ROLE_ADMIN?
             │
             ├── No
             │   │
             │   ▼
             │  403
             │
             └── Yes
                 │
                 ▼
             Controller
                 │
                 ▼
                200
```

---

## Authorization Examples

The configured users behave as follows:

| User            | Roles       | `/public` | `/authenticated` | `/user` | `/admin` |
|-----------------|-------------|----------:|-----------------:|--------:|---------:|
| Unauthenticated | None        |       200 |              401 |     401 |      401 |
| `user`          | USER        |       200 |              200 |     200 |      403 |
| `admin`         | USER, ADMIN |       200 |              200 |     200 |      200 |

This illustrates that authorization is performed after authentication.

---

## Test Coverage

The tests verify:

- Public endpoint is accessible without authentication.
- Authenticated endpoint accepts authenticated users.
- Authenticated endpoint rejects unauthenticated users.
- Users with `ROLE_USER` can access user resources.
- Users without `ROLE_ADMIN` cannot access admin resources.
- Users with `ROLE_ADMIN` can access admin resources.
- Unmatched requests are denied.

---

## Key Takeaways

1. **Authentication and authorization are different concerns.**
2. Authentication determines who the user is.
3. Authorization determines what the authenticated user can access.
4. `permitAll()` allows everyone to access a resource.
5. `authenticated()` requires authentication.
6. `hasRole()` restricts access based on a role.
7. `hasAuthority()` checks an authority directly.
8. `denyAll()` explicitly rejects matching requests.
9. `hasRole("ADMIN")` normally checks for `ROLE_ADMIN`.
10. `401` indicates an authentication problem, while `403` indicates an authorization problem.
11. Authorization rules are evaluated in their configured order.
12. Authorization is enforced by Spring Security before the request reaches the controller.

---

## What This Example Does Not Cover

This example intentionally focuses on request-based authorization.

It does not cover:

- Method security
- `@PreAuthorize`
- `@PostAuthorize`
- `@Secured`
- `@RolesAllowed`
- Custom authorization managers
- OAuth2 authorization
- JWT-based authorization
- Database-backed users and roles
- Fine-grained domain-level permissions

These topics are covered by later examples in the Spring Security module.

---

## Running the Example

From the `authorization` directory:

```bash
mvn spring-boot:run
```

The application can then be accessed at:

```text
http://localhost:8080/public
http://localhost:8080/authenticated
http://localhost:8080/user
http://localhost:8080/admin
```

Example credentials:

```text
User:
Username: user
Password: password

Admin:
Username: admin
Password: password
```

---

## Running the Tests

```bash
mvn test
```

All authorization scenarios should pass.

---

## Next Example

The next example will demonstrate **SecurityContext**, showing how Spring Security stores and exposes the authentication information associated with the current request.