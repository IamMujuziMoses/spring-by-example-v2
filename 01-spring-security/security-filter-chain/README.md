# SecurityFilterChain

Spring Security uses `SecurityFilterChain` to define how incoming HTTP requests are secured.

This example focuses on configuring a `SecurityFilterChain` with `HttpSecurity` and defining authorization rules for public, authenticated, and role-based endpoints.

The example demonstrates `authorizeHttpRequests`, `requestMatchers`, `permitAll`, `authenticated`, `hasRole`, `anyRequest`, and the difference between `securityMatcher` and `requestMatchers`.

## Learning Objectives

By completing this example, you will understand:

- What a `SecurityFilterChain` is
- How `HttpSecurity` configures web security
- How authorization rules are defined
- How `requestMatchers` select requests for authorization rules
- How `permitAll()` allows unauthenticated access
- How `authenticated()` requires authentication
- How `hasRole()` restricts access based on roles
- How `anyRequest()` handles remaining requests
- Why authorization rule ordering matters
- The difference between `401 Unauthorized` and `403 Forbidden`
- The difference between `securityMatcher` and `requestMatchers`
- How to test `SecurityFilterChain` behavior with MockMvc

---

## What Is SecurityFilterChain?

A `SecurityFilterChain` defines the security rules applied to incoming HTTP requests.

It is configured as a Spring bean:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // Configure security rules.

    return http.build();
}
```

Spring Security uses the resulting filter chain to process requests before they reach the application controllers.

Conceptually:

```text
HTTP Request
     │
     ▼
SecurityFilterChain
     │
     ▼
Security Rules
     │
     ├── Public request
     │
     ├── Authenticated request
     │
     ├── Role-based request
     │
     └── Denied request
     │
     ▼
Controller
```

---

## SecurityFilterChain Flow

This example defines four types of access:

```text
                         HTTP Request
                              │
                              ▼
                     SecurityFilterChain
                              │
                              ▼
                    Authorization Rules
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
       /public              /user              /admin
          │                   │                   │
          ▼                   ▼                   ▼
      permitAll()        hasRole("USER")    hasRole("ADMIN")
          │                   │                   │
          ▼                   ▼                   ▼
       200 OK              Allowed/Denied     Allowed/Denied
```

A separate rule handles authenticated requests:

```text
/authenticated
      │
      ▼
authenticated()
      │
 ┌────┴────┐
 │         │
 ▼         ▼
No user   Authenticated user
 │         │
 ▼         ▼
401       200
```

---

## Creating a SecurityFilterChain

The central configuration is:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/public").permitAll()
                    .requestMatchers("/admin").hasRole("ADMIN")
                    .requestMatchers("/user").hasRole("USER")
                    .requestMatchers("/authenticated").authenticated()
                    .anyRequest().denyAll())
            .httpBasic(withDefaults());

    return http.build();
}
```

The configuration defines:

1. CSRF is disabled for this focused example.
2. `/public` is accessible without authentication.
3. `/admin` requires the `ADMIN` role.
4. `/user` requires the `USER` role.
5. `/authenticated` requires authentication.
6. Any other request is denied.
7. HTTP Basic authentication is enabled.

---

## HttpSecurity

`HttpSecurity` provides the API used to configure web security.

For example:

```java
http.authorizeHttpRequests(...).httpBasic(withDefaults());
```

The configuration is eventually converted into a `SecurityFilterChain`:

```java
return http.build();
```

Conceptually:

```text
HttpSecurity
     │
     ▼
Security Configuration
     │
     ▼
http.build()
     │
     ▼
SecurityFilterChain
```

---

## authorizeHttpRequests

`authorizeHttpRequests` defines authorization rules for incoming requests.

```java
http.authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/public").permitAll()
        .requestMatchers("/admin").hasRole("ADMIN")
        .requestMatchers("/user").hasRole("USER")
        .requestMatchers("/authenticated").authenticated()
        .anyRequest().denyAll());
```

Each rule specifies:

```text
Which request?
      │
      ▼
What access is required?
```

For example:

```java
.requestMatchers("/admin").hasRole("ADMIN")
```

means:

```text
/admin
  │
  ▼
Requires ADMIN role
```

---

## requestMatchers

`requestMatchers` selects the requests to which an authorization rule applies.

For example:

```java
.requestMatchers("/public").permitAll()
```

matches requests to `/public`.

Other examples:

```java
.requestMatchers("/admin").hasRole("ADMIN")
.requestMatchers("/user").hasRole("USER")
.requestMatchers("/authenticated").authenticated()
```

The matcher determines **which request** the authorization rule applies to.

The authorization method determines **what access is required**.

```text
requestMatchers
      │
      ▼
Select the request
      │
      ▼
Authorization rule
      │
      ▼
Determine access
```

---

## permitAll

`permitAll()` allows access without authentication.

```java
.requestMatchers("/public").permitAll()
```

Therefore:

```text
GET /public
      │
      ▼
permitAll()
      │
      ▼
200 OK
```

The endpoint can be accessed without providing credentials.

---

## authenticated

`authenticated()` requires the request to come from an authenticated user.

```java
.requestMatchers("/authenticated").authenticated()
```

An unauthenticated request:

```text
GET /authenticated
      │
      ▼
No credentials
      │
      ▼
401 Unauthorized
```

A request with valid credentials:

```text
GET /authenticated
      │
      ▼
Valid credentials
      │
      ▼
200 OK
```

---

## hasRole

`hasRole()` restricts access to users with a particular role.

The example uses:

```java
.requestMatchers("/user").hasRole("USER")
.requestMatchers("/admin").hasRole("ADMIN")
```

The application defines two users:

```text
user
  └── USER

admin
  ├── USER
  └── ADMIN
```

Therefore:

| Endpoint | `user`  | `admin` |
|----------|---------|---------|
| `/user`  | Allowed | Allowed |
| `/admin` | Denied  | Allowed |

---

## anyRequest

`anyRequest()` matches requests that have not already matched one of the preceding rules.

The example uses:

```java
.anyRequest().denyAll()
```

This means that requests without an explicit authorization rule are denied.

For example:

```text
/public          → permitAll()
/user            → hasRole("USER")
/admin           → hasRole("ADMIN")
/authenticated   → authenticated()
/unknown         → denyAll()
```

This makes the security configuration explicit.

---

## Rule Ordering

Authorization rules are evaluated in order.

For example:

```java
.authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/public").permitAll()
        .requestMatchers("/admin").hasRole("ADMIN")
        .requestMatchers("/user").hasRole("USER")
        .anyRequest().denyAll())
```

The specific rules are defined before the catch-all rule:

```java
.anyRequest().denyAll()
```

The catch-all rule should therefore appear after the rules that are intended to match specific requests.

Conceptually:

```text
Specific rules
      │
      ├── /public
      ├── /admin
      ├── /user
      └── /authenticated
              │
              ▼
        Catch-all rule
              │
              ▼
        anyRequest()
```

---

## 401 Unauthorized vs 403 Forbidden

This example demonstrates an important distinction.

### 401 Unauthorized

A request that requires authentication but does not provide valid credentials receives `401 Unauthorized`.

For example:

```text
GET /authenticated
      │
      ▼
No credentials
      │
      ▼
401 Unauthorized
```

### 403 Forbidden

A request can be authenticated but still lack permission to access the resource.

For example:

```text
GET /admin
Authorization: user/password
      │
      ▼
Authenticated
      │
      ▼
User does not have ADMIN role
      │
      ▼
403 Forbidden
```

The same distinction applies to the catch-all rule:

```text
GET /unknown
      │
      ├── No credentials
      │      │
      │      ▼
      │   401 Unauthorized
      │
      └── Valid credentials
             │
             ▼
        anyRequest().denyAll()
             │
             ▼
         403 Forbidden
```

---

## securityMatcher vs requestMatchers

`securityMatcher` and `requestMatchers` serve different purposes.

### securityMatcher

`securityMatcher` determines which requests the entire `SecurityFilterChain` applies to.

For example:

```java
http.securityMatcher("/api/**");
```

This means the filter chain is selected for requests matching `/api/**`.

### requestMatchers

`requestMatchers` selects requests for individual authorization rules within the security configuration.

For example:

```java
http.authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/admin").hasRole("ADMIN")
        .requestMatchers("/user").hasRole("USER"));
```

The distinction can be summarized as:

```text
securityMatcher
      │
      ▼
Which requests use this SecurityFilterChain?

requestMatchers
      │
      ▼
What authorization rule applies to this request?
```

This example focuses on a single `SecurityFilterChain`. Multiple filter chains can be explored separately when learning more advanced Spring Security configurations.

---

## HTTP Basic Authentication

The example enables HTTP Basic authentication:

```java
.httpBasic(withDefaults())
```

This allows the tests to authenticate users using a username and password.

For example:

```java
.with(httpBasic("user", "password"))
```

HTTP Basic is used here because it provides a simple authentication mechanism for demonstrating authorization rules.

More advanced authentication mechanisms are covered in later examples.

---

## In-Memory Users

The example defines two users:

```java
@Bean
UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    UserDetails user = User.withUsername("user").password(passwordEncoder.encode("password")).roles("USER").build();
    UserDetails admin = User.withUsername("admin").password(passwordEncoder.encode("password")).roles("USER", "ADMIN").build();

    return new InMemoryUserDetailsManager(user, admin);
}
```

The users are:

| Username | Password   | Roles           |
|----------|------------|-----------------|
| `user`   | `password` | `USER`          |
| `admin`  | `password` | `USER`, `ADMIN` |

The users exist only to make the authorization rules observable.

Authentication and `UserDetailsService` are explored in greater detail in their dedicated Module 01 examples.

---

## Password Encoding

The example uses Spring Security's delegating password encoder:

```java
@Bean
PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

Passwords are encoded when the users are created:

```java
.password(passwordEncoder.encode("password"))
```

Password encoding is covered in greater detail in the dedicated Password Encoding example.

---

## Complete Security Configuration

The complete configuration is:

```java
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/public").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/user").hasRole("USER")
                        .requestMatchers("/authenticated").authenticated()
                        .anyRequest().denyAll())
                .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("user").password(passwordEncoder.encode("password")).roles("USER").build();
        UserDetails admin = User.withUsername("admin").password(passwordEncoder.encode("password")).roles("USER", "ADMIN").build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
```

The important part of this configuration is the `SecurityFilterChain`:

```text
SecurityFilterChain
       │
       ▼
authorizeHttpRequests
       │
       ├── /public → permitAll
       ├── /admin → ADMIN
       ├── /user → USER
       ├── /authenticated → authenticated
       └── everything else → denyAll
```

---

## Authorization Test Matrix

| Endpoint         | Credentials      | Result             |
|------------------|------------------|--------------------|
| `/public`        | None             | `200 OK`           |
| `/user`          | `user/password`  | `200 OK`           |
| `/admin`         | `user/password`  | `403 Forbidden`    |
| `/admin`         | `admin/password` | `200 OK`           |
| `/authenticated` | None             | `401 Unauthorized` |
| `/authenticated` | `user/password`  | `200 OK`           |
| `/unknown`       | None             | `401 Unauthorized` |
| `/unknown`       | `user/password`  | `403 Forbidden`    |

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

Test the authenticated endpoint without credentials:

```bash
curl -i http://localhost:8080/authenticated
```

Test the user endpoint:

```bash
curl -i -u user:password http://localhost:8080/user
```

Test the admin endpoint with the regular user:

```bash
curl -i -u user:password http://localhost:8080/admin
```

Test the admin endpoint with the admin user:

```bash
curl -i -u admin:password http://localhost:8080/admin
```

---

## Key Concepts

### `SecurityFilterChain`

Defines the security processing and authorization rules applied to requests.

### `HttpSecurity`

Provides the API used to configure web security.

### `authorizeHttpRequests`

Defines authorization rules for HTTP requests.

### `requestMatchers`

Selects requests to which an authorization rule applies.

### `permitAll()`

Allows access without authentication.

### `authenticated()`

Requires the requester to be authenticated.

### `hasRole()`

Requires the authenticated user to have a specific role.

### `anyRequest()`

Matches requests that have not matched an earlier authorization rule.

### `denyAll()`

Denies access to matching requests.

### `securityMatcher()`

Determines which requests a particular `SecurityFilterChain` applies to.

---

## Key Takeaways

1. `SecurityFilterChain` is the central mechanism for configuring web security.
2. `HttpSecurity` provides the configuration API used to build the filter chain.
3. `authorizeHttpRequests` defines authorization rules.
4. `requestMatchers` selects requests for individual authorization rules.
5. `permitAll()` allows unauthenticated access.
6. `authenticated()` requires authentication.
7. `hasRole()` restricts access based on roles.
8. `anyRequest()` provides a catch-all rule.
9. Authorization rules should be ordered carefully.
10. An unauthenticated request can receive `401 Unauthorized`.
11. An authenticated request without sufficient permission can receive `403 Forbidden`.
12. `securityMatcher` selects requests for the entire filter chain, while `requestMatchers` selects requests for individual authorization rules.
13. MockMvc can verify the behavior of the configured security rules.

The central idea is:

```text
HTTP Request
     │
     ▼
SecurityFilterChain
     │
     ▼
Authorization Rules
     │
     ├── permitAll()
     ├── authenticated()
     ├── hasRole()
     └── denyAll()
     │
     ▼
Access Decision
     │
     ▼
Controller or HTTP Error
```

---

## Next

**Authentication**

The next example explores how Spring Security authenticates users and how authentication information is established during a request.
