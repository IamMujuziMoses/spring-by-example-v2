# Spring Data Repositories

This example demonstrates the **Spring Data repository abstraction**.

It focuses on how Spring Data allows applications to define repository interfaces without implementing data-access logic manually, and how Spring Data creates the repository implementation at runtime.

## What This Example Demonstrates

- What Spring Data repositories are.
- The `Repository` interface.
- Defining repository operations.
- Spring Data repository implementations.
- Persisting entities through a repository.
- Finding entities through a repository.
- Deleting entities through a repository.
- The relationship between repositories, JPA, and the database.
- Testing repositories with `@DataJpaTest`.

---

## What Is a Spring Data Repository?

A Spring Data repository provides an abstraction over data-access operations.

Instead of writing a DAO implementation manually, an application can define a repository interface:

```java
public interface PersonRepository extends Repository<Person, Long> {

    Person save(Person person);

    Optional<Person> findById(Long id);

    void deleteById(Long id);
}
```

Spring Data detects the interface and creates the required implementation at runtime.

Conceptually:

```text
PersonRepository
       │
       ▼
Spring Data
       │
       ▼
Repository Proxy
       │
       ▼
JPA
       │
       ▼
Database
```

This allows application code to work with a repository abstraction instead of directly implementing database access logic.

---

## The Repository Interface

The example uses Spring Data's base `Repository` interface:

```java
public interface PersonRepository extends Repository<Person, Long> {
    // ...
}
```

The generic parameters represent:

```text
Repository<Entity, ID>
```

For this example:

```text
Repository<Person, Long>
```

means that the repository manages `Person` entities whose identifiers are of type `Long`.

Unlike `CrudRepository`, the base `Repository` interface does not expose CRUD methods automatically.

Instead, the required methods are explicitly declared.

This keeps the example focused on the fundamental repository abstraction.

---

## Repository Operations

The example declares three repository operations.

### Save

```java
Person person = personRepository.save(new Person("John"));
```

The repository passes the persistence operation to the underlying JPA infrastructure.

The persisted entity receives its generated identifier.

Conceptually:

```text
Person
  │
  ▼
save()
  │
  ▼
Spring Data Repository
  │
  ▼
JPA
  │
  ▼
Database
```

### Find

```java
Optional<Person> person = personRepository.findById(id);
```

The repository retrieves the entity associated with the specified identifier.

The result is represented using `Optional` so that the absence of an entity can be handled explicitly.

### Delete

```java
personRepository.deleteById(id);
```

The repository removes the entity associated with the specified identifier.

The application does not need to implement the underlying delete operation itself.

---

## Repository Implementation

One of the important concepts demonstrated by this example is that `PersonRepository` does not have a manually written implementation.

There is no class such as:

```java
public class PersonRepositoryImpl {
    // Manual database access
}
```

Instead, Spring Data creates the repository implementation and exposes it as a Spring bean.

The simplified flow is:

```text
Application
     │
     ▼
PersonRepository
     │
     ▼
Spring Data Repository Infrastructure
     │
     ▼
Generated Repository Implementation
     │
     ▼
JPA
     │
     ▼
Database
```

---

## Entity

The example uses a simple `Person` entity:

```java
@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    protected Person() {
    }

    public Person(String name) {
        this.name = name;
    }
}
```

The entity is intentionally simple.

The purpose of this example is to focus on the repository abstraction rather than detailed entity mapping.

Entity mapping is covered separately in this module.

---

## Repository and Entity Relationship

The repository is associated with the entity through its generic parameters:

```java
Repository<Person, Long>
```

This establishes:

```text
PersonRepository
      │
      ├── Entity: Person
      │
      └── ID: Long
```

Spring Data uses this information to create the repository infrastructure for the `Person` entity.

---

## Persistence Flow

When an entity is saved:

```text
Person
  │
  ▼
PersonRepository.save()
  │
  ▼
Spring Data
  │
  ▼
JPA
  │
  ▼
EntityManager
  │
  ▼
H2 Database
```

When an entity is retrieved:

```text
PersonRepository.findById()
  │
  ▼
Spring Data
  │
  ▼
JPA
  │
  ▼
EntityManager
  │
  ▼
H2 Database
```

The repository provides the abstraction between application code and the persistence infrastructure.

---

## Repository Abstraction

The main idea of this example can be summarized as:

```text
Application Code
       │
       ▼
Repository Interface
       │
       ▼
Spring Data
       │
       ▼
JPA
       │
       ▼
Database
```

The application depends on the repository abstraction rather than directly implementing database operations.

This separation provides a consistent programming model for persistence.

---

## `Repository` vs `CrudRepository`

This example intentionally uses:

```java
Repository<Person, Long>
```

rather than:

```java
CrudRepository<Person, Long>
```

The base `Repository` abstraction allows the example to explicitly declare only the operations it needs.

The next example will focus specifically on `CrudRepository` and its standard CRUD operations.

---

## Testing with H2

The example uses H2 as an embedded test database.

The tests therefore follow this simplified structure:

```text
@DataJpaTest
     │
     ▼
Spring Test Context
     │
     ▼
JPA
     │
     ▼
H2 Database
```

This allows repository behavior to be tested against a real persistence layer without requiring a separately installed database.

---

## Running the Tests

Run the tests from the module:

```bash
mvn test
```

Or from the repository root:

```bash
mvn -pl 02-spring-data/spring-data-repositories test
```

---

## Key Takeaways

- Spring Data provides repository abstractions for database access.
- Repository interfaces do not require manually implemented data-access logic.
- `Repository` is the base Spring Data repository abstraction.
- Repository operations can be explicitly declared.
- Spring Data creates the repository implementation at runtime.
- Repositories provide an abstraction between application code and persistence infrastructure.
- `@DataJpaTest` provides focused testing support for JPA repositories.
- H2 can be used as an embedded database for repository tests.
- `CrudRepository` builds on the repository abstraction and provides standard CRUD operations.

---

## Next Example

The next example will explore **`CrudRepository`** and its standard CRUD operations.