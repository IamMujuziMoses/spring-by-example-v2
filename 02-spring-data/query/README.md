# @Query

This example demonstrates how **Spring Data JPA** allows repository queries to be explicitly defined using `@Query`.

While Spring Data can derive queries from repository method names, some queries are easier to express by writing the query explicitly.

This example focuses on **JPQL**, named parameters, and explicit repository queries.

## What This Example Demonstrates

- `@Query`
- JPQL
- `@Param`
- Named query parameters
- Explicit repository queries
- `LIKE`
- Comparison operators
- Repository testing with `@DataJpaTest`

---

## What Is `@Query`?

Spring Data repository interfaces can define queries explicitly using the `@Query` annotation.

For example:

```java
@Query("select p from Person p where p.name = :name")
List<Person> findPeopleByName(@Param("name") String name);
```

Instead of deriving the query from the method name, Spring Data uses the JPQL provided by `@Query`.

Conceptually, the flow is:

```text
Repository Method
       ↓
@Query
       ↓
JPQL
       ↓
JPA
       ↓
Database
       ↓
Entities
```

---

## `@Query` vs Query Methods

The previous **Query Methods** example demonstrated derived queries:

```java
List<Person> findByName(String name);
```

Spring Data interprets the method name:

```text
findByName
```

and derives the appropriate query.

With `@Query`, the query is explicitly defined:

```java
@Query("select p from Person p where p.name = :name")
List<Person> findPeopleByName(@Param("name") String name);
```

The difference can be summarized as:

| Approach     | Query Definition              |
|--------------|-------------------------------|
| Query Method | Derived from method name      |
| `@Query`     | Explicitly defined using JPQL |

Query methods are convenient for straightforward queries, while `@Query` provides more control over the query itself.

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

These properties are referenced by the JPQL queries in the repository.

---

## Repository

The repository extends `CrudRepository`:

```java
public interface PersonRepository extends CrudRepository<Person, Long> {

    @Query("select p from Person p where p.id = :id")
    List<Person> findPeopleById(@Param("id") String id);
    
    @Query("select p from Person p where p.name = :name")
    List<Person> findPeopleByName(@Param("name") String name);

    @Query("select p from Person p where p.name like %:text%")
    List<Person> searchByName(@Param("text") String text);

    @Query("select p from Person p where p.age > :age")
    List<Person> findPeopleOlderThan(@Param("age") int age);
}
```

Each method demonstrates a different use of `@Query`.

---

## JPQL

The queries in this example use **JPQL**, or Java Persistence Query Language.

JPQL operates on **entities and their properties**, rather than directly operating on database tables and columns.

For example:

```java
@Query("select p from Person p where p.name = :name")
```

references:

```text
Person
p.name
```

rather than:

```text
person
name
```

This is an important distinction between JPQL and SQL.

Conceptually, the JPQL:

```sql
select p from Person p where p.name = :name
```

is translated by JPA into SQL appropriate for the underlying database.

---

## Selecting an Entity

The query:

```java
@Query("select p from Person p where p.name = :name")
```

selects `Person` entities.

The `p` is an alias:

```text
Person p
```

The query can then reference the entity through that alias:

```text
p.name
```

The complete query means:

```text
Select Person entities
where the Person name matches the supplied parameter
```

---

## Named Parameters

The example uses named parameters:

```java
@Query("select p from Person p where p.name = :name")
List<Person> findPeopleByName(@Param("name") String name);
```

The parameter is declared in the JPQL query using:

```text
:name
```

and connected to the repository method parameter using:

```java
@Param("name")
```

The relationship is:

```text
JPQL parameter
      ↓
:name
      ↓
@Param("name")
      ↓
String name
```

This makes the relationship between the query and method parameter explicit.

---

## Finding People by Name

The first query finds people whose name matches the supplied value:

```java
@Query("select p from Person p where p.name = :name")
List<Person> findPeopleByName(@Param("name") String name);
```

It can be used as:

```java
personRepository.findPeopleByName("John");
```

Conceptually, the query represents:

```sql
SELECT * FROM person WHERE name = 'John';
```

The actual SQL is generated by the JPA provider.

### Test

```java
@Test
void findPeopleByName_shouldReturnMatchingPeople() {
    personRepository.save(new Person("John", 30));
    personRepository.save(new Person("Jane", 25));

    List<Person> people = personRepository.findPeopleByName("John");

    assertThat(people).extracting(Person::getName).containsExactly("John");
}
```

The test verifies that the explicitly defined JPQL query returns the expected entity.

---

## Searching with `LIKE`

The second query demonstrates `LIKE`:

```java
@Query("select p from Person p where p.name like %:text%")
List<Person> searchByName(@Param("text") String text);
```

For example:

```java
personRepository.searchByName("John");
```

can match:

```text
John
Johnny
Johnathan
```

Conceptually, this represents a pattern such as:

```sql
SELECT * FROM person WHERE name LIKE '%John%';
```

### Test

```java
@Test
void searchByName_shouldReturnPeopleContainingText() {
    personRepository.save(new Person("John", 30));
    personRepository.save(new Person("Johnny", 35));
    personRepository.save(new Person("Jane", 25));

    List<Person> people = personRepository.searchByName("John");

    assertThat(people).extracting(Person::getName).containsExactlyInAnyOrder("John", "Johnny");
}
```

The test confirms that the query can find multiple entities containing the supplied text.

---

## Comparison Queries

`@Query` can also be used for comparison operations.

For example:

```java
@Query("select p from Person p where p.age > :age")
List<Person> findPeopleOlderThan(@Param("age") int age);
```

Calling:

```java
personRepository.findPeopleOlderThan(30);
```

finds people whose age is greater than `30`.

Conceptually:

```sql
SELECT * FROM person WHERE age > 30;
```

### Test

```java
@Test
void findPeopleOlderThan_shouldReturnPeopleAboveAge() {
    personRepository.save(new Person("John", 30));
    personRepository.save(new Person("Jane", 25));
    personRepository.save(new Person("Peter", 40));

    List<Person> people = personRepository.findPeopleOlderThan(30);

    assertThat(people).extracting(Person::getName).containsExactly("Peter");
    assertThat(people).extracting(Person::getAge).containsExactly(40);
}
```

---

## Query Parameters

Parameters can be represented in JPQL in different ways.

This example uses named parameters:

```java
@Query("select p from Person p where p.name = :name")
```

with:

```java
@Param("name")
```

Named parameters are useful because the relationship between the query and method parameter is clear.

For example:

```text
:name
  ↓
@Param("name")
  ↓
String name
```

This is generally easier to understand than relying on parameter positions.

---

## JPQL vs SQL

One of the important concepts demonstrated by this example is that `@Query` does not necessarily mean writing SQL.

The queries in this example are JPQL:

```java
@Query("select p from Person p where p.name = :name")
```

JPQL works with the entity model.

For example:

```text
Person
p.name
p.age
```

SQL, on the other hand, works with database structures such as:

```text
person
name
age
```

JPA translates JPQL into database-specific SQL.

The simplified flow is:

```text
JPQL
  ↓
JPA Provider
  ↓
SQL
  ↓
Database
```

---

## `@Query` and Query Method Derivation

The same basic query can often be expressed using a query method.

For example:

```java
List<Person> findByName(String name);
```

or explicitly:

```java
@Query("select p from Person p where p.name = :name")
List<Person> findPeopleByName(@Param("name") String name);
```

Both can retrieve people by name.

The difference is how the query is defined.

### Query Method

```text
Method Name
     ↓
Spring Data derives query
     ↓
Query execution
```

### `@Query`

```text
@Query
  ↓
Explicit JPQL
  ↓
Query execution
```

---

## When Should `@Query` Be Used?

`@Query` is useful when a query cannot be expressed clearly or conveniently through a repository method name.

For example, simple queries such as:

```java
findByName(String name)
```

are easy to express using query derivation.

More complex queries may be clearer when written explicitly:

```java
@Query("""
        select p
        from Person p
        where p.name like %:text%
        and p.age > :age
        """)
List<Person> search(
        @Param("text") String text,
        @Param("age") int age);
```

Explicit queries can make complex query logic easier to understand than very long method names.

---

## Testing

The repository is tested using:

```java
@DataJpaTest
```

This provides a focused Spring test environment for JPA repositories.

The tests use the H2 database.

For example:

```java
@DataJpaTest
class PersonRepositoryTest {
```

Each test stores sample entities and executes the corresponding repository query.

---

## Running the Tests

From the project root:

```bash
mvn -pl 02-spring-data/query clean verify
```

Or run the complete project test suite:

```bash
mvn clean verify
```

---

## Key Takeaways

- `@Query` allows repository queries to be explicitly defined.
- The example uses JPQL rather than native SQL.
- JPQL operates on entities and their properties.
- `@Param` binds method parameters to named JPQL parameters.
- `LIKE` can be used for pattern matching.
- JPQL supports comparison operators such as `>`.
- `@Query` provides more control than query method derivation.
- Simple queries can often be expressed more concisely using query methods.
- Explicit queries become useful when repository query logic becomes more complex.
- `@DataJpaTest` provides a focused environment for testing repository queries.

---

## Query Flow

The complete flow for this example can be summarized as:

```text
Repository Method
       ↓
     @Query
       ↓
      JPQL
       ↓
   JPA Provider
       ↓
       SQL
       ↓
    Database
       ↓
     Entity
```

---

## Next Example

The next example will explore **Entity Mapping** and how JPA maps Java entities and their properties to database tables and columns.
