# Password Encoding Example

This example demonstrates how Spring Security uses `PasswordEncoder` to encode passwords and verify supplied passwords during authentication.

It builds on the previous **UserDetailsService** example and focuses specifically on password encoding, password verification, `PasswordEncoder`, and `DelegatingPasswordEncoder`.

## Overview

Passwords should not be stored as plain text.

Instead, an application should store an encoded representation of the password and use that representation when verifying authentication attempts.

The basic flow is:

```text
Raw Password
     │
     ▼
PasswordEncoder
     │
     ▼
Encoded Password
     │
     ▼
Store Encoded Password
```

During authentication:

```text
Login Request
     │
     │ Raw Password
     ▼
AuthenticationProvider
     │
     ▼
PasswordEncoder.matches()
     │
     │ Compare
     ▼
Stored Encoded Password
     │
     ▼
Authentication Result
```

The application does not need to decode the stored password.

Instead, `PasswordEncoder.matches()` determines whether the supplied raw password matches the stored encoded representation.

---

## What This Example Demonstrates

This example covers:

- `PasswordEncoder`
- Password encoding with `encode()`
- Password verification with `matches()`
- `DelegatingPasswordEncoder`
- `PasswordEncoderFactories.createDelegatingPasswordEncoder()`
- Encoded password identifiers
- Integrating `PasswordEncoder` with `UserDetailsService`
- Password verification during authentication
- Testing password encoding
- Testing successful authentication
- Testing failed authentication

---

## 1. Why Password Encoding Matters

Passwords should not be stored as plain text.

For example, storing:

```text
password
```

would expose the original password if the stored data were compromised.

Instead, the application should store an encoded representation:

```text
password
   │
   ▼
PasswordEncoder
   │
   ▼
Encoded representation
```

The original password is not stored as the value used for authentication.

During a login attempt, Spring Security receives the password supplied by the user and checks it against the stored encoded value.

```text
Supplied Password
        │
        ▼
PasswordEncoder.matches()
        │
        ▼
Stored Encoded Password
        │
        ▼
true / false
```

---

## 2. PasswordEncoder

`PasswordEncoder` is the Spring Security abstraction responsible for encoding passwords and checking password matches.

Its two most important operations are:

```java
String encode(CharSequence rawPassword);

boolean matches(CharSequence rawPassword, String encodedPassword);
```

### encode()

The `encode()` method transforms a raw password into an encoded representation.

```java
String encodedPassword = passwordEncoder.encode("password");
```

The result should not be treated as the original password.

Conceptually:

```text
"password"
     │
     ▼
encode()
     │
     ▼
"{algorithm}encoded-value"
```

### matches()

The `matches()` method checks whether a raw password corresponds to an encoded password.

```java
boolean matches = passwordEncoder.matches("password", encodedPassword);
```

The result is:

```text
true
```

for the correct password and:

```text
false
```

for an incorrect password.

---

## 3. Creating a PasswordEncoder

This example uses:

```java
@Bean
PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

`PasswordEncoderFactories.createDelegatingPasswordEncoder()` creates a `DelegatingPasswordEncoder`.

This allows Spring Security to support multiple password-encoding strategies through a single `PasswordEncoder` abstraction.

---

## 4. DelegatingPasswordEncoder

The delegating encoder uses an identifier to determine which password encoder should be used for a stored password.

An encoded password can conceptually look like:

```text
{algorithm}encoded-value
```

The identifier tells Spring Security which encoder should handle the encoded password.

For example:

```text
{algorithm}encoded-value
  │
  └── Encoder identifier
```

The exact encoded value depends on the encoder and its configuration.

The important concept is that the encoding strategy is identified alongside the encoded password.

This allows applications to support different password-encoding strategies and migrate between them more easily.

---

## 5. Security Configuration

The security configuration defines the `PasswordEncoder` as a Spring bean:

```java
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/public").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("user").password(passwordEncoder.encode("password")).roles("USER").build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
```

The important part is:

```java
.password(passwordEncoder.encode("password"))
```

The raw password is encoded before being placed into the `UserDetails`.

This replaces the `{noop}` password used in the previous `UserDetailsService` example:

```java
.password("{noop}password")
```

The progression is therefore:

```text
UserDetailsService Example

.password("{noop}password")
```

becomes:

```text
Password Encoding Example

.password(passwordEncoder.encode("password"))
```

---

## 6. Password Encoding During User Creation

When creating a user, the application should encode the raw password before storing it.

```text
User Creation
     │
     │ Raw password
     ▼
PasswordEncoder.encode()
     │
     ▼
Encoded password
     │
     ▼
UserDetails
     │
     ▼
User Store
```

For example:

```java
UserDetails user = User.withUsername("user").password(passwordEncoder.encode("password")).roles("USER").build();
```

The raw password is only supplied to the encoder.

The resulting encoded password becomes part of the `UserDetails`.

---

## 7. Password Verification During Authentication

When the user authenticates, the process is different.

The user supplies the raw password:

```text
password
```

Spring Security retrieves the stored `UserDetails`, including its encoded password.

The authentication provider then uses the `PasswordEncoder` to verify the supplied password.

Conceptually:

```text
Login Request
     │
     ├── Username: user
     └── Password: password
              │
              ▼
     AuthenticationProvider
              │
              ▼
     UserDetailsService
              │
              ▼
        UserDetails
              │
              ├── Username
              └── Encoded Password
                       │
                       ▼
             PasswordEncoder.matches()
                       │
              ┌────────┴────────┐
              ▼                 ▼
            true              false
              │                 │
              ▼                 ▼
       Authentication       Authentication
          succeeds             fails
```

The stored password does not need to be decoded.

---

## 8. encode() vs matches()

These methods serve different purposes.

| Method      | Purpose                                             |
|-------------|-----------------------------------------------------|
| `encode()`  | Creates an encoded representation of a raw password |
| `matches()` | Checks a raw password against an encoded password   |

### Encoding

```java
String encodedPassword = passwordEncoder.encode("password");
```

### Verification

```java
boolean matches = passwordEncoder.matches("password", encodedPassword);
```

The application should not attempt to reverse the encoded password.

Instead, use:

```java
passwordEncoder.matches(...)
```

to verify credentials.

---

## 9. Controller

The controller exposes a public endpoint and an authenticated endpoint:

```java
@RestController
public class PasswordEncodingController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/authenticated")
    public String authenticatedEndpoint(Authentication authentication) {
        return "Authenticated user: "
                + authentication.getName();
    }
}
```

The controller does not perform password encoding itself.

Password handling belongs to the authentication infrastructure.

The controller only receives the resulting authenticated `Authentication` object.

---

## 10. Complete Password Authentication Flow

Combining the previous examples gives the following flow:

```text
HTTP Request
     │
     │ Username + Password
     ▼
SecurityFilterChain
     │
     ▼
Authentication Filter
     │
     ▼
AuthenticationManager
     │
     ▼
AuthenticationProvider
     │
     ├─────────────────────────────┐
     │                             │
     ▼                             ▼
UserDetailsService           PasswordEncoder
     │                             │
     │ loadUserByUsername()        │ matches()
     ▼                             │
UserDetails                       │
     │                             │
     └──────────────┬──────────────┘
                    ▼
             Authentication
                    │
                    ▼
             SecurityContext
                    │
                    ▼
               Controller
```

The responsibilities are now clearer:

| Component                | Responsibility                                  |
|--------------------------|-------------------------------------------------|
| `SecurityFilterChain`    | Defines the security rules and filters          |
| `AuthenticationManager`  | Coordinates authentication                      |
| `AuthenticationProvider` | Performs authentication                         |
| `UserDetailsService`     | Loads user information                          |
| `UserDetails`            | Represents user security information            |
| `PasswordEncoder`        | Encodes and verifies passwords                  |
| `SecurityContext`        | Holds the authenticated user's security context |

---

## 11. Why Passwords Are Not Decoded

A common misconception is that the application should encode a password when storing it and later decode it when authenticating.

That is not the model used here.

Instead:

```text
Store:

Raw Password
     │
     ▼
encode()
     │
     ▼
Encoded Password
```

Then:

```text
Authenticate:

Raw Password
     │
     ▼
matches()
     │
     ▼
Encoded Password
```

The stored representation is used for verification rather than being decoded back into the original password.

---

## 12. Why This Example Uses DelegatingPasswordEncoder

The example uses:

```java
PasswordEncoderFactories.createDelegatingPasswordEncoder()
```

rather than manually selecting a specific encoder implementation.

This keeps the example aligned with Spring Security's password-encoding abstraction.

The application depends on:

```java
PasswordEncoder
```

rather than directly coupling the authentication configuration to one concrete encoder.

Conceptually:

```text
Application
     │
     ▼
PasswordEncoder
     │
     ▼
DelegatingPasswordEncoder
     │
     ▼
Appropriate Encoding Strategy
```

This abstraction also makes it easier to support password-encoding migrations.

---

## 13. What This Example Does Not Cover

To keep the example focused, the following topics are intentionally covered elsewhere.

### User Loading

`UserDetailsService` and `loadUserByUsername()` are covered in the previous example.

### Authorization

Roles, authorities, and access decisions are covered in the upcoming **Authorization** example.

### Database Persistence

Database-backed user storage belongs to the **Spring Data** module.

### OAuth2 and JWT

These are covered later in the Spring Security module.

---

## 14. Running the Example

From the project root:

```bash
mvn clean verify
```

To run this example directly:

```bash
cd 01-spring-security/password-encoding
mvn spring-boot:run
```

The application starts on the default Spring Boot port:

```text
http://localhost:8080
```

### Public Endpoint

```bash
curl http://localhost:8080/public
```

Expected response:

```text
This endpoint is public.
```

### Correct Password

```bash
curl -u user:password http://localhost:8080/authenticated
```

Expected response:

```text
Authenticated user: user
```

### Incorrect Password

```bash
curl -u user:wrong-password http://localhost:8080/authenticated
```

Expected result:

```text
401 Unauthorized
```

---

## 15. Key Takeaways

The most important concepts from this example are:

1. Passwords should not be stored as plain text.
2. `PasswordEncoder` provides the abstraction for password encoding and verification.
3. `encode()` creates an encoded password representation.
4. `matches()` verifies a raw password against an encoded password.
5. Applications should verify passwords rather than attempting to decode stored passwords.
6. `DelegatingPasswordEncoder` supports password-encoding strategies through an encoder identifier.
7. `PasswordEncoder` integrates with `AuthenticationProvider` during username/password authentication.
8. `UserDetailsService` loads the user, while `PasswordEncoder` verifies the password.
9. The raw password is not stored as part of the encoded user information.
10. Password encoding and authorization are separate security concerns.

The central relationship is:

```text
UserDetailsService
        │
        ▼
UserDetails
        │
        ├── Username
        └── Encoded Password
                 │
                 ▼
          PasswordEncoder
                 │
                 │ matches()
                 ▼
        Authentication Result
```

---

## Next Example

The next example is: **Authorization**

It will focus on what an authenticated user is allowed to access, including roles, authorities, request authorization rules, and the difference between authentication and authorization.
