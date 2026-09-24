# Basic Spring Security

Spring Security is the framework used by Spring applications to provide authentication, authorization, and protection against common security threats.

This example introduces the basic Spring Security security model using a simple Spring Boot application with public and protected endpoints.

The example demonstrates how to configure a `SecurityFilterChain`, enable HTTP Basic authentication, configure an in-memory user, encode passwords, and test secured endpoints with MockMvc.

## Learning Objectives

By completing this example, you will understand:

- How Spring Security secures a Spring Boot application
- The role of `SecurityFilterChain`
- How to configure public and protected endpoints
- The difference between authentication and authorization
- How HTTP Basic authentication works
- How to configure an in-memory user
- The role of `UserDetailsService`
- The role of `PasswordEncoder`
- How to test Spring Security behavior with MockMvc

---

## Basic Spring Security Flow

At a high level, the example follows this flow:

```text
HTTP Request
     │
     ▼
SecurityFilterChain
     │
     ├── /public
     │      │
     │      ▼
     │   permitAll()
     │      │
     │      ▼
     │    200 OK
     │
     └── /private
            │
            ▼
       authenticated()
            │
       ┌────┴────┐
       │         │
       ▼         ▼
   No credentials   Valid credentials
       │                 │
       ▼                 ▼
   401 Unauthorized    200 OK
```

The security configuration determines whether a request can continue to the application.

---

## SecurityFilterChain

The `SecurityFilterChain` defines how incoming HTTP requests are secured.

For this example:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize.requestMatchers("/public").permitAll().anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());

    return http.build();
}
```

The configuration establishes three basic rules:

1. CSRF protection is disabled for this introductory example.
2. `/public` is accessible without authentication.
3. All other requests require authentication.

HTTP Basic authentication is enabled so that protected endpoints can be accessed using a username and password.

CSRF is covered separately in a later example.

---

## Public and Protected Endpoints

The example exposes two endpoints:

```java
@RestController
public class BasicSecurityController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/private")
    public String privateEndpoint() {
        return "This endpoint is protected.";
    }
}
```

The `/public` endpoint is explicitly permitted:

```java
.requestMatchers("/public").permitAll()
```

All other endpoints require authentication:

```java
.anyRequest().authenticated()
```

Therefore:

| Endpoint   | Authentication | Expected Response                      |
|------------|----------------|----------------------------------------|
| `/public`  | Not required   | `200 OK`                               |
| `/private` | Required       | `401 Unauthorized` without credentials |
| `/private` | Required       | `200 OK` with valid credentials        |

---

## Authentication and Authorization

Authentication and authorization are related but different concepts.

### Authentication

Authentication answers:

> Who are you?

For this example, Spring Security authenticates the user using HTTP Basic credentials.

```text
Username + Password
        │
        ▼
Authentication
        │
        ▼
Authenticated User
```

### Authorization

Authorization answers:

> Are you allowed to access this resource?

The configuration:

```java
.requestMatchers("/public").permitAll().anyRequest().authenticated()
```

determines whether a request is allowed to proceed.

The distinction is:

```text
Authentication
       │
       ▼
Identify the user
       │
       ▼
Authorization
       │
       ▼
Determine whether access is allowed
```

---

## HTTP Basic Authentication

HTTP Basic authentication sends a username and password with the HTTP request.

The configuration enables it with:

```java
.httpBasic(Customizer.withDefaults())
```

A request without credentials:

```text
GET /private
```

is rejected because the endpoint requires authentication.

A request with valid credentials:

```text
Authorization: Basic <credentials>
```

can access the protected endpoint.

This example uses HTTP Basic because it provides a simple way to demonstrate authentication without introducing more advanced authentication mechanisms.

---

## In-Memory User

The example uses a simple in-memory user:

```java
@Bean
UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails user = User.withUsername("user").password(passwordEncoder.encode("password")).roles("USER").build();

    return new InMemoryUserDetailsManager(user);
}
```

The user is configured with:

```text
Username: user
Password: password
Role: USER
```

`InMemoryUserDetailsManager` stores the user in memory and implements `UserDetailsService`.

This provides the user information that Spring Security needs during authentication.

The in-memory configuration is intentionally simple and is used only to demonstrate the basic security flow.

Later examples will explore authentication and `UserDetailsService` in greater detail.

---

## Password Encoding

Passwords should not be stored as plain text.

The example defines a `PasswordEncoder`:

```java
@Bean
PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

The user password is encoded when the user is created:

```java
.password(passwordEncoder.encode("password"))
```

Conceptually:

```text
Plain Password
      │
      ▼
PasswordEncoder
      │
      ▼
Encoded Password
      │
      ▼
Stored in UserDetails
```

The actual password encoding mechanisms will be explored further in the dedicated Password Encoding example.

---

## Complete Security Configuration

The complete configuration is:

```java
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize.requestMatchers("/public").permitAll().anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

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

The configuration brings together the basic pieces:

```text
SecurityFilterChain
        │
        ├── Request authorization
        │
        └── HTTP Basic authentication

UserDetailsService
        │
        └── In-memory user

PasswordEncoder
        │
        └── Password protection
```

---

## Why CSRF Is Disabled

Spring Security enables CSRF protection by default for servlet-based applications.

For this introductory example, it is disabled:

```java
http.csrf(AbstractHttpConfigurer::disable)
```

This allows the example to focus on the basic authentication and authorization flow without introducing another security mechanism at the same time.

CSRF protection is covered separately in the Module 01 CSRF example.

---

## Security Flow

The complete request flow can be summarized as:

```text
                         HTTP Request
                              │
                              ▼
                     SecurityFilterChain
                              │
                    ┌─────────┴─────────┐
                    │                   │
                /public             /private
                    │                   │
                    ▼                   ▼
                permitAll()       authenticated()
                    │                   │
                    │             ┌─────┴─────┐
                    │             │           │
                    │          No credentials  Valid credentials
                    │             │           │
                    ▼             ▼           ▼
                  200 OK      401 Unauthorized  200 OK
```

---

## Key Concepts

### `SecurityFilterChain`

Defines the security rules applied to incoming HTTP requests.

### Authentication

Verifies the identity of the user making a request.

### Authorization

Determines whether an authenticated request is allowed to access a resource.

### `UserDetailsService`

Provides user information to Spring Security during authentication.

### `InMemoryUserDetailsManager`

Provides an in-memory implementation of `UserDetailsService`.

### `PasswordEncoder`

Encodes passwords so they are not stored as plain text.

### HTTP Basic

Provides a simple username-and-password authentication mechanism.

### MockMvc

Allows Spring MVC endpoints and their security behavior to be tested without an external HTTP client.

---

## Dependencies

The example uses Spring Boot's web, security, testing, and Spring Security testing support.

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

You can test the public endpoint:

```bash
curl http://localhost:8080/public
```

The protected endpoint requires authentication:

```bash
curl -i http://localhost:8080/private
```

To access the protected endpoint with the configured user:

```bash
curl -i -u user:password http://localhost:8080/private
```

---

## Key Takeaways

1. Spring Security can secure a Spring Boot application through a `SecurityFilterChain`.
2. Authentication determines the identity of the requester.
3. Authorization determines whether the requester can access a resource.
4. `permitAll()` allows requests without authentication.
5. `authenticated()` requires the requester to be authenticated.
6. HTTP Basic provides a simple authentication mechanism.
7. `UserDetailsService` provides user information during authentication.
8. `InMemoryUserDetailsManager` can be used for simple in-memory users.
9. Passwords should be encoded using a `PasswordEncoder`.
10. MockMvc can verify Spring Security behavior without an external HTTP client.
11. CSRF is a separate security concern and is covered in a later example.

The central idea is:

```text
HTTP Request
     │
     ▼
SecurityFilterChain
     │
     ▼
Authentication
     │
     ▼
Authorization
     │
     ▼
Controller
```

---

## Next

**SecurityFilterChain**

The next example explores `SecurityFilterChain` in greater detail, including how Spring Security's HTTP security configuration determines which requests are permitted, authenticated, or denied.