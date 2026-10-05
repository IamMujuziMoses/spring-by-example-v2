# Spring Data Specifications

This example demonstrates how to use **Spring Data JPA Specifications** to build reusable and composable database query predicates.

Specifications are useful when query requirements become more dynamic than simple repository query methods or `@Query` annotations can comfortably support.

The example uses `JpaSpecificationExecutor` together with the JPA Criteria API to define reusable specifications and combine them using `and()` and `or()`.

## What This Example Demonstrates

- Creating reusable `Specification` implementations
- Using the JPA Criteria API
- Extending a repository with `JpaSpecificationExecutor`
- Filtering by exact values
- Filtering using partial text matches
- Filtering by numeric comparisons
- Filtering using a range
- Combining specifications with `and()`
- Combining specifications with `or()`
- Counting records using a specification
- Checking whether matching records exist
- Combining specifications with pagination

---

## The Person Entity

The example uses a simple `Person` entity containing a name and age.

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

The entity is intentionally simple so the example can focus on Specifications rather than entity modelling.

---

## JpaSpecificationExecutor

A repository can extend `JpaSpecificationExecutor` to gain support for Specification-based queries.

```java
public interface PersonRepository extends CrudRepository<Person, Long>, JpaSpecificationExecutor<Person> {
}
```

`JpaSpecificationExecutor` provides methods such as:

- `findAll(Specification<T>)`
- `findAll(Specification<T>, Pageable)`
- `findAll(Specification<T>, Sort)`
- `findOne(Specification<T>)`
- `count(Specification<T>)`
- `exists(Specification<T>)`

This allows specifications to be passed directly to the repository.

---

## Creating Specifications

Specifications can be defined as reusable predicates.

The example provides several specifications for the `Person` entity.

### Exact Name

```java
public static Specification<Person> hasName(String name) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), name);
}
```

This creates a predicate equivalent to:

```sql
where name = ?
```

The specification can then be used directly:

```java
List<Person> people = personRepository.findAll(PersonSpecifications.hasName("Jane"));
```

---

## Text Search

A specification can also perform a partial text search.

```java
public static Specification<Person> nameContains(String text) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"), "%" + text + "%");
}
```

For example:

```java
List<Person> people = personRepository.findAll(PersonSpecifications.nameContains("Jan"));
```

This can match both:

```text
Jane
Janet
```

---

## Numeric Comparisons

Specifications can also represent numeric conditions.

### Greater Than

```java
public static Specification<Person> ageGreaterThan(int age) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("age"), age);
}
```

Usage:

```java
List<Person> people = personRepository.findAll(PersonSpecifications.ageGreaterThan(30));
```

### Less Than

```java
public static Specification<Person> ageLessThan(int age) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("age"), age);
}
```

Usage:

```java
List<Person> people = personRepository.findAll(PersonSpecifications.ageLessThan(30));
```

---

## Range Queries

Specifications can also represent a range.

```java
public static Specification<Person> ageBetween(int minimum, int maximum) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("age"), minimum, maximum);
}
```

Usage:

```java
List<Person> people = personRepository.findAll(PersonSpecifications.ageBetween(30, 40));
```

This makes the filtering rule reusable instead of embedding the criteria directly inside every repository method.

---

## Combining Specifications

One of the main advantages of Specifications is that individual predicates can be composed.

### AND

Two specifications can be combined using `and()`.

```java
Specification<Person> specification = PersonSpecifications.nameContains("Jan").and(PersonSpecifications.ageGreaterThan(30));

List<Person> people = personRepository.findAll(specification);
```

This produces a query conceptually similar to:

```sql
where name like '%Jan%' and age > 30
```

For example, given:

| Name  | Age |
|-------|----:|
| John  |  30 |
| Jane  |  25 |
| Peter |  40 |
| Janet |  35 |

Only `Janet` matches both conditions.

---

## OR

Specifications can also be combined using `or()`.

```java
Specification<Person> specification = PersonSpecifications.hasName("John").or(PersonSpecifications.hasName("Peter"));

List<Person> people = personRepository.findAll(specification);
```

This produces a query conceptually similar to:

```sql
where name = 'John' or name = 'Peter'
```

The important idea is that the individual specifications remain reusable and the application can compose them according to the query requirements.

---

## Counting With Specifications

`JpaSpecificationExecutor` can also count records matching a specification.

```java
long count = personRepository.count(PersonSpecifications.ageGreaterThan(25));
```

For example, if the database contains:

```text
John   30
Jane   25
Peter  40
```

then:

```java
assertThat(count).isEqualTo(2);
```

The specification is reused without requiring a separate count query.

---

## Checking Whether a Match Exists

Specifications can also be used with `exists()`.

```java
boolean exists = personRepository.exists(PersonSpecifications.hasName("John"));
```

This is useful when the application only needs to know whether at least one matching entity exists.

---

## Specifications and Pagination

Specifications work together with Spring Data pagination.

```java
Page<Person> page = personRepository.findAll(PersonSpecifications.ageGreaterThan(25), PageRequest.of(0, 2, Sort.by("id")
        .ascending()));
```

This combines:

1. A dynamic filtering condition
2. Pagination
3. Sorting

The result is a `Page<Person>` containing both the requested records and pagination metadata.

For example:

```java
assertThat(page.getContent()).extracting(Person::getName).containsExactly("John", "Peter");
assertThat(page.getTotalElements()).isEqualTo(4);
assertThat(page.getTotalPages()).isEqualTo(2);
```

This is particularly useful when an application needs to support dynamic search together with paginated results.

---

## Specification Flow

A simplified view of the process is:

```text
Specification
      ↓
JpaSpecificationExecutor
      ↓
JPA Criteria API
      ↓
Criteria Predicate
      ↓
Generated SQL
      ↓
Database
```

The application defines reusable query conditions as Specifications.

Spring Data JPA then passes those specifications to the JPA Criteria API, which builds the corresponding query.

---

## Specifications vs Query Methods

Spring Data query methods work well for simple, predefined queries:

```java
List<Person> findByName(String name);

List<Person> findByAgeGreaterThan(int age);
```

However, applications can quickly accumulate many repository methods when filtering requirements become more dynamic.

Specifications allow those conditions to be represented independently:

```java
Specification<Person> specification = PersonSpecifications.nameContains("Jan").and(PersonSpecifications.ageGreaterThan(30));
```

This makes it possible to compose different query conditions without creating a separate repository method for every combination.

| Approach       | Best suited for                  |
|----------------|----------------------------------|
| Query Methods  | Simple, predefined queries       |
| `@Query`       | Explicit JPQL queries            |
| Specifications | Dynamic and composable filtering |

---

## Testing with @DataJpaTest

The example uses Spring Boot's `@DataJpaTest`.

```java
@DataJpaTest
class PersonRepositoryTest {
}
```

This provides a focused JPA test environment with the repository and an embedded database.

The tests cover:

- Exact name matching
- Partial name matching
- Age greater than
- Age less than
- Age ranges
- Combining specifications with `and()`
- Combining specifications with `or()`
- Counting matching entities
- Checking whether matching entities exist
- Combining specifications with pagination

Each test runs against the actual repository and JPA infrastructure rather than mocking the Specification behaviour.

---

## Running the Tests

From the project root:

```bash
mvn test -pl 02-spring-data/specifications
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

- A `Specification` represents a reusable query predicate.
- Specifications are based on the JPA Criteria API.
- `JpaSpecificationExecutor` enables Specification-based repository operations.
- Specifications can be reused across different queries.
- Specifications can be composed using `and()` and `or()`.
- Specifications support operations such as `findAll()`, `count()`, and `exists()`.
- Specifications work together with pagination and sorting.
- Specifications are particularly useful for dynamic search requirements.
- They reduce the need to create repository methods for every possible combination of filters.

The key idea is:

> **Define query conditions independently, then compose them when building the query.**

---

## Next Example

The next example in Module 02 is:

**Projections**

It will demonstrate how Spring Data JPA can retrieve only selected fields from an entity instead of loading the complete entity.