# Spring Data Projections

This example demonstrates how to use **Spring Data JPA Projections** to retrieve only the data an application needs instead of always working with the complete entity.

Projections are useful when an application needs a simplified view of an entity, such as returning a person's ID and name without exposing the rest of the entity's data.

The example demonstrates **interface-based projections**, **class-based DTO projections**, and **dynamic projections**.

## What This Example Demonstrates

- Interface-based projections
- Class-based DTO projections
- Java record DTO projections
- Dynamic projections
- Selecting only required entity properties
- Using projections with derived query methods
- Using the same repository method with different projection types
- Testing projections with `@DataJpaTest`

---

## The Person Entity

The example uses a simple `Person` entity containing an ID, name, and age.

```java
@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int age;

    protected Person() {
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
```

The entity is intentionally simple so the example can focus on projections.

---

## What Is a Projection?

A projection provides a view of an entity containing only selected properties.

Instead of returning:

```text
Person
├── id
├── name
└── age
```

an application can request:

```text
PersonNameProjection
├── id
└── name
```

This is useful when the consumer does not need the complete entity.

For example, a user list might only need:

```text
ID
Name
```

rather than every property available on `Person`.

---

## Interface-Based Projections

The first projection is an interface-based projection.

```java
public interface PersonNameProjection {

    Long getId();

    String getName();
}
```

The projection exposes only the properties required by the caller.

The repository can return the projection directly:

```java
List<PersonNameProjection> findByName(String name);
```

For example:

```java
List<PersonNameProjection> people = personRepository.findByName("Jane");
```

The returned objects expose:

```java
people.get(0).getId();
people.get(0).getName();
```

but the projection does not expose the `age` property.

---

## Why Use Interface Projections?

Interface projections are useful when the application needs a small view of an entity without creating a separate DTO class.

For example:

```text
Person
    ↓
┌───────────────┐
│ id            │
│ name          │
└───────────────┘
    ↓
PersonNameProjection
```

Spring Data JPA creates the projection implementation at runtime.

This allows the repository method to return an interface instead of the entity itself.

---

## Class-Based Projections

The second approach uses a DTO.

Because this project uses Java 21, a record provides a concise representation for the DTO.

```java
public record PersonSummary(Long id, String name, int age) {
}
```

The repository can return this DTO:

```java
List<PersonSummary> findByAgeGreaterThan(int age);
```

For example:

```java
List<PersonSummary> people = personRepository.findByAgeGreaterThan(30);
```

The result contains the selected properties:

```text
PersonSummary
├── id
├── name
└── age
```

Unlike an interface projection, this is a concrete DTO type.

---

## Why Use DTO Projections?

DTO projections are useful when the returned data represents a specific application-facing structure.

For example, a REST endpoint might need:

```text
PersonSummary
├── id
├── name
└── age
```

without returning the complete persistence entity.

This also separates the representation returned to the caller from the JPA entity itself.

---

## Dynamic Projections

Spring Data JPA also supports dynamic projections.

The repository can define:

```java
<T> List<T> findByAgeGreaterThan(int age, Class<T> type);
```

The caller then chooses the projection type.

For example:

```java
List<PersonNameProjection> names = personRepository.findByAgeGreaterThan(25, PersonNameProjection.class);
```

Or:

```java
List<PersonSummary> summaries = personRepository.findByAgeGreaterThan(25, PersonSummary.class);
```

The same repository method can therefore return different projection types.

```text
findByAgeGreaterThan()
          │
          ├── PersonNameProjection.class
          │       ↓
          │   ID + Name
          │
          └── PersonSummary.class
                  ↓
              ID + Name + Age
```

This is the main idea behind dynamic projections.

---

## Repository

The complete repository combines the three approaches:

```java
public interface PersonRepository extends CrudRepository<Person, Long> {

    List<PersonNameProjection> findByName(String name);

    List<PersonSummary> findByAgeGreaterThan(int age);

    <T> List<T> findByAgeGreaterThan(int age, Class<T> type);
}
```

Each method demonstrates a different projection mechanism.

| Repository Method                     | Projection Type      |
|---------------------------------------|----------------------|
| `findByName(String)`                  | Interface projection |
| `findByAgeGreaterThan(int)`           | DTO projection       |
| `findByAgeGreaterThan(int, Class<T>)` | Dynamic projection   |

---

## Entity vs Projection

Without a projection:

```java
List<Person> people = ...
```

The caller works with the complete entity.

With an interface projection:

```java
List<PersonNameProjection> people = ...
```

The caller receives a focused view:

```text
ID + Name
```

With a DTO projection:

```java
List<PersonSummary> people = ...
```

The caller receives a concrete data transfer object:

```text
ID + Name + Age
```

The choice depends on the needs of the application.

---

## When to Use Projections

Projections are particularly useful when:

- Only a subset of entity properties is required.
- A query is used to populate a list or summary view.
- An API should return a specific representation rather than a persistence entity.
- Loading complete entities would be unnecessary.
- Different consumers require different views of the same entity.
- A repository query needs to return a specific DTO structure.

---

## Projections vs Specifications

The previous example introduced Specifications for dynamic query predicates.

The two features solve different problems.

| Feature        | Purpose                                     |
|----------------|---------------------------------------------|
| Specifications | Define **which records** should be returned |
| Projections    | Define **which data** should be returned    |

They can also be used together in larger applications.

For example:

```text
Specification
      ↓
Which people match?
      ↓
Projection
      ↓
Which fields should be returned?
```

This makes Specifications and Projections complementary rather than competing features.

---

## Testing with @DataJpaTest

The example uses Spring Boot's `@DataJpaTest`.

```java
@DataJpaTest
public class PersonRepositoryTest {
}
```

The tests verify:

- Interface-based projections
- DTO projections
- Dynamic projections
- Projection property values
- Generated entity IDs
- Multiple projection types using the same repository method

The tests interact with the real Spring Data JPA repository and embedded database rather than mocking projection behaviour.

---

## Running the Tests

From the project root:

```bash
mvn test -pl 02-spring-data/projections
```

Or run the complete Maven test suite:

```bash
mvn test
```

To run verification:

```bash
mvn clean verify
```

---

## Key Takeaways

- Projections provide a focused view of an entity.
- Interface-based projections are useful for simple entity views.
- Class-based projections provide concrete DTO representations.
- Java records are a concise option for DTO projections.
- Dynamic projections allow the caller to select the result type.
- Projections can reduce the amount of entity data exposed to application layers.
- Specifications and Projections solve different problems and can complement each other.

The key idea is:

> **Use projections when you need a specific view of your entity rather than the complete entity itself.**

---

## Next Example

The next example in Module 02 is:

**Auditing**

It will demonstrate how Spring Data JPA can automatically track entity creation and modification metadata.