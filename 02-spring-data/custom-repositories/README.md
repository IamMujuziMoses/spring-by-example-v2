# Spring Data Custom Repositories

This example demonstrates how to extend a Spring Data repository with custom data-access behavior using a **repository fragment**.

Spring Data provides many repository operations out of the box, including CRUD operations, derived query methods, and `@Query`. However, some data-access operations require custom implementation logic.

Custom repository fragments allow us to add this behavior while keeping the standard Spring Data repository functionality.

This example uses a `PersonRepositoryCustom` fragment implemented by `PersonRepositoryCustomImpl` and composed into the main `PersonRepository`.

## What This Example Demonstrates

- Extending a Spring Data repository with custom functionality
- Creating a custom repository fragment interface
- Implementing a custom repository fragment
- Using `EntityManager` inside a custom repository implementation
- Combining a custom repository fragment with `CrudRepository`
- Spring Data repository composition
- Automatic discovery of the `Impl` implementation
- Testing custom repository behavior with `@DataJpaTest`

---

## Why Custom Repositories?

Spring Data provides several ways to define repository operations.

For example, a simple query can be expressed using a derived query method:

```java
List<Person> findByName(String name);
```

A more explicit query can use `@Query`:

```java
@Query("select p from Person p where p.name = :name")
List<Person> findPeopleByName(@Param("name") String name);
```

For dynamic filtering, a `Specification` can be used:

```java
personRepository.findAll(PersonSpecifications.ageGreaterThan(30));
```

However, some operations require custom implementation logic that does not fit naturally into these approaches.

That is where a custom repository fragment can be useful.

```text
Derived Query
     ↓
findByName(...)

@Query
     ↓
Custom JPQL

Specification
     ↓
Dynamic Criteria

Custom Repository
     ↓
Custom Implementation Logic
```

---

## Repository Composition

The main repository combines `CrudRepository` with the custom repository fragment.

```java
public interface PersonRepository extends CrudRepository<Person, Long>, PersonRepositoryCustom {
}
```

This means the repository exposes both the standard CRUD operations and the custom `search` operation.

Conceptually:

```text
PersonRepository
       │
       ├── CrudRepository
       │      ├── save()
       │      ├── findById()
       │      ├── findAll()
       │      ├── delete()
       │      └── ...
       │
       └── PersonRepositoryCustom
              └── search()
```

Spring Data combines these repository components when creating the repository bean.

---

## Custom Repository Fragment

The custom behavior is first declared in its own interface.

```java
public interface PersonRepositoryCustom {

    List<Person> search(String text);
}
```

This interface defines the custom operation that should become part of `PersonRepository`.

It does not contain any implementation details.

---

## Custom Repository Implementation

The implementation provides the actual custom behavior.

```java
public class PersonRepositoryCustomImpl implements PersonRepositoryCustom {

    private final EntityManager entityManager;

    public PersonRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Person> search(String text) {
        return entityManager.createQuery(
                        """
                        select p
                        from Person p
                        where lower(p.name) like lower(:text)
                        """,
                        Person.class)
                .setParameter("text", "%" + text + "%")
                .getResultList();
    }
}
```

The implementation uses `EntityManager` directly to execute a JPQL query.

The query performs a case-insensitive partial match against the person's name.

For example:

```text
search("john")
```

can match:

```text
John Doe
Johnny Brown
```

---

## Why Is `PersonRepositoryCustomImpl` Not Explicitly Used?

There is no code such as:

```java
new PersonRepositoryCustomImpl(...)
```

in the application.

This is intentional.

Spring Data automatically discovers the implementation as part of repository composition.

The naming convention connects the fragment interface and implementation:

```text
PersonRepositoryCustom
        ↓
PersonRepositoryCustomImpl
```

The main repository then includes the fragment:

```java
public interface PersonRepository extends CrudRepository<Person, Long>, PersonRepositoryCustom {
}
```

When Spring creates the `PersonRepository` bean, the custom implementation is composed into the repository.

Therefore:

```java
personRepository.search("john");
```

ultimately invokes:

```java
PersonRepositoryCustomImpl.search(...)
```

The application interacts only with the repository interface.

---

## Repository Composition Flow

The complete flow can be simplified as:

```text
PersonRepository
        │
        ├─────────────────────┐
        ↓                     ↓
CrudRepository       PersonRepositoryCustom
        │                     │
        ↓                     ↓
Standard Spring Data   PersonRepositoryCustomImpl
implementation                │
        │                     ↓
        │               EntityManager
        │                     │
        └──────────┬──────────┘
                   ↓
          Composed Repository
```

This allows the repository to expose standard Spring Data operations alongside custom behavior.

---

## The Entity

The example uses a simple `Person` entity.

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

The entity is intentionally small so that the focus remains on repository customization.

---

## Testing the Standard Repository Behavior

The custom repository should continue to provide the standard operations inherited from `CrudRepository`.

```java
@Test
void save_shouldPersistPerson() {
    Person person = personRepository.save(new Person("John", 30));

    assertThat(person.getId()).isNotNull();
    assertThat(person.getName()).isEqualTo("John");
    assertThat(person.getAge()).isEqualTo(30);
}
```

This demonstrates that adding a custom repository fragment does not replace the standard repository implementation.

---

## Testing the Custom Search

The custom `search` method can be tested through the main repository:

```java
@Test
void search_shouldFindPeopleByName() {
    personRepository.save(new Person("John Doe", 30));
    personRepository.save(new Person("Jane Smith", 25));
    personRepository.save(new Person("Johnny Brown", 40));

    List<Person> people = personRepository.search("john");

    assertThat(people).extracting(Person::getName).containsExactlyInAnyOrder("John Doe", "Johnny Brown");
}
```

Notice that the test does not instantiate `PersonRepositoryCustomImpl`.

It interacts with the repository exactly as application code would:

```java
personRepository.search("john");
```

Spring Data handles the composition and delegates the call to the custom implementation.

---

## Case-Insensitive Search

The custom implementation uses `lower()` for both the entity property and the search parameter.

```java
where lower(p.name) like lower(:text)
```

This allows searches such as:

```java
personRepository.search("JOHN");
```

to match:

```text
John Doe
```

The behavior is verified by:

```java
@Test
void search_shouldBeCaseInsensitive() {
    personRepository.save(new Person("John Doe", 30));
    personRepository.save(new Person("Jane Smith", 25));

    List<Person> people = personRepository.search("JOHN");

    assertThat(people).extracting(Person::getName).containsExactly("John Doe");
}
```

---

## Empty Search Results

The custom implementation should also correctly handle cases where nothing matches.

```java
@Test
void search_shouldReturnEmptyResultWhenNothingMatches() {
    personRepository.save(new Person("John Doe", 30));

    List<Person> people = personRepository.search("Peter");

    assertThat(people).isEmpty();
}
```

An empty result is returned rather than `null` or an exception.

---

## Testing with `@DataJpaTest`

The repository is tested using `@DataJpaTest`:

```java
@DataJpaTest
class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    // ...
}
```

`@DataJpaTest` provides a focused JPA test environment with:

- Entity scanning
- Spring Data repository configuration
- JPA infrastructure
- An embedded test database
- Transactional test execution

This allows the custom repository implementation to be tested through the same Spring Data repository composition mechanism used by the application.

---

## Why Test Through `PersonRepository`?

The custom implementation is an implementation detail.

The important behavior is that:

```java
personRepository.search("john");
```

works correctly.

Testing through the main repository verifies the complete integration:

```text
PersonRepository
      ↓
Repository Composition
      ↓
PersonRepositoryCustom
      ↓
PersonRepositoryCustomImpl
      ↓
EntityManager
      ↓
Database
```

This is more useful than directly instantiating the implementation because it verifies that Spring Data actually discovers and composes the custom repository fragment.

---

## When to Use Custom Repositories

Custom repository fragments are useful when repository behavior requires implementation logic that does not fit naturally into:

- Derived query methods
- `@Query`
- Specifications
- Standard repository methods

For example:

```text
Simple query
    → Derived Query

Explicit JPQL
    → @Query

Dynamic filtering
    → Specification

Custom data-access behavior
    → Custom Repository Fragment
```

A custom implementation can also use other data-access infrastructure when appropriate, such as `EntityManager` or other Spring-managed dependencies.

---

## Custom Repository vs Service Layer

A custom repository and a service layer have different responsibilities.

A repository should focus on **data access**:

```text
Repository
    ↓
Database
```

A service should generally coordinate **application or business logic**:

```text
Service
    ↓
Business Logic
    ↓
Repository
```

This example does not introduce a service because the custom behavior is specifically data-access logic.

For example, constructing and executing a custom JPQL query belongs naturally in the repository layer.

---

## Scope of This Example

This example focuses specifically on repository fragments.

It does not cover:

- Custom repository base classes
- Replacing `SimpleJpaRepository`
- Custom repository factory implementations
- Repository factory customization
- Native SQL
- Querydsl
- JDBC repositories

These are separate and more advanced repository customization mechanisms.

---

## Running the Tests

From the module directory:

```bash
mvn test
```

Or from the project root:

```bash
mvn -pl 02-spring-data/custom-repositories test
```

To run the complete project build:

```bash
mvn clean verify
```

---

## Key Takeaways

- Spring Data repositories can be extended with custom repository fragments.
- A custom fragment is declared using a separate repository interface.
- The implementation provides the actual custom data-access behavior.
- Spring Data automatically discovers implementations using the expected `Impl` naming convention.
- The custom fragment can be combined with `CrudRepository`.
- Application code interacts with the composed repository rather than the implementation directly.
- `EntityManager` can be used when custom JPQL or other persistence logic is required.
- `@DataJpaTest` can verify the complete repository composition.
- Custom repositories should focus on data-access behavior rather than business logic.

The main idea is:

```text
Custom Repository Interface
          ↓
Custom Repository Implementation
          ↓
Repository Composition
          ↓
Main Spring Data Repository
          ↓
Application Code
```

---

## Next Example

The next example will be **Database Testing**.

It will demonstrate how Spring Data applications can test their database interactions using dedicated JPA test support and an embedded database.