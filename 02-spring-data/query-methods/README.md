# Query Methods

This example demonstrates **Spring Data Query Methods** and how Spring Data can derive database queries directly from repository method names.

Instead of writing JPQL or SQL manually, Spring Data analyzes a repository method name and creates the appropriate query automatically.

## What This Example Demonstrates

- Query method derivation
- `findBy`
- Property-based queries
- `Containing`
- `StartingWith`
- `GreaterThan`
- `LessThan`
- `Between`
- Repository testing with `@DataJpaTest`

---

## What Are Query Methods?

Spring Data allows repository interfaces to define queries using method names.

For example:

```java
List<Person> findByName(String name);
```

Spring Data recognizes:

```text
findBy
```

as the query prefix and:

```text
Name
```

as the entity property to query.

Conceptually, the method represents a query similar to:

```sql
SELECT * FROM person WHERE name = ?
```

The developer does not need to write the SQL or JPQL manually.

Spring Data parses the method name and creates the query implementation at runtime.

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

The entity contains:

- `id`
- `name`
- `age`

These properties can then be referenced by repository query methods.

---

## Repository

The repository extends `CrudRepository`:

```java
public interface PersonRepository extends CrudRepository<Person, Long> {

    List<Person> findByName(String name);

    List<Person> findByNameContaining(String text);

    List<Person> findByNameStartingWith(String prefix);

    List<Person> findByAgeGreaterThan(int age);

    List<Person> findByAgeLessThan(int age);

    List<Person> findByAgeBetween(int minimum, int maximum);
}
```

The important part of this example is that none of these query methods require an explicit query definition.

Spring Data derives the queries from their names.

---

## `findByName`

The simplest example is:

```java
List<Person> findByName(String name);
```

The method name can be understood as:

```text
find
  ↓
By
  ↓
Name
```

Spring Data interprets this as a query for people whose `name` property matches the supplied value.

For example:

```java
personRepository.findByName("John");
```

Conceptually represents:

```sql
SELECT * FROM person WHERE name = 'John';
```

The test verifies that only the matching person is returned:

```java
@Test
void findByName_shouldReturnPeopleWithMatchingName() {
    personRepository.save(new Person("John", 30));
    personRepository.save(new Person("Jane", 25));

    Iterable<Person> people = personRepository.findByName("John");

    assertThatIterable(people).extracting(Person::getName).containsExactly("John");
}
```

---

## `Containing`

Spring Data also supports keywords such as `Containing`.

```java
List<Person> findByNameContaining(String text);
```

This allows queries based on whether a property contains a particular value.

For example:

```java
personRepository.findByNameContaining("John");
```

can match:

```text
John
Johnny
Johnathan
```

Conceptually, this corresponds to a pattern-based query such as:

```sql
SELECT * FROM person WHERE name LIKE '%John%';
```

The test demonstrates this behavior:

```java
@Test
void findByNameContaining_shouldReturnPeopleContainingText() {
    personRepository.save(new Person("John", 30));
    personRepository.save(new Person("Johnny", 35));
    personRepository.save(new Person("Jane", 25));

    List<Person> people = personRepository.findByNameContaining("John");

    assertThat(people).extracting(Person::getName).containsExactlyInAnyOrder("John", "Johnny");
}
```

---

## `StartingWith`

`StartingWith` creates a query that matches values beginning with a particular prefix.

```java
List<Person> findByNameStartingWith(String prefix);
```

For example:

```java
personRepository.findByNameStartingWith("Jo");
```

can match:

```text
John
Johnny
Johnathan
```

but not:

```text
Jane
Peter
```

Conceptually:

```sql
SELECT * FROM person WHERE name LIKE 'Jo%';
```

---

## `GreaterThan`

Query methods can also express comparisons.

```java
List<Person> findByAgeGreaterThan(int age);
```

For example:

```java
personRepository.findByAgeGreaterThan(30);
```

finds people whose age is greater than `30`.

Conceptually:

```sql
SELECT * FROM person WHERE age > 30;
```

The test uses:

```java
@Test
void findByAgeGreaterThan_shouldReturnPeopleAboveAge() {
    personRepository.save(new Person("John", 30));
    personRepository.save(new Person("Jane", 25));
    personRepository.save(new Person("Peter", 40));

    List<Person> people = personRepository.findByAgeGreaterThan(30);

    assertThat(people).extracting(Person::getName).containsExactly("Peter");
}
```

---

## `LessThan`

`LessThan` works in the opposite direction:

```java
List<Person> findByAgeLessThan(int age);
```

For example:

```java
personRepository.findByAgeLessThan(30);
```

conceptually represents:

```sql
SELECT * FROM person WHERE age < 30;
```

With the example data:

```text
John   30
Jane   25
Peter  40
```

only Jane matches.

---

## `Between`

Spring Data also supports range queries using `Between`:

```java
List<Person> findByAgeBetween(int minimum, int maximum);
```

For example:

```java
personRepository.findByAgeBetween(25, 35);
```

finds people whose age falls between the supplied boundaries.

Conceptually:

```sql
SELECT * FROM person WHERE age BETWEEN 25 AND 35;
```

For the example data:

```text
John   30
Jane   25
Peter  40
```

the matching people are:

```text
John
Jane
```

The test verifies this:

```java
@Test
void findByAgeBetween_shouldReturnPeopleWithinAgeRange() {
    personRepository.save(new Person("John", 30));
    personRepository.save(new Person("Jane", 25));
    personRepository.save(new Person("Peter", 40));

    List<Person> people = personRepository.findByAgeBetween(25, 35);

    assertThat(people).extracting(Person::getName).containsExactlyInAnyOrder("John", "Jane");
}
```

---

## How Query Derivation Works

A useful way to think about query methods is:

```text
Repository Method
       ↓
Spring Data parses method name
       ↓
Identifies entity property
       ↓
Identifies query keyword
       ↓
Creates query
       ↓
Executes query through JPA
       ↓
Returns entities
```

For example:

```text
findByAgeGreaterThan
```

can be broken down into:

```text
find
 ↓
By
 ↓
Age
 ↓
GreaterThan
```

Where:

- `find` indicates a retrieval operation
- `By` separates the operation from the query criteria
- `Age` identifies the entity property
- `GreaterThan` specifies the comparison

---

## Common Query Keywords

Spring Data supports many query keywords.

Some common examples include:

| Keyword            | Example                     | Meaning                        |
|--------------------|-----------------------------|--------------------------------|
| `Is`               | `findByName`                | Equals                         |
| `Containing`       | `findByNameContaining`      | Contains value                 |
| `StartingWith`     | `findByNameStartingWith`    | Starts with value              |
| `EndingWith`       | `findByNameEndingWith`      | Ends with value                |
| `GreaterThan`      | `findByAgeGreaterThan`      | Greater than                   |
| `LessThan`         | `findByAgeLessThan`         | Less than                      |
| `GreaterThanEqual` | `findByAgeGreaterThanEqual` | Greater than or equal          |
| `LessThanEqual`    | `findByAgeLessThanEqual`    | Less than or equal             |
| `Between`          | `findByAgeBetween`          | Within a range                 |
| `In`               | `findByNameIn`              | Matches values in a collection |
| `Not`              | `findByNameNot`             | Not equal                      |
| `IsNull`           | `findByNameIsNull`          | Property is null               |
| `IsNotNull`        | `findByNameIsNotNull`       | Property is not null           |

This makes repository interfaces expressive without requiring query strings for many common cases.

---

## Testing

The example uses:

```java
@DataJpaTest
```

`@DataJpaTest` provides a test environment focused on JPA components.

The tests use the H2 database configured for the example.

For example:

```java
@DataJpaTest
class PersonRepositoryTest {
```

Each test can persist entities and execute repository queries against the test database.

---

## Why Use Query Methods?

Query methods are useful when the query can be clearly expressed through a method name.

For example:

```java
findByName(String name)
```

is simple and readable.

So is:

```java
findByAgeGreaterThan(int age)
```

This avoids writing explicit JPQL for straightforward queries.

It also keeps repository interfaces strongly aligned with the domain model.

---

## When Query Methods Become Difficult

Method-name query derivation can become less convenient when queries become very complex.

For example, a method such as:

```java
findByFirstNameAndLastNameAndAgeGreaterThanAndStatusAndCreatedDateBetween(...)
```

can become difficult to read and maintain.

For more complex queries, Spring Data provides other mechanisms such as:

- `@Query`
- Specifications
- Custom repository implementations
- Query by Example
- QueryDSL integrations

Those approaches are outside the scope of this example and will be explored separately where appropriate.

---

## Query Methods vs `@Query`

This example focuses on derived queries.

For example:

```java
List<Person> findByNameContaining(String text);
```

The query is derived from the method name.

With `@Query`, the query itself is explicitly defined:

```java
@Query("select p from Person p where p.name like %:text%")
List<Person> searchByName(@Param("text") String text);
```

The next example in this module will explore explicit queries using `@Query`.

---

## Running the Tests

From the project root:

```bash
mvn -pl 02-spring-data/query-methods clean verify
```

Or run the complete project test suite:

```bash
mvn clean verify
```

---

## Key Takeaways

- Spring Data can derive queries from repository method names.
- `findBy` identifies a query method.
- Entity properties are referenced directly in method names.
- Query keywords describe the required operation.
- `Containing` supports substring matching.
- `StartingWith` supports prefix matching.
- `GreaterThan` and `LessThan` support comparisons.
- `Between` supports range queries.
- No explicit JPQL or SQL is required for these examples.
- `@DataJpaTest` provides a focused environment for testing JPA repositories.
- Query methods are convenient for straightforward repository queries.

---

## Next Example

The next example will explore **`@Query`** and how to define explicit repository queries using JPQL.
