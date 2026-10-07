# Spring Data Database Testing

This example demonstrates how to test Spring Data JPA repositories against a real database using Spring Boot's `@DataJpaTest` test slice and an embedded H2 database.

The focus is on repository integration testing rather than unit testing repository interfaces with mocks.

## What This Example Demonstrates

- Testing Spring Data JPA repositories with `@DataJpaTest`
- Using an embedded H2 database for tests
- Persisting and retrieving entities through a repository
- Testing derived query methods
- Testing JPQL queries with `@Query`
- Loading test data with `@Sql`
- Verifying test database isolation
- Understanding transactional test rollback
- Testing repository behavior against an actual database

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

The entity is intentionally simple so the example can focus on database testing rather than JPA mapping.

---

## Repository

The repository contains both a derived query and a JPQL query:

```java
public interface PersonRepository extends CrudRepository<Person, Long> {

    List<Person> findByAgeGreaterThan(int age);

    @Query("""
            select p
            from Person p
            where lower(p.name) like lower(concat('%', :text, '%'))
            """)
    List<Person> searchByName(@Param("text") String text);
}
```

This allows the tests to demonstrate two different repository query mechanisms.

### Derived Query

Spring Data derives the query from the method name:

```java
List<Person> findByAgeGreaterThan(int age);
```

Spring Data translates this method into the appropriate database query.

### JPQL Query

The `searchByName` method explicitly defines a JPQL query:

```java
@Query("""
        select p
        from Person p
        where lower(p.name) like lower(concat('%', :text, '%'))
        """)
List<Person> searchByName(@Param("text") String text);
```

This demonstrates that database tests can verify that manually defined queries actually work against the database.

---

## Testing with `@DataJpaTest`

The test class uses `@DataJpaTest`:

```java
@DataJpaTest
class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    // tests
}
```

`@DataJpaTest` is designed specifically for testing JPA-related components.

It configures the JPA infrastructure, repositories, entity mappings, and an embedded database for the test.

This makes it a better fit for repository integration tests than loading the entire application with `@SpringBootTest`.

---

## Loading Test Data with `@Sql`

Spring's `@Sql` annotation can be used to load SQL test data before a test.

The SQL file is stored at:

```text
src/test/resources/test-data/people.sql
```

The test can then reference it explicitly from the classpath:

```java
@Test
@Sql("classpath:test-data/people.sql")
void findAll_shouldLoadSqlTestData() {
    Iterable<Person> people = personRepository.findAll();

    assertThatIterable(people).extracting(Person::getName).containsExactlyInAnyOrder("John", "Jane");
    assertThatIterable(people).extracting(Person::getAge).containsExactlyInAnyOrder(30, 25);
}
```

The SQL file contains:

```sql
INSERT INTO person (name, age)
VALUES ('John', 30);

INSERT INTO person (name, age)
VALUES ('Jane', 25);
```

Using `classpath:` makes it explicit that Spring should look for the script on the test classpath.

---

## Why Use Database Tests?

A unit test can verify application logic without touching a database.

For example, a mocked repository can verify that a method was called:

```text
Service
   ↓
Mock Repository
```

But this does not verify whether the repository query itself is valid.

A database integration test verifies:

```text
Repository
    ↓
Spring Data
    ↓
JPA
    ↓
Hibernate
    ↓
H2 Database
```

This allows the test to catch problems such as:

- Invalid JPQL
- Incorrect entity mappings
- Incorrect derived query methods
- Incorrect column mappings
- Persistence problems
- Unexpected query results

---

## `@DataJpaTest` vs `@SpringBootTest`

For repository-focused tests, `@DataJpaTest` is usually the more focused choice.

| Annotation        | Purpose                                          |
|-------------------|--------------------------------------------------|
| `@DataJpaTest`    | Test JPA repositories and persistence components |
| `@SpringBootTest` | Load the complete Spring application context     |

This example deliberately uses `@DataJpaTest` because the goal is to test the database layer rather than the entire application.

---

## Running the Tests

Run the tests for this module with:

```bash
mvn test
```

To run the complete module build:

```bash
mvn verify
```

From the project root, the module can also be tested through Maven:

```bash
mvn -pl 02-spring-data/database-testing test
```

---

## Key Takeaways

- `@DataJpaTest` provides a focused test slice for JPA repositories.
- An embedded H2 database allows repository queries to execute against a real database.
- Derived queries should be tested to verify that Spring Data interprets the method name correctly.
- JPQL queries should be tested because query syntax and behavior cannot be verified by mocking alone.
- `@Sql` provides a convenient way to load predefined test data.
- Test SQL resources belong under `src/test/resources`.
- Explicit `classpath:` paths make `@Sql` resource locations clear.
- `@DataJpaTest` tests are transactional and normally roll back after each test.
- Database integration tests complement unit tests rather than replacing them.

---

## Next Example

The next example moves beyond traditional blocking persistence and begins **Module 03 — Reactive Spring**.

The focus will shift from imperative database access to reactive programming and the reactive Spring ecosystem.
