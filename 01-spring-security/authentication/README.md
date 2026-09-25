# Authentication

Authentication is the process of verifying the identity of a user or other principal.

In Spring Security, authentication involves several components working together. Credentials supplied with a request are processed by an authentication mechanism and passed to an `AuthenticationManager`, which delegates the authentication to an appropriate `AuthenticationProvider`.

This example demonstrates username-and-password authentication using `AuthenticationManager`, `ProviderManager`, `DaoAuthenticationProvider`, `UserDetailsService`, and `PasswordEncoder`.

## Learning Objectives

By completing this example, you will understand:

- What authentication means in Spring Security
- The role of `Authentication`
- The role of `AuthenticationManager`
- How `ProviderManager` delegates authentication
- The role of `AuthenticationProvider`
- How `DaoAuthenticationProvider` authenticates username and password
- How `UserDetailsService` provides user information
- How `PasswordEncoder` validates passwords
- How authentication failures are handled
- How the authenticated user can be accessed from a controller
- The relationship between authentication and the `SecurityContext`

---

## Authentication Flow

A simplified authentication flow looks like this:

```text
HTTP Request
     │
     │ Username + Password
     ▼
Authentication Filter
     │
     ▼
UsernamePasswordAuthenticationToken
     │
     ▼
AuthenticationManager
     │
     ▼
ProviderManager
     │
     ▼
DaoAuthenticationProvider
     │
     ├───────────────┐
     ▼               ▼
UserDetailsService  PasswordEncoder
     │               │
     ▼               ▼
UserDetails       Password Check
     │               │
     └───────┬───────┘
             ▼
      Authentication
             │
             ▼
      SecurityContext
             │
             ▼
         Controller
```

The important idea is that authentication is handled by a chain of cooperating Spring Security components rather than by a single class.

---

## What Is Authentication?

Authentication answers the question:

> Who are you?

For username-and-password authentication, the application receives credentials such as:

```text
Username: user
Password: password
```

Spring Security validates these credentials against the configured user information.

A successful authentication produces an authenticated `Authentication` object.

```text
Credentials
     │
     ▼
Authentication
     │
     ▼
Authenticated Principal
```

Authentication is different from authorization.

### Authentication

Determines the identity of the requester.

```text
Who are you?
```

### Authorization

Determines whether the authenticated requester is allowed to perform an action.

```text
Are you allowed to do this?
```

This example focuses on authentication. Authorization is explored separately in Module 01.

---

## Authentication

Spring Security represents authentication information using the `Authentication` interface.

An `Authentication` can contain information such as:

- Principal
- Credentials
- Authorities
- Authentication state

Conceptually:

```text
Authentication
     │
     ├── Principal
     ├── Credentials
     ├── Authorities
     └── Authenticated state
```

Before authentication succeeds, an `Authentication` can represent an authentication request.

After successful authentication, it represents the authenticated principal.

---

## AuthenticationManager

`AuthenticationManager` is the main abstraction responsible for authenticating an `Authentication` request.

Its central operation is:

```java
Authentication authenticate(Authentication authentication)
```

Conceptually:

```text
Authentication Request
        │
        ▼
AuthenticationManager
        │
        ▼
Authenticated Authentication
```

The application configures an `AuthenticationManager` in this example:

```java
@Bean
AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder);

    return new ProviderManager(authenticationProvider);
}
```

---

## ProviderManager

`ProviderManager` is an implementation of `AuthenticationManager`.

It delegates authentication to one or more `AuthenticationProvider` implementations.

The example creates a `ProviderManager` with a single provider:

```java
return new ProviderManager(authenticationProvider);
```

The relationship is:

```text
AuthenticationManager
        │
        ▼
ProviderManager
        │
        ▼
AuthenticationProvider
```

A `ProviderManager` can work with multiple authentication providers when an application supports multiple authentication mechanisms.

For this example, there is one provider:

```text
ProviderManager
      │
      ▼
DaoAuthenticationProvider
```

---

## AuthenticationProvider

`AuthenticationProvider` defines the mechanism used to authenticate a particular type of authentication request.

Conceptually:

```text
AuthenticationManager
        │
        ▼
AuthenticationProvider
        │
        ▼
Authenticate credentials
```

Spring Security provides multiple authentication provider implementations for different authentication mechanisms.

This example uses:

```text
DaoAuthenticationProvider
```

for username-and-password authentication.

---

## DaoAuthenticationProvider

`DaoAuthenticationProvider` is responsible for authenticating a username and password using user information obtained from a `UserDetailsService`.

The configuration is:

```java
DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);

authenticationProvider.setPasswordEncoder(passwordEncoder);
```

The authentication flow is:

```text
Username + Password
        │
        ▼
DaoAuthenticationProvider
        │
        ├── UserDetailsService
        │       │
        │       ▼
        │   UserDetails
        │
        └── PasswordEncoder
                │
                ▼
          Password validation
```

If the user exists and the password is valid, authentication succeeds.

If the username does not exist or the password is invalid, authentication fails.

---

## UserDetailsService

`UserDetailsService` provides user information to Spring Security.

In this example, users are stored in memory:

```java
@Bean
UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails user = User.withUsername("user").password(passwordEncoder.encode("password")).roles("USER").build();

    return new InMemoryUserDetailsManager(user);
}
```

The important distinction is:

```text
UserDetailsService
        │
        ▼
Loads user information
```

It does not perform the password authentication itself.

The authentication is performed by `DaoAuthenticationProvider`, which uses `UserDetailsService` as part of that process.

Conceptually:

```text
DaoAuthenticationProvider
        │
        ├── Ask UserDetailsService for user
        │
        ▼
    UserDetails
        │
        └── Validate submitted password
```

A dedicated example later in Module 01 explores `UserDetailsService` in greater detail.

---

## PasswordEncoder

Passwords should not be stored as plain text.

The example configures a `PasswordEncoder`:

```java
@Bean
PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

The password is encoded when the user is created:

```java
.password(passwordEncoder.encode("password"))
```

During authentication, the `DaoAuthenticationProvider` uses the `PasswordEncoder` to verify the supplied password.

Conceptually:

```text
Stored Password
      │
      │
      ▼
PasswordEncoder
      ▲
      │
Submitted Password
```

The Password Encoding example later in Module 01 explores password encoding in greater detail.

---

## UsernamePasswordAuthenticationToken

Username-and-password authentication uses `UsernamePasswordAuthenticationToken` to represent the authentication request.

Conceptually:

```text
Username
   +
Password
   │
   ▼
UsernamePasswordAuthenticationToken
   │
   ▼
AuthenticationManager
```

The authentication request contains the credentials that need to be validated.

After successful authentication, Spring Security produces an authenticated `Authentication` representing the principal.

---

## Successful Authentication

When the credentials are valid, the authentication process succeeds.

```text
user / password
       │
       ▼
AuthenticationManager
       │
       ▼
ProviderManager
       │
       ▼
DaoAuthenticationProvider
       │
       ├── UserDetailsService
       │
       └── PasswordEncoder
       │
       ▼
Successful Authentication
```

The authenticated user can then be accessed by the application.

For example:

```java
@GetMapping("/authenticated")
public String authenticatedEndpoint(Authentication authentication) {
    return "Authenticated as: " + authentication.getName();
}
```

For the configured user, the response is:

```text
Authenticated as: user
```

---

## Failed Authentication

Authentication fails when the supplied credentials cannot be validated.

For example, an incorrect password:

```text
Username: user
Password: wrong-password
```

results in an authentication failure.

Similarly, an unknown username cannot be authenticated.

From the HTTP perspective, the example returns:

```text
401 Unauthorized
```

for unauthenticated requests to protected endpoints.

```text
Invalid Credentials
       │
       ▼
Authentication Failure
       │
       ▼
401 Unauthorized
```

---

## SecurityContext

After successful authentication, Spring Security makes the authenticated `Authentication` available through the security context.

Conceptually:

```text
Successful Authentication
          │
          ▼
    SecurityContext
          │
          ▼
 SecurityContextHolder
          │
          ▼
 Current Authentication
```

The application can access the current authentication through Spring Security mechanisms such as controller method parameters.

For example:

```java
@GetMapping("/authenticated")
public String authenticatedEndpoint(Authentication authentication) {
    return "Authenticated as: " + authentication.getName();
}
```

The SecurityContext is covered in greater detail in a later Module 01 example.

---

## Complete Authentication Configuration

The complete configuration is:

```java
@Configuration
public class AuthenticationConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/public").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authenticationProvider);
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

The configuration brings together the authentication components:

```text
AuthenticationManager
        │
        ▼
ProviderManager
        │
        ▼
DaoAuthenticationProvider
        │
        ├── UserDetailsService
        │
        └── PasswordEncoder
```

---

## Authentication Controller

The controller exposes a public endpoint and an authenticated endpoint:

```java
@RestController
public class AuthenticationController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/authenticated")
    public String authenticatedEndpoint(Authentication authentication) {
        return "Authenticated as: " + authentication.getName();
    }
}
```

The `Authentication` parameter gives the controller access to the authenticated principal.

---

## Authentication Test Matrix

| Scenario           | Credentials       | Expected Result    |
|--------------------|-------------------|--------------------|
| Public endpoint    | None              | `200 OK`           |
| Protected endpoint | None              | `401 Unauthorized` |
| Protected endpoint | Valid credentials | `200 OK`           |
| Protected endpoint | Wrong password    | `401 Unauthorized` |
| Protected endpoint | Unknown username  | `401 Unauthorized` |

The tests demonstrate both successful and unsuccessful authentication.

---

## Authentication vs Authorization

Authentication and authorization should not be confused.

```text
Authentication
      │
      ▼
Who are you?
      │
      ▼
Authenticated Principal
      │
      ▼
Authorization
      │
      ▼
What are you allowed to access?
```

For example:

```text
user/password
     │
     ▼
Authentication
     │
     ▼
Authenticated as "user"
     │
     ▼
Authorization
     │
     ▼
Can "user" access this resource?
```

This example focuses on the first part of that process.

Authorization is covered separately in Module 01.

---

## Authentication Component Relationships

The major components introduced by this example can be summarized as:

| Component                   | Responsibility                               |
|-----------------------------|----------------------------------------------|
| `Authentication`            | Represents authentication information        |
| `AuthenticationManager`     | Main authentication API                      |
| `ProviderManager`           | Delegates authentication to providers        |
| `AuthenticationProvider`    | Performs a specific authentication mechanism |
| `DaoAuthenticationProvider` | Authenticates username and password          |
| `UserDetailsService`        | Loads user information                       |
| `UserDetails`               | Represents the loaded user                   |
| `PasswordEncoder`           | Verifies encoded passwords                   |
| `SecurityContext`           | Holds the authenticated `Authentication`     |

---

## Complete Authentication Flow

Putting everything together:

```text
                     HTTP Request
                          │
                          │ user / password
                          ▼
              Authentication Filter
                          │
                          ▼
       UsernamePasswordAuthenticationToken
                          │
                          ▼
               AuthenticationManager
                          │
                          ▼
                  ProviderManager
                          │
                          ▼
              DaoAuthenticationProvider
                          │
                    ┌─────┴─────┐
                    │           │
                    ▼           ▼
          UserDetailsService  PasswordEncoder
                    │           │
                    ▼           ▼
               UserDetails   Password Check
                    │           │
                    └─────┬─────┘
                          ▼
                Authentication
                          │
                          ▼
                  SecurityContext
                          │
                          ▼
                     Controller
```

---

## Running the Example

From the example directory, run:

```bash
mvn clean verify
```

To start the application:

```bash
mvn spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

Test the public endpoint:

```bash
curl http://localhost:8080/public
```

Test the protected endpoint without credentials:

```bash
curl -i http://localhost:8080/authenticated
```

Test the protected endpoint with valid credentials:

```bash
curl -i -u user:password http://localhost:8080/authenticated
```

---

## Key Concepts

### `Authentication`

Represents the authentication request or authenticated principal.

### `AuthenticationManager`

Defines the API responsible for authenticating an `Authentication`.

### `ProviderManager`

Common `AuthenticationManager` implementation that delegates to `AuthenticationProvider` instances.

### `AuthenticationProvider`

Performs a particular authentication mechanism.

### `DaoAuthenticationProvider`

Authenticates username-and-password credentials using `UserDetailsService` and `PasswordEncoder`.

### `UserDetailsService`

Loads user information used during authentication.

### `PasswordEncoder`

Validates passwords against their encoded representation.

### `SecurityContext`

Contains the authenticated `Authentication`.

---

## Key Takeaways

1. Authentication verifies the identity of a requester.
2. `AuthenticationManager` is the main abstraction for performing authentication.
3. `ProviderManager` delegates authentication to `AuthenticationProvider` implementations.
4. `DaoAuthenticationProvider` handles username-and-password authentication.
5. `UserDetailsService` provides user information to the authentication provider.
6. `PasswordEncoder` is used to validate passwords securely.
7. Successful authentication produces an authenticated `Authentication`.
8. The authenticated `Authentication` is made available through the `SecurityContext`.
9. Invalid credentials result in authentication failure.
10. Authentication and authorization are separate concerns.

The central idea is:

```text
Credentials
     │
     ▼
AuthenticationManager
     │
     ▼
AuthenticationProvider
     │
     ├── UserDetailsService
     └── PasswordEncoder
     │
     ▼
Authenticated Principal
     │
     ▼
SecurityContext
```

---

## Next

**UserDetailsService**

The next example focuses specifically on `UserDetailsService`, including how Spring Security loads user information and how different `UserDetailsService` implementations can be configured.