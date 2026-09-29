# JWT

This example demonstrates how to configure **Spring Security as an OAuth2 Resource Server** to authenticate requests using JSON Web Tokens (JWT).

The example uses an RSA key pair generated at application startup so the entire JWT authentication flow can run locally without requiring an external authorization server.

## What This Example Demonstrates

- Configuring Spring Security as a JWT Resource Server
- Bearer token authentication
- JWT decoding and validation
- RSA public/private key pairs
- `JwtEncoder`
- `JwtDecoder`
- `BearerTokenAuthenticationFilter`
- `JwtAuthenticationToken`
- Accessing authenticated users through `SecurityContext`
- Testing JWT authentication with MockMvc
- Sending JWTs through the `Authorization` header

---

## What Is JWT?

**JSON Web Token (JWT)** is a compact token format commonly used to represent claims between parties.

A JWT can contain information such as:

- Subject
- Issued-at time
- Expiration time
- Scopes
- Roles
- Other application-specific claims

A simplified JWT structure is:

```text
Header.Payload.Signature
```

For example:

```text
eyJhbGciOiJSUzI1NiJ9
.
eyJzdWIiOiJ1c2VyIn0
.
signature
```

The actual values are encoded and signed according to the JWT specification.

---

## JWT Resource Server

In this example, the application acts as an **OAuth2 Resource Server**.

It receives JWT bearer tokens from clients and validates them before allowing access to protected resources.

```text
Client
   │
   │ Authorization: Bearer <JWT>
   ▼
Spring Security
   │
   ▼
JWT Resource Server
   │
   ▼
JwtDecoder
   │
   ├── Validate signature
   ├── Validate token
   └── Extract claims
   │
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

## Configuring JWT Authentication

JWT authentication is enabled through:

```java
http.oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
```

This tells Spring Security to configure the application as an OAuth2 Resource Server using JWT bearer-token authentication.

The complete security configuration also protects all endpoints except `/` and `/public`:

```java
http.authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/", "/public").permitAll()
        .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
        .jwt(withDefaults()));
```

---

## RSA Key Pair

The example generates an RSA key pair at startup:

```text
RSA Key Pair
    │
    ├── Private Key
    │       ↓
    │    Signs JWT
    │
    └── Public Key
            ↓
        Validates JWT
```

The private key is used by `JwtEncoder` to sign tokens.

The public key is provided to `JwtDecoder` so that incoming tokens can be validated.

```java
@Bean
KeyPair keyPair() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);

    return generator.generateKeyPair();
}
```

This keeps the example self-contained.

In a production application, the resource server would normally obtain trusted public keys from the authorization server or a configured JWK endpoint instead of generating its own keys.

---

## JwtEncoder

The `JwtEncoder` creates signed JWTs.

The example uses the generated RSA private key:

```java
@Bean
JwtEncoder jwtEncoder(KeyPair keyPair) {
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

    RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();

    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
}
```

The encoder is mainly used by the tests to create valid JWTs.

---

## JwtDecoder

The `JwtDecoder` uses the RSA public key to validate incoming JWTs:

```java
@Bean
JwtDecoder jwtDecoder(KeyPair keyPair) {
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

    return NimbusJwtDecoder.withPublicKey(publicKey).build();
}
```

When a request contains a bearer token, Spring Security passes the token to the decoder.

If the token cannot be successfully validated, authentication fails.

---

## Sending a JWT

A client sends the JWT through the `Authorization` header:

```http
GET /user
Authorization: Bearer <JWT>
```

The `BearerTokenAuthenticationFilter` extracts the token from the request.

Spring Security then validates the token and creates an authenticated `Authentication` object.

---

## BearerTokenAuthenticationFilter

The `BearerTokenAuthenticationFilter` is responsible for extracting the bearer token from the request.

For example:

```http
Authorization: Bearer eyJhbGciOi...
```

The filter extracts:

```text
eyJhbGciOi...
```

and passes it into Spring Security's authentication process.

---

## JWT Authentication Flow

The complete simplified flow is:

```text
HTTP Request
     │
     │ Authorization: Bearer <JWT>
     ▼
BearerTokenAuthenticationFilter
     │
     │ Extract token
     ▼
JwtDecoder
     │
     ├── Signature validation
     ├── Token validation
     └── Claims extraction
     │
     ▼
JwtAuthenticationToken
     │
     ▼
SecurityContext
     │
     ▼
JwtController
```

This is the main flow this example is intended to demonstrate.

---

## Accessing the Authenticated User

The controller receives the authenticated user through Spring Security's `Authentication` interface:

```java
@GetMapping("/user")
public String user(Authentication authentication) {
    return "Authenticated as: " + authentication.getName();
}
```

For the test JWT:

```text
Authenticated as: user
```

The JWT subject becomes the authenticated user's name.

---

## Creating a JWT

The tests create a JWT using `JwtEncoder`:

```java
JwtClaimsSet claims = JwtClaimsSet.builder().subject(subject).issuedAt(now).expiresAt(now.plusSeconds(300))
        .claim("scope", "read").build();

return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
```

The token contains:

- `sub` — the authenticated subject
- `iat` — issued-at timestamp
- `exp` — expiration timestamp
- `scope` — an example scope claim

---

## Public and Protected Endpoints

The example exposes three endpoints:

| Endpoint  | Authentication |
|-----------|----------------|
| `/`       | Public         |
| `/public` | Public         |
| `/user`   | JWT required   |

The `/user` endpoint demonstrates accessing the authenticated principal after successful JWT validation.

---

## JWT vs OAuth2 Login

The previous OAuth2 example demonstrated **OAuth2 Login**.

That flow involves a browser and an external authorization provider:

```text
Browser
   ↓
Application
   ↓
Authorization Server
   ↓
User Authentication
   ↓
Authorization Code
   ↓
Application
   ↓
Authenticated User
```

This example instead demonstrates **JWT bearer-token authentication**:

```text
Client
   ↓
Bearer JWT
   ↓
Resource Server
   ↓
JWT Validation
   ↓
Authenticated User
```

OAuth2 and JWT should not be treated as interchangeable concepts.

OAuth2 is an authorization framework, while JWT is a token format.

JWTs can be used within OAuth2-based systems, including OAuth2 resource-server scenarios.

---

## Why No External Authorization Server?

This example intentionally does not depend on GitHub, Auth0, Keycloak, or another external authorization server.

The RSA key pair is generated locally so that the example can demonstrate:

- JWT creation
- JWT signing
- JWT decoding
- JWT validation
- Bearer authentication

without requiring additional infrastructure.

This makes the example easier to run and test.

---

## Running the Example

From the module directory:

```bash
mvn spring-boot:run
```

The application starts on the default Spring Boot port:

```text
http://localhost:8080
```

Public endpoint:

```text
http://localhost:8080/public
```

Protected endpoint:

```text
http://localhost:8080/user
```

The protected endpoint requires a valid JWT.

---

## Running Tests

Run:

```bash
mvn test
```

The tests cover:

- Public endpoint access
- Protected endpoint without authentication
- Valid JWT authentication
- Invalid JWT authentication
- Authenticated username retrieval

The tests generate their own signed JWTs and do not require an external authentication service.

---

## What This Example Does Not Cover

This example intentionally focuses on the fundamentals of JWT resource-server authentication.

It does not cover:

- JWT role-based authorization
- Scope-based authorization
- Custom JWT claims
- Custom JWT authentication converters
- External JWK endpoints
- OAuth2 Authorization Server configuration
- Refresh tokens
- Token issuance APIs
- Key rotation
- OpenID Connect

These concepts can be explored through additional focused examples where appropriate.

---

## Key Takeaways

- Spring Security can act as an OAuth2 Resource Server.
- JWT bearer tokens can be used to authenticate API requests.
- `BearerTokenAuthenticationFilter` extracts bearer tokens from requests.
- `JwtDecoder` validates incoming JWTs.
- RSA private keys can sign JWTs while corresponding public keys validate them.
- Successful JWT validation creates an authenticated `Authentication`.
- The authentication is stored in the `SecurityContext`.
- JWT authentication does not require browser redirects.
- JWT is a token format, while OAuth2 is an authorization framework.
- JWT authentication can be tested without contacting an external authentication provider.

---

## Next Example

The next example will explore **Spring Security Testing**, including how Spring Security's testing support can be used to test authenticated users, roles, authorities, CSRF protection, and other security behavior.