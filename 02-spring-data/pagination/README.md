# Pagination

This example demonstrates **Spring Data pagination** using `Page`, `Pageable`, `PageRequest`, and `Sort`.

Pagination allows an application to retrieve a large result set in smaller portions instead of loading every matching entity at once.

The goal is to understand how Spring Data accepts pagination requests, returns paginated results, provides pagination metadata, and combines pagination with sorting.

## What This Example Demonstrates

- What pagination is.
- Using `Pageable` with a Spring Data repository.
- Creating pagination requests with `PageRequest`.
- Understanding zero-based page numbering.
- Configuring page size.
- Sorting paginated results.
- Returning a `Page<T>` from a repository.
- Accessing paginated content.
- Reading pagination metadata.
- Checking whether previous or next pages exist.
- Detecting the last page.
- Testing pagination with `@DataJpaTest`.
- Using H2 as an embedded test database.

---

## What Is Pagination?

Pagination is the process of dividing a large collection of results into smaller pages.

For example, if a database contains five people and the requested page size is two:

```text
Page 0
-------
John
Jane

Page 1
-------
Peter
Mary

Page 2
-------
David
```

Instead of retrieving all five records at once, the application can request only the records belonging to a particular page.

Conceptually:

```text
Database
    │
    ▼
All matching records
    │
    ▼
Pagination
    │
    ├── Page 0
    ├── Page 1
    └── Page 2
```

Spring Data provides the `Pageable` abstraction for representing this request.

---

## `Pageable`

`Pageable` represents pagination information supplied to a repository query.

It can describe:

- Which page should be returned.
- How many records should be returned.
- How the records should be sorted.

For example:

```java
Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());
```

This means:

```text
Page number: 0
Page size:   2
Sort:        id ascending
```

The repository can then use the `Pageable` instance to retrieve the requested portion of the result set.

---

## `PageRequest`

`PageRequest` is a convenient implementation of `Pageable`.

A basic pagination request can be created with:

```java
PageRequest.of(0, 2);
```

The first argument is the page number and the second argument is the page size.

A request can also include sorting:

```java
PageRequest.of(0,2,Sort.by("id").ascending());
```

The simplified flow is:

```text
PageRequest
     │
     ▼
Pageable
     │
     ▼
Repository
     │
     ▼
Database
```

---

## Zero-Based Page Numbers

Spring Data uses **zero-based page numbering**.

This means:

```text
Page 0 → First page
Page 1 → Second page
Page 2 → Third page
```

For example:

```java
PageRequest.of(0, 2);
```

requests the first page.

While:

```java
PageRequest.of(1, 2);
```

requests the second page.

This is important when converting page numbers received from an application or API into Spring Data pagination requests.

---

## Repository Definition

The repository exposes a paginated `findAll()` method:

```java
public interface PersonRepository extends CrudRepository<Person, Long> {
    Page<Person> findAll(Pageable pageable);
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

The additional `findAll(Pageable pageable)` method makes the pagination operation explicit in the example.

Spring Data uses the supplied `Pageable` to determine which records should be retrieved.

---

## Returning a `Page`

The repository method returns:

```java
Page<Person>
```

A `Page` represents a single page of results together with metadata about the complete result set.

Conceptually:

```text
Page<Person>
     │
     ├── Content
     │     ├── Person
     │     └── Person
     │
     └── Pagination Metadata
           ├── Page number
           ├── Page size
           ├── Total elements
           ├── Total pages
           └── Navigation information
```

This means the application does not need to calculate the pagination information manually.

---

## Requesting the First Page

Suppose five people have been persisted:

```text
John
Jane
Peter
Mary
David
```

A page size of two can be requested with:

```java
Page<Person> page = personRepository.findAll(PageRequest.of(0, 2, Sort.by("id").ascending()));
```

The first page contains:

```text
John
Jane
```

The page metadata describes the result:

```text
Current page:  0
Page size:     2
Total records: 5
Total pages:   3
```

The test verifies the requested content:

```java
@Test
void findAll_shouldReturnRequestedPage() {
    Person john = personRepository.save(new Person("John", 30));
    Person jane = personRepository.save(new Person("Jane", 25));

    personRepository.save(new Person("Peter", 40));
    personRepository.save(new Person("Mary", 35));
    personRepository.save(new Person("David", 28));

    Page<Person> page = personRepository.findAll(PageRequest.of(0, 2, Sort.by("id").ascending()));

    assertThatIterable(page.getContent()).extracting(Person::getName).containsExactly("John", "Jane");
    assertThatIterable(page.getContent()).extracting(Person::getId).containsExactly(john.getId(), jane.getId());

    assertThat(page.getNumber()).isZero();
    assertThat(page.getSize()).isEqualTo(2);
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getTotalPages()).isEqualTo(3);
    assertThat(page.hasNext()).isTrue();
}
```

The test captures the generated IDs instead of assuming that database-generated IDs always begin at `1`.

---

## Requesting the Second Page

The second page has page number `1` because Spring Data uses zero-based page numbering.

```java
Page<Person> page = personRepository.findAll(PageRequest.of(1, 2, Sort.by("id").ascending()));
```

With five records and a page size of two:

```text
Page 0
-------
John
Jane

Page 1
-------
Peter
Mary

Page 2
-------
David
```

The second-page test verifies this behavior:

```java
@Test
void findAll_shouldReturnSecondPage() {
    personRepository.save(new Person("John", 30));
    personRepository.save(new Person("Jane", 25));

    Person peter = personRepository.save(new Person("Peter", 40));
    Person mary = personRepository.save(new Person("Mary", 35));

    personRepository.save(new Person("David", 28));

    Page<Person> page = personRepository.findAll(PageRequest.of(1, 2, Sort.by("id").ascending()));

    assertThatIterable(page.getContent()).extracting(Person::getName).containsExactly("Peter", "Mary");
    assertThatIterable(page.getContent()).extracting(Person::getId).containsExactly(peter.getId(), mary.getId());

    assertThat(page.getNumber()).isEqualTo(1);
    assertThat(page.getSize()).isEqualTo(2);
    assertThat(page.hasPrevious()).isTrue();
    assertThat(page.hasNext()).isTrue();
}
```

Because the second page has both a previous and a next page, the test verifies both navigation properties.

---

## Requesting the Last Page

The third page has page number `2`.

```java
Page<Person> page = personRepository.findAll(PageRequest.of(2, 2, Sort.by("id").ascending()));
```

Since there are five records and the page size is two, the final page contains only one record:

```text
Page 2
-------
David
```

The test verifies that the page is the final page:

```java
@Test
void findAll_shouldReturnLastPage() {
    personRepository.save(new Person("John", 30));
    personRepository.save(new Person("Jane", 25));
    personRepository.save(new Person("Peter", 40));
    personRepository.save(new Person("Mary", 35));

    Person david = personRepository.save(new Person("David", 28));

    Page<Person> page = personRepository.findAll(PageRequest.of(2, 2, Sort.by("id").ascending()));

    assertThatIterable(page.getContent()).extracting(Person::getName).containsExactly("David");
    assertThatIterable(page.getContent()).extracting(Person::getId).containsExactly(david.getId());

    assertThat(page.getNumber()).isEqualTo(2);
    assertThat(page.getSize()).isEqualTo(2);
    assertThat(page.getNumberOfElements()).isEqualTo(1);
    assertThat(page.hasPrevious()).isTrue();
    assertThat(page.hasNext()).isFalse();
    assertThat(page.isLast()).isTrue();
}
```

---

## Working with `Page`

The `Page` interface provides access to both the current page's content and pagination metadata.

Some commonly used methods are:

| Method                  | Purpose                                            |
|-------------------------|----------------------------------------------------|
| `getContent()`          | Returns the entities on the current page           |
| `getNumber()`           | Returns the current zero-based page number         |
| `getSize()`             | Returns the requested page size                    |
| `getNumberOfElements()` | Returns the number of elements on the current page |
| `getTotalElements()`    | Returns the total number of matching elements      |
| `getTotalPages()`       | Returns the total number of pages                  |
| `hasNext()`             | Checks whether another page exists                 |
| `hasPrevious()`         | Checks whether a previous page exists              |
| `isFirst()`             | Checks whether this is the first page              |
| `isLast()`              | Checks whether this is the last page               |

For example:

```java
assertThat(page.getNumber()).isEqualTo(0);
assertThat(page.getSize()).isEqualTo(2);
assertThat(page.getNumberOfElements()).isEqualTo(2);
assertThat(page.getTotalElements()).isEqualTo(5);
assertThat(page.getTotalPages()).isEqualTo(3);
assertThat(page.hasNext()).isTrue();
```

---

## `getContent()`

`getContent()` returns the entities contained in the current page:

```java
List<Person> people = page.getContent();
```

The returned content represents only the requested page, not the complete result set.

For example:

```text
Database
    │
    ├── John
    ├── Jane
    ├── Peter
    ├── Mary
    └── David
             │
             ▼
       PageRequest(0, 2)
             │
             ▼
       page.getContent()
             │
             ├── John
             └── Jane
```

---

## Pagination Metadata

For five records with a page size of two:

```text
Total elements:       5
Page size:            2
Total pages:          3
```

The `Page` object exposes this information:

```java
assertThat(page.getTotalElements()).isEqualTo(5);
assertThat(page.getTotalPages()).isEqualTo(3);
```

The number of elements on the current page can be checked separately:

```java
assertThat(page.getNumberOfElements()).isEqualTo(2);
```

On the final page:

```text
Total elements:        5
Page size:             2
Total pages:           3
Current page elements: 1
```

This is because the final page contains only the remaining record.

---

## Pagination and Sorting

Pagination can be combined with sorting:

```java
PageRequest.of(0,2,Sort.by("id").ascending());
```

The sort ensures that the records have a deterministic order.

Without an explicit ordering, applications should not assume that database results will always be returned in a particular order.

The example therefore uses:

```java
Sort.by("id").ascending()
```

to keep the pagination tests deterministic.

Conceptually:

```text
PageRequest
     │
     ├── Page number
     ├── Page size
     └── Sort
          │
          ▼
      Repository
          │
          ▼
       Database
```

---

## Why Sorting Matters for Pagination

Consider a query returning:

```text
John
Jane
Peter
Mary
David
```

If the application requests:

```text
Page size = 2
```

the records need a stable ordering for page boundaries to be predictable.

This example explicitly sorts by ID:

```java
Sort.by("id").ascending()
```

which produces:

```text
Page 0 → John, Jane
Page 1 → Peter, Mary
Page 2 → David
```

This makes the behavior easy to reason about and test.

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

The entity is intentionally simple because the focus of this example is pagination rather than entity mapping.

---

## Repository Definition

The complete repository is:

```java
public interface PersonRepository extends CrudRepository<Person, Long> {
    Page<Person> findAll(Pageable pageable);
}
```

The repository builds on the `CrudRepository` abstraction while explicitly exposing a paginated `findAll()` operation.

This keeps the pagination API visible in the example.

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

The tests use an embedded H2 database so that pagination can be tested against a real persistence layer without requiring an external database.

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

## Generated IDs in Tests

The tests intentionally avoid hard-coding generated IDs such as:

```java
assertThatIterable(page.getContent()).extracting(Person::getId).containsExactly(1L, 2L);
```

Database identity sequences can continue increasing even when test transactions roll back.

For example:

```text
Test 1 → IDs 1–5
Test 2 → IDs 6–10
Test 3 → IDs 11–15
```

The rows may be rolled back between tests while the identity sequence continues.

Instead, the tests capture the generated IDs:

```java
Person john = personRepository.save(new Person("John", 30));
Person jane = personRepository.save(new Person("Jane", 25));
```

and then assert against those values:

```java
assertThatIterable(page.getContent()).extracting(Person::getId).containsExactly(john.getId(), jane.getId());
```

This makes the tests independent of the current database identity sequence.

---

## Pagination Flow

The complete pagination flow can be viewed as:

```text
Application
     │
     ▼
PageRequest
     │
     ├── Page number
     ├── Page size
     └── Sort
     │
     ▼
Pageable
     │
     ▼
PersonRepository
     │
     ▼
Spring Data
     │
     ▼
JPA
     │
     ▼
Database
     │
     ▼
Page<Person>
     │
     ├── Content
     └── Pagination Metadata
```

---

## Running the Tests

Run the tests from the module:

```bash
mvn clean test
```

Or from the repository root:

```bash
mvn -pl 02-spring-data/pagination clean test
```

To run the complete verification lifecycle:

```bash
mvn -pl 02-spring-data/pagination clean verify
```

---

## Key Takeaways

- `Pageable` represents a pagination request.
- `PageRequest` is used to create pagination requests.
- Spring Data uses zero-based page numbering.
- `Page<T>` contains both page content and pagination metadata.
- Page size controls the maximum number of records returned.
- `getTotalElements()` returns the total number of matching records.
- `getTotalPages()` returns the total number of available pages.
- `hasNext()` determines whether another page exists.
- `hasPrevious()` determines whether a previous page exists.
- `isLast()` determines whether the current page is the final page.
- Pagination can be combined with `Sort`.
- Explicit sorting makes pagination results deterministic.
- Generated IDs should not be hard-coded in repository tests.
- `@DataJpaTest` provides focused JPA testing support.
- H2 can be used as an embedded database for pagination tests.

---

## Next Example

The next example will explore **Spring Data Specifications** and how dynamic query predicates can be built and combined for more flexible repository queries.