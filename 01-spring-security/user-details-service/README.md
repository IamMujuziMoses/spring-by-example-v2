# UserDetailsService Example

This example demonstrates how Spring Security uses `UserDetailsService` to load user information during username/password authentication.

It builds on the previous **Authentication** example and focuses specifically on the responsibility of `UserDetailsService`, the `loadUserByUsername()` method, `UserDetails`, and how loaded user information participates in the authentication process.

## Overview

When a user authenticates with a username and password, Spring Security needs a way to retrieve information about that user.

This is the responsibility of `UserDetailsService`.

The central contract is:

```java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException;
}
```

The implementation receives a username and returns a `UserDetails` object containing the information Spring Security needs during authentication.

The simplified flow is:

```text
HTTP Request
     │
     │ Username + Password
     ▼
Authentication Filter
     │
     ▼
AuthenticationManager
     │
     ▼
AuthenticationProvider
     │
     ▼
UserDetailsService
     │
     │ loadUserByUsername()
     ▼
UserDetails
     │
     ▼
AuthenticationProvider
     │
     │ Verify credentials
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

## What This Example Demonstrates

This example covers:

- `UserDetailsService`
- `loadUserByUsername()`
- `UserDetails`
- `UsernameNotFoundException`
- Custom `UserDetailsService` implementations
- Registering `UserDetailsService` as a Spring bean
- The relationship between `UserDetailsService` and `AuthenticationProvider`
- Loading a user during authentication
- Handling unknown users
- How the loaded username becomes available through `Authentication`

---

## 1. What Is UserDetailsService?

`UserDetailsService` is a Spring Security interface used to retrieve user information by username.

Its primary method is:

```java
UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
```

Spring Security calls this method when it needs to retrieve information about a user during username/password authentication.

A simple implementation looks like:

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!"user".equals(username)) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return User.withUsername("user").password("{noop}password").roles("USER").build();
    }
}
```

The implementation has a focused responsibility:

```text
Username
   │
   ▼
loadUserByUsername()
   │
   ▼
UserDetails
```

It retrieves the user information.

It does not itself perform the complete authentication process.

---

## 2. The UserDetails Interface

`UserDetails` represents the information Spring Security needs about a user.

It provides information such as:

- Username
- Password
- Authorities
- Account status
- Account expiration
- Credential expiration
- Account locking

Conceptually:

```text
UserDetails
├── username
├── password
├── authorities
├── accountNonExpired
├── accountNonLocked
├── credentialsNonExpired
└── enabled
```

Spring Security provides the `User` implementation for creating simple user definitions:

```java
UserDetails user = User.withUsername("user").password("{noop}password").roles("USER").build();
```

The returned object is then available to the authentication provider.

---

## 3. Implementing UserDetailsService

This example uses a custom implementation:

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (!"user".equals(username)) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return User.withUsername("user").password("{noop}password").roles("USER").build();
    }
}
```

The `@Service` annotation registers the implementation as a Spring bean.

Spring Security can then use this bean when it needs a `UserDetailsService`.

---

## 4. loadUserByUsername()

The most important method in this example is:

```java
loadUserByUsername(String username)
```

Spring Security provides the username supplied by the authentication request.

For example:

```text
Username: user
```

causes:

```java
loadUserByUsername("user");
```

The service then retrieves the corresponding user:

```text
"user"
  │
  ▼
loadUserByUsername("user")
  │
  ▼
UserDetails
  │
  ├── username: user
  ├── password: {noop}password
  └── role: USER
```

In a real application, the implementation could retrieve the user from:

- A relational database
- LDAP
- An external identity service
- Another persistence system
- An API

For this learning example, the user is defined directly in the service so that the focus remains on the `UserDetailsService` contract.

---

## 5. Unknown Users

A `UserDetailsService` implementation should throw `UsernameNotFoundException` when the requested username cannot be found.

```java
if (!"user".equals(username)) {
    throw new UsernameNotFoundException("User not found: " + username);
}
```

The flow becomes:

```text
Username
   │
   ▼
loadUserByUsername()
   │
   ▼
User not found
   │
   ▼
UsernameNotFoundException
   │
   ▼
Authentication fails
```

This prevents authentication from continuing without a valid user.

---

## 6. UserDetailsService Does Not Authenticate

One of the most important concepts in this example is the distinction between loading a user and authenticating a user.

`UserDetailsService`:

```text
Loads user information
```

An `AuthenticationProvider`:

```text
Uses that information during authentication
```

The relationship can be represented as:

```text
AuthenticationManager
        │
        ▼
AuthenticationProvider
        │
        ├──────────────────────┐
        │                      │
        ▼                      ▼
UserDetailsService       PasswordEncoder
        │
        ▼
UserDetails
```

The `UserDetailsService` provides the user information.

The authentication provider uses that information to determine whether authentication can succeed.

---

## 7. Relationship With DaoAuthenticationProvider

For username/password authentication, Spring Security commonly uses `DaoAuthenticationProvider`.

Conceptually:

```text
Authentication Request
        │
        ▼
DaoAuthenticationProvider
        │
        ├── UserDetailsService
        │        │
        │        ▼
        │    UserDetails
        │
        └── PasswordEncoder
                 │
                 ▼
            Password Check
```

The provider asks the `UserDetailsService` for the user:

```java
loadUserByUsername("user");
```

The service returns:

```java
UserDetails
```

The provider can then use the returned information as part of authentication.

This is why `UserDetailsService` is an important extension point in Spring Security.

---

## 8. Security Configuration

The security configuration is intentionally small:

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
}
```

The configuration:

- Allows `/public` without authentication.
- Requires authentication for other endpoints.
- Enables HTTP Basic authentication.

The custom `UserDetailsService` is discovered through its `@Service` annotation.

---

## 9. Why `{noop}` Is Used

The example uses:

```java
.password("{noop}password")
```

The `{noop}` prefix tells Spring Security to use the `noop` password encoder for this example.

This is intentional because the purpose of this module is to demonstrate `UserDetailsService`, not password encoding.

The production approach should use a proper password encoder.

Password encoding is covered separately in the next dedicated example.

The separation keeps the learning progression focused:

```text
Authentication
      │
      ▼
UserDetailsService
      │
      ▼
Password Encoding
      │
      ▼
Authorization
```

---

## 10. Controller

The controller exposes a public endpoint and an authenticated endpoint:

```java
@RestController
public class UserDetailsServiceController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/user")
    public String userEndpoint(Authentication authentication) {
        return "Authenticated user: " + authentication.getName();
    }
}
```

The authenticated endpoint receives:

```java
Authentication authentication
```

Spring Security provides the authenticated user's information through this object.

For the example user, the response is:

```text
Authenticated user: user
```

This demonstrates the relationship between the user loaded by `UserDetailsService` and the resulting authenticated principal.

---

## 11. Authentication Flow

A complete request can be viewed as:

```text
Client
  │
  │ GET /user
  │ Authorization: Basic ...
  ▼
Spring Security Filter Chain
  │
  ▼
Authentication Filter
  │
  ▼
AuthenticationManager
  │
  ▼
DaoAuthenticationProvider
  │
  ▼
CustomUserDetailsService
  │
  │ loadUserByUsername("user")
  ▼
UserDetails
  │
  ├── username
  ├── password
  └── authorities
  │
  ▼
AuthenticationProvider
  │
  │ Authenticate
  ▼
Authentication
  │
  ▼
SecurityContext
  │
  ▼
Controller
  │
  ▼
HTTP Response
```

The important part for this example is:

```text
DaoAuthenticationProvider
        │
        ▼
UserDetailsService
        │
        ▼
loadUserByUsername()
        │
        ▼
UserDetails
```

---

## 12. UserDetailsService vs UserDetails

These two types have different responsibilities.

| Component            | Responsibility                           |
|----------------------|------------------------------------------|
| `UserDetailsService` | Loads a user by username                 |
| `UserDetails`        | Represents the loaded user's information |

Conceptually:

```text
UserDetailsService
        │
        │ loadUserByUsername()
        ▼
UserDetails
```

`UserDetailsService` answers:

> "How do I find this user?"

`UserDetails` answers:

> "What information do I have about this user?"

---

## 13. UserDetailsService vs AuthenticationProvider

These components also have different responsibilities.

| Component                | Responsibility                                 |
|--------------------------|------------------------------------------------|
| `UserDetailsService`     | Retrieves user information                     |
| `AuthenticationProvider` | Performs authentication using that information |

The relationship is:

```text
AuthenticationProvider
        │
        ▼
UserDetailsService
        │
        ▼
UserDetails
```

The authentication provider coordinates the authentication process.

The user details service supplies the user information needed by that process.

---

## 14. In-Memory vs Custom UserDetailsService

Spring Security provides implementations such as:

```java
InMemoryUserDetailsManager
```

For example:

```java
@Bean
UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager(user);
}
```

This is useful for simple applications and examples.

This example instead implements the interface directly:

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
```

The reason is educational.

It makes the `UserDetailsService` contract explicit:

```text
UserDetailsService
       │
       ▼
loadUserByUsername()
       │
       ▼
Find user
       │
       ▼
Return UserDetails
```

In a real application, the implementation could replace the hard-coded lookup with a repository:

```text
UserDetailsService
       │
       ▼
UserRepository
       │
       ▼
Database
       │
       ▼
User
       │
       ▼
UserDetails
```

That integration will fit naturally with the Spring Data module later in V2.

---

## 15. What This Example Does Not Cover

To keep the example focused, the following topics are intentionally covered elsewhere.

### Password Encoding

Password hashing and `PasswordEncoder` are covered in the dedicated **Password Encoding** example.

### Authorization

Roles, authorities, and access decisions are covered in the dedicated **Authorization** example.

### Database-Backed Users

Database persistence belongs to the **Spring Data** module.

### OAuth2 and JWT

These are covered later in the Spring Security module.

---

## 16. Running the Example

From the project root:

```bash
mvn clean verify
```

To run this example directly:

```bash
cd 01-spring-security/user-details-service
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

### Authenticated Endpoint

```bash
curl -u user:password http://localhost:8080/user
```

Expected response:

```text
Authenticated user: user
```

### Unknown User

```bash
curl -u unknown:password http://localhost:8080/user
```

Expected result:

```text
401 Unauthorized
```

---

## 17. Key Takeaways

The most important concepts from this example are:

1. `UserDetailsService` loads user information by username.
2. `loadUserByUsername()` is the central method of the interface.
3. The method returns a `UserDetails` object.
4. `UsernameNotFoundException` represents an unknown user.
5. `UserDetailsService` does not perform the complete authentication process.
6. `AuthenticationProvider` uses the loaded user information during authentication.
7. `DaoAuthenticationProvider` commonly works with `UserDetailsService` for username/password authentication.
8. A custom `UserDetailsService` can retrieve users from any appropriate data source.
9. `UserDetails` represents the authenticated user's security information.
10. Password encoding is a separate concern and is covered in the next example.

The central relationship is:

```text
Username
   │
   ▼
UserDetailsService
   │
   ▼
loadUserByUsername()
   │
   ▼
UserDetails
   │
   ▼
AuthenticationProvider
   │
   ▼
Authentication
```

---

## Next Example

The next example is:

**Password Encoding**

It will focus on how Spring Security handles passwords, why passwords should not be stored in plain text, and how `PasswordEncoder` integrates with the authentication process.
