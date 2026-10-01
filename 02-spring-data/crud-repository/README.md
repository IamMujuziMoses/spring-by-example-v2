# CrudRepository

This example demonstrates Spring Data's **`CrudRepository`** abstraction and the standard CRUD operations it provides out of the box.

Unlike the previous example, where the repository extended the base `Repository` interface and explicitly declared each operation, `CrudRepository` provides a predefined API for common persistence operations.

The goal is to understand how `CrudRepository` reduces the amount of repository code an application needs to write while providing a consistent interface for working with persisted entities.

## What This Example Demonstrates

- What `CrudRepository` is.
- How `CrudRepository` extends the Spring Data repository abstraction.
- Defining a repository using `CrudRepository`.
- Saving entities.
- Finding entities by ID.
- Finding all entities.
- Checking whether an entity exists.
- Counting entities.
- Deleting entities by ID.
- Deleting entities.
- Testing CRUD operations with `@DataJpaTest`.
- Using H2 as an embedded test database.

---

## What Is `CrudRepository`?

`CrudRepository` is a Spring Data repository interface that provides standard CRUD operations for an entity.

CRUD stands for:

| Operation | Meaning                     |
|-----------|-----------------------------|
| Create    | Persist a new entity        |
| Read      | Retrieve persisted entities |
| Update    | Modify an existing entity   |
| Delete    | Remove an entity            |

Instead of declaring these operations manually, a repository can extend `CrudRepository`:

```java
public interface PersonRepository extends CrudRepository<Person, Long> {
}
```

Spring Data then provides the standard CRUD methods through the repository implementation it creates at runtime.

Conceptually:

```text
PersonRepository
       │
       ▼
CrudRepository<Person, Long>
       │
       ▼
Spring Data Repository Infrastructure
       │
       ▼
JPA
       │
       ▼
Database
```

---

## `CrudRepository` and `Repository`

The previous example introduced the base `Repository` interface:

```java
public interface PersonRepository extends Repository<Person, Long> {

    Person save(Person person);

    Optional<Person> findById(Long id);

    void deleteById(Long id);
}
```

The required methods had to be explicitly declared.

With `CrudRepository`, those standard operations are already defined:

```java
public interface PersonRepository extends CrudRepository<Person, Long> {
}
```

This means the repository interface itself can be empty.

The difference can be summarized as:

```text
Repository
    │
    └── Defines the repository abstraction
         │
         └── Operations are explicitly declared

CrudRepository
    │
    └── Extends Repository
         │
         └── Provides standard CRUD operations
```

---

## Repository Definition

The repository in this example is:

```java
public interface PersonRepository extends CrudRepository<Person, Long> {
}
```

The generic parameters specify:

```text
CrudRepository<Entity, ID>
```

For this example:

```text
CrudRepository<Person, Long>
```

means:

- The repository manages `Person` entities.
- The identifier type is `Long`.

Spring Data uses this information to create the appropriate repository implementation.

---

## Standard CRUD Operations

`CrudRepository` provides a collection of standard persistence operations.

Some of the most important ones are:

| Method         | Purpose                         |
|----------------|---------------------------------|
| `save()`       | Saves an entity                 |
| `saveAll()`    | Saves multiple entities         |
| `findById()`   | Finds an entity by ID           |
| `findAll()`    | Retrieves all entities          |
| `existsById()` | Checks whether an entity exists |
| `count()`      | Counts entities                 |
| `deleteById()` | Deletes an entity by ID         |
| `delete()`     | Deletes an entity               |
| `deleteAll()`  | Deletes all entities            |

The application does not need to implement these methods manually.

---

## Saving an Entity

The `save()` method can persist a new entity:

```java
Person person = personRepository.save(new Person("John"));
```

After persistence, the generated ID is available on the entity.

The test verifies this behavior:

```java
@Test
void save_shouldPersistPerson() {
    Person person = personRepository.save(new Person("John"));

    assertThat(person.getId()).isNotNull();
    assertThat(person.getName()).isEqualTo("John");
}
```

The simplified flow is:

```text
Person
   │
   ▼
save()
   │
   ▼
CrudRepository
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

---

## Finding an Entity by ID

`findById()` retrieves an entity using its identifier:

```java
Optional<Person> result = personRepository.findById(savedPerson.getId());
```

The result is an `Optional<Person>`.

This allows the application to explicitly handle the case where an entity does not exist.

The test verifies that the persisted entity can be retrieved:

```java
@Test
void findById_shouldReturnPerson() {
    Person savedPerson = personRepository.save(new Person("John"));

    Optional<Person> result = personRepository.findById(savedPerson.getId());

    assertThat(result).isPresent().get().extracting(Person::getName).isEqualTo("John");
}
```

---

## Finding All Entities

`findAll()` retrieves all entities managed by the repository.

Unlike some collection-oriented APIs, `CrudRepository.findAll()` returns an `Iterable<T>`:

```java
Iterable<Person> people = personRepository.findAll();
```

For example:

```java
@Test
void findAll_shouldReturnAllPeople() {
    personRepository.save(new Person("John"));
    personRepository.save(new Person("Jane"));

    Iterable<Person> people = personRepository.findAll();

    List<Person> result = StreamSupport.stream(people.spliterator(), false).toList();

    assertThat(result).extracting(Person::getName).containsExactlyInAnyOrder("John", "Jane");
}
```

The test converts the `Iterable<Person>` into a `List<Person>` before using AssertJ's collection assertions.

The important point is that the repository contract intentionally exposes the result as an `Iterable`.

Conceptually:

```text
CrudRepository.findAll()
        │
        ▼
Iterable<Person>
        │
        ▼
Application
```

---

## Checking Whether an Entity Exists

`existsById()` checks whether an entity with a particular identifier exists:

```java
boolean exists = personRepository.existsById(id);
```

The test verifies this behavior:

```java
@Test
void existsById_shouldReturnTrueForExistingPerson() {
    Person savedPerson = personRepository.save(new Person("John"));

    assertThat(personRepository.existsById(savedPerson.getId())).isTrue();
}
```

This is useful when an application needs to determine whether an entity exists without retrieving the entity itself.

---

## Counting Entities

`count()` returns the number of entities managed by the repository:

```java
long count = personRepository.count();
```

The example tests this behavior:

```java
@Test
void count_shouldReturnNumberOfPeople() {
    personRepository.save(new Person("John"));
    personRepository.save(new Person("Jane"));

    assertThat(personRepository.count()).isEqualTo(2);
}
```

The operation provides a simple way to determine how many persisted entities are currently available.

---

## Deleting by ID

`deleteById()` removes an entity using its identifier:

```java
personRepository.deleteById(savedPerson.getId());
```

The test verifies that the entity can no longer be found afterward:

```java
@Test
void deleteById_shouldRemovePerson() {
    Person savedPerson = personRepository.save(new Person("John"));
    personRepository.deleteById(savedPerson.getId());

    assertThat(personRepository.findById(savedPerson.getId())).isEmpty();
}
```

The simplified flow is:

```text
Person ID
    │
    ▼
deleteById()
    │
    ▼
CrudRepository
    │
    ▼
JPA
    │
    ▼
Database
```

---

## Deleting an Entity

`CrudRepository` also provides `delete()`:

```java
personRepository.delete(savedPerson);
```

The example verifies that deleting the entity removes it from persistence:

```java
@Test
void delete_shouldRemovePerson() {
    Person savedPerson = personRepository.save(new Person("John"));
    personRepository.delete(savedPerson);

    assertThat(personRepository.findById(savedPerson.getId())).isEmpty();
}
```

This demonstrates the difference between:

```java
deleteById(id);
```

and:

```java
delete(entity);
```

The first deletes using an identifier, while the second deletes the supplied entity.

---

## No Repository Implementation

The repository does not contain a manually implemented class:

```java
public class PersonRepositoryImpl {
    // No manual implementation is required.
}
```

Instead:

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

Spring Data creates the repository infrastructure at runtime.

This is one of the central benefits of Spring Data repositories.

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

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
```

The entity is intentionally simple because this example focuses on the `CrudRepository` API rather than detailed JPA entity mapping.

---

## Testing with `@DataJpaTest`

The repository tests use:

```java
@DataJpaTest
class PersonRepositoryTest {
    // ...
}
```

`@DataJpaTest` provides focused Spring Boot test support for JPA components.

The tests use an embedded H2 database, allowing the repository operations to be tested against an actual persistence layer without requiring an external database.

The simplified testing flow is:

```text
@DataJpaTest
      │
      ▼
Spring Test Context
      │
      ▼
PersonRepository
      │
      ▼
JPA
      │
      ▼
H2 Database
```

---

## CRUD Operation Flow

The complete example can be viewed as:

```text
                    PersonRepository
                           │
                           ▼
                  CrudRepository<Person, Long>
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
        Create            Read            Delete
          │                │                │
          ▼                ▼                ▼
        save()       findById()/findAll()  delete()
                         │
                         ▼
                    existsById()
                         │
                         ▼
                       count()
```

All of these operations are provided through the repository abstraction.

---

## `Repository` vs `CrudRepository`

The two examples now demonstrate an important progression.

### Base `Repository`

```java
public interface PersonRepository extends Repository<Person, Long> {

    Person save(Person person);

    Optional<Person> findById(Long id);

    void deleteById(Long id);
}
```

The application explicitly declares the operations it wants to expose.

### `CrudRepository`

```java
public interface PersonRepository extends CrudRepository<Person, Long> {
}
```

The standard CRUD operations are already provided.

This makes `CrudRepository` useful when an application needs the conventional repository operations without having to declare each method manually.

---

## Running the Tests

Run the tests from the module:

```bash
mvn clean test
```

Or from the repository root:

```bash
mvn -pl 02-spring-data/crud-repository clean test
```

To run the complete verification lifecycle:

```bash
mvn -pl 02-spring-data/crud-repository clean verify
```

---

## Key Takeaways

- `CrudRepository` provides a standard CRUD repository abstraction.
- It extends Spring Data's base `Repository` abstraction.
- Standard persistence operations are provided without manually implementing them.
- `save()` persists entities.
- `findById()` retrieves an entity by its identifier.
- `findAll()` retrieves all entities.
- `existsById()` checks whether an entity exists.
- `count()` returns the number of entities.
- `deleteById()` deletes an entity by its identifier.
- `delete()` deletes an entity.
- Spring Data creates the repository implementation at runtime.
- `@DataJpaTest` provides focused JPA testing support.
- H2 can be used as an embedded database for repository tests.

---

## Next Example

The next example will explore **Query Methods** and how Spring Data can derive database queries from repository method names.
