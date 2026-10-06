# Spring Data Auditing

This example demonstrates how Spring Data JPA automatically tracks **who created or modified an entity** and **when those changes occurred**.

Spring Data auditing provides annotations such as `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, and `@LastModifiedBy`. An `AuditorAware` implementation supplies the current auditor, while `AuditingEntityListener` integrates the auditing lifecycle with JPA.

This example keeps the setup intentionally small and focuses on the core auditing mechanism without introducing a service layer.

## What This Example Demonstrates

- Enabling Spring Data JPA auditing
- Configuring `AuditorAware`
- Using `@CreatedDate`
- Using `@LastModifiedDate`
- Using `@CreatedBy`
- Using `@LastModifiedBy`
- Registering `AuditingEntityListener`
- Automatically populating audit fields when entities are persisted
- Updating modification audit fields when entities change
- Testing auditing with `@DataJpaTest`

---

## The Entity

The `Person` entity contains both regular domain fields and audit fields.

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int age;

    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant lastModifiedDate;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;

    // ...
}
```

The auditing annotations tell Spring Data which fields should be populated automatically.

| Annotation          | Purpose                                   |
|---------------------|-------------------------------------------|
| `@CreatedDate`      | Records when the entity was created       |
| `@LastModifiedDate` | Records when the entity was last modified |
| `@CreatedBy`        | Records who created the entity            |
| `@LastModifiedBy`   | Records who last modified the entity      |

---

## Enabling Auditing

Auditing is enabled using `@EnableJpaAuditing`.

```java
@Configuration
@EnableJpaAuditing
public class AuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("system");
    }
}
```

`@EnableJpaAuditing` activates Spring Data JPA's auditing support.

The `AuditorAware` bean tells Spring Data how to determine the current user or system responsible for a change.

For this example, the auditor is always:

```text
system
```

In a real application, `AuditorAware` would typically obtain the current authenticated user from the application's security context.

---

## The Auditing Entity Listener

The entity registers Spring Data's auditing listener:

```java
@EntityListeners(AuditingEntityListener.class)
```

The listener connects JPA entity lifecycle events with Spring Data auditing.

When an entity is persisted or updated, the listener allows Spring Data to populate the appropriate auditing fields.

The simplified creation flow is:

```text
Entity persisted
      ↓
JPA lifecycle event
      ↓
AuditingEntityListener
      ↓
Spring Data auditing
      ↓
@CreatedDate / @CreatedBy
```

For updates:

```text
Entity modified
      ↓
JPA lifecycle event
      ↓
AuditingEntityListener
      ↓
Spring Data auditing
      ↓
@LastModifiedDate / @LastModifiedBy
```

---

## Creation Auditing

When a new `Person` is saved:

```java
Person person = personRepository.save(new Person("John", 30));
```

Spring Data automatically populates the creation fields.

The test verifies this behavior:

```java
assertThat(person.getCreatedDate()).isNotNull();
assertThat(person.getCreatedBy()).isEqualTo("system");
```

The modification fields are also populated as part of the auditing lifecycle:

```java
assertThat(person.getLastModifiedDate()).isNotNull();
assertThat(person.getLastModifiedBy()).isEqualTo("system");
```

The application therefore does not need to manually assign these values.

---

## Modification Auditing

When an existing entity is modified and saved again, Spring Data updates the modification fields.

```java
Person person = personRepository.save(new Person("John", 30));

Instant createdDate = person.getCreatedDate();
Instant originalLastModifiedDate = person.getLastModifiedDate();

person.changeName("John Doe");

Person updatedPerson = personRepository.save(person);
```

The creation timestamp remains unchanged:

```java
assertThat(updatedPerson.getCreatedDate()).isEqualTo(createdDate);
```

The modification timestamp reflects the later save:

```java
assertThat(updatedPerson.getLastModifiedDate()).isAfterOrEqualTo(originalLastModifiedDate);
```

This gives the entity a basic history of when it was created and last modified.

---

## Created By and Last Modified By

Auditing can track not only timestamps but also the actor responsible for the change.

```java
@CreatedBy
private String createdBy;

@LastModifiedBy
private String lastModifiedBy;
```

The values come from `AuditorAware`.

In this example:

```java
@Bean
public AuditorAware<String> auditorAware() {
    return () -> Optional.of("system");
}
```

Therefore:

```text
createdBy      → system
lastModifiedBy → system
```

A real application could instead return the currently authenticated user's username or identifier.

---

## Repository

The repository does not require any auditing-specific methods.

```java
public interface PersonRepository extends CrudRepository<Person, Long> {
}
```

Auditing is transparent to the repository.

The repository continues to provide normal persistence operations while Spring Data handles the audit metadata automatically.

---

## Testing with `@DataJpaTest`

The example uses `@DataJpaTest` because auditing is being tested at the JPA persistence layer.

The auditing configuration is imported explicitly:

```java
@DataJpaTest
@Import(AuditingConfig.class)
class PersonRepositoryTest {
}
```

This is important because `@DataJpaTest` creates a focused JPA test context rather than loading the entire application.

The test therefore explicitly imports the configuration that enables auditing.

---

## Auditing vs Manual Metadata

Without Spring Data auditing, application code might need to explicitly populate fields:

```java
person.setCreatedDate(Instant.now());
person.setCreatedBy(currentUser);
```

With auditing enabled, these responsibilities move into the persistence infrastructure:

```text
Application
    ↓
Save Entity
    ↓
Spring Data Auditing
    ↓
Populate Audit Metadata
    ↓
Persist Entity
```

This keeps audit metadata handling consistent across entities.

---

## Typical Production `AuditorAware`

The example uses a fixed auditor to keep the demonstration deterministic:

```java
return () -> Optional.of("system");
```

In a secured application, `AuditorAware` would normally integrate with the application's authentication mechanism.

Conceptually:

```text
Authenticated User
        ↓
SecurityContext
        ↓
AuditorAware
        ↓
@CreatedBy / @LastModifiedBy
```

This allows audit metadata to identify the actual user responsible for a change.

---

## Running the Tests

From the module directory:

```bash
mvn test
```

Or from the project root:

```bash
mvn -pl 02-spring-data/auditing test
```

To run the complete project build:

```bash
mvn clean verify
```

---

## Key Takeaways

- Spring Data JPA provides auditing support for entity metadata.
- `@CreatedDate` tracks when an entity was created.
- `@LastModifiedDate` tracks when an entity was last modified.
- `@CreatedBy` identifies the creator.
- `@LastModifiedBy` identifies the last modifying auditor.
- `AuditorAware` supplies the current auditor.
- `AuditingEntityListener` connects JPA lifecycle events with Spring Data auditing.
- `@EnableJpaAuditing` activates auditing support.
- Auditing does not require a service layer.
- Auditing can be tested with `@DataJpaTest` by importing the auditing configuration.

The main idea is:

```text
JPA Entity
    ↓
AuditingEntityListener
    ↓
Spring Data Auditing
    ↓
AuditorAware + Audit Annotations
    ↓
Automatically populated metadata
```

---

## Next Example

The next example will be **Custom Repositories**.

It will demonstrate how to extend Spring Data's generated repository behavior with custom repository implementations when derived queries and standard repository methods are not sufficient.
