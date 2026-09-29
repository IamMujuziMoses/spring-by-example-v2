# OAuth2 Login

This example demonstrates how to configure **OAuth2 Login** with Spring Security using GitHub as an OAuth2 provider.

The example focuses on the OAuth2 Authorization Code flow and shows how Spring Security authenticates a user through an external OAuth2 provider.

## What This Example Demonstrates

- Configuring OAuth2 Login with Spring Security
- Registering an OAuth2 client
- Using GitHub as an OAuth2 provider
- Starting the OAuth2 authorization flow
- Handling the OAuth2 callback
- Creating an authenticated security context
- Accessing the authenticated OAuth2 principal
- Testing OAuth2 authentication with `oauth2Login()`
- Keeping OAuth2 credentials outside source control

---

## What Is OAuth2?

**OAuth 2.0** is an authorization framework that allows an application to obtain limited access to resources on behalf of a user without requiring the application to handle the user's password.

Spring Security supports several OAuth2 use cases.

This example focuses specifically on **OAuth2 Login**.

OAuth2 Login allows users to authenticate through an external provider such as GitHub.

---

## OAuth2 Login Flow

The simplified flow used by this example is:

```text
Browser
   │
   │ GET /oauth2/authorization/github
   ▼
Spring Security
   │
   │ Redirect
   ▼
GitHub
   │
   │ User authenticates
   ▼
Authorization Code
   │
   ▼
Spring Security
   │
   │ Exchanges code for token
   ▼
Access Token
   │
   ▼
OAuth2 User Information
   │
   ▼
Authenticated Principal
   │
   ▼
SecurityContext
```

The application does not implement this entire flow manually.

Spring Security provides the filters and OAuth2 client components responsible for handling the authorization flow.

---

## Configuring OAuth2 Login

OAuth2 Login is enabled through the security configuration:

```java
http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/", "/public").permitAll()
                .anyRequest().authenticated()).oauth2Login(withDefaults());
```

The `oauth2Login()` configuration enables Spring Security's OAuth2 Login support.

Protected endpoints require authentication, while `/` and `/public` remain accessible without authentication.

---

## OAuth2 Client Registration

The GitHub client is configured in `application.yml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope:
              - read:user
              - user:email
```

The client ID and client secret are read from environment variables rather than being committed to the repository.

This keeps OAuth2 credentials outside the source code.

---

## Starting the Login Flow

Once the application is running, the OAuth2 authorization flow can be started through:

```text
/oauth2/authorization/github
```

Spring Security uses the configured `github` registration to determine where to redirect the user.

The user is then redirected to GitHub to authenticate and authorize the application.

---

## OAuth2 Callback

After successful authorization, GitHub redirects the user back to the application.

Spring Security processes the authorization response and exchanges the authorization code for an access token.

Spring Security then uses the token to obtain the user's information from the configured OAuth2 provider.

The resulting user is represented as an authenticated principal.

---

## Accessing the Authenticated User

The controller exposes a protected endpoint:

```java
@GetMapping("/user")
public String user(OAuth2AuthenticationToken authentication) {
    return "Authenticated as: " + authentication.getName();
}
```

The `OAuth2AuthenticationToken` provides access to information about the authenticated OAuth2 user.

For example:

```text
Authenticated as: <provider-specific-user-name>
```

The exact value depends on the identity returned by the OAuth2 provider.

---

## Public and Protected Endpoints

This example exposes three endpoints:

| Endpoint  | Authentication |
|-----------|----------------|
| `/`       | Public         |
| `/public` | Public         |
| `/user`   | Required       |

Requests to `/user` require an authenticated user.

If a user is not authenticated, Spring Security starts the OAuth2 login process.

---

## Testing OAuth2 Login

The tests use Spring Security Test's `oauth2Login()` request post processor.

```java
mockMvc.perform(get("/user").with(oauth2Login())).andExpect(status().isOk());
```

This creates a mocked authenticated OAuth2 user.

The tests therefore do not need to contact GitHub or perform a real OAuth2 login.

This keeps the tests:

- Fast
- Deterministic
- Independent of external services
- Safe to run in CI

---

## OAuth2 Credentials

The example expects the following environment variables when running the real OAuth2 login flow:

```text
GITHUB_CLIENT_ID
GITHUB_CLIENT_SECRET
```

For example:

```bash
export GITHUB_CLIENT_ID=your-client-id
export GITHUB_CLIENT_SECRET=your-client-secret
```

Never commit real OAuth2 credentials to the repository.

---

## OAuth2 Login vs JWT

OAuth2 and JWT are related but represent different concepts.

**OAuth2** is an authorization framework and defines flows for obtaining authorization.

**JWT** is a token format that can be used to represent claims.

This example focuses on:

```text
OAuth2 Login
    ↓
Authorization Code Flow
    ↓
Authenticated User
```

A later example will explore JWT-based authentication separately.

---

## Running the Example

From the module directory:

```bash
mvn spring-boot:run
```

Then open:

```text
http://localhost:8080/
```

To start OAuth2 login:

```text
http://localhost:8080/oauth2/authorization/github
```

Make sure the GitHub OAuth2 client credentials are configured before attempting a real login.

---

## Running Tests

Run the module tests with:

```bash
mvn test
```

The OAuth2 authentication tests use mocked authentication and do not require GitHub credentials.

---

## Key Concepts

### OAuth2 Client

The application acts as an OAuth2 client when it communicates with an external OAuth2 provider.

### Authorization Server

GitHub acts as the authorization server in this example, handling user authorization and issuing authorization codes and access tokens.

### Authorization Code

The authorization code is returned to the application after the user successfully authorizes the application.

### Access Token

Spring Security exchanges the authorization code for an access token, which can then be used to obtain user information from the provider.

### OAuth2 Principal

The authenticated user's identity is represented by an OAuth2 principal and made available through Spring Security's authentication system.

### SecurityContext

After successful authentication, Spring Security stores the authentication information in the `SecurityContext`.

---

## What This Example Does Not Cover

This example intentionally focuses on OAuth2 Login.

It does not cover:

- Building an OAuth2 Authorization Server
- Acting as an OAuth2 Resource Server
- JWT bearer-token authentication
- Custom OAuth2 providers
- Custom authorization requests
- Refresh token handling
- OpenID Connect in depth
- Advanced OAuth2 client customization

These topics can be explored in separate examples where appropriate.

---

## Key Takeaways

- Spring Security provides OAuth2 Login support out of the box.
- `oauth2Login()` enables OAuth2-based authentication.
- OAuth2 client registrations define how the application communicates with an OAuth2 provider.
- The authorization code flow allows the application to authenticate users without handling their provider passwords.
- OAuth2 credentials should remain outside source control.
- Spring Security manages the OAuth2 authentication flow and places the resulting authentication in the `SecurityContext`.
- OAuth2 login can be tested without contacting the real provider by using `oauth2Login()`.

---

## Next Example

The next example will explore **JWT** in Spring Security, focusing on bearer-token authentication and how Spring Security processes JWT-based requests.
