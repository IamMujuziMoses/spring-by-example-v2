# Entity Mapping

This example demonstrates how **Spring Data JPA** maps Java classes and fields to database tables and columns using JPA mapping annotations.

The example focuses on basic entity-to-table and field-to-column mapping before introducing relationships between entities.

## What This Example Demonstrates

- `@Entity`
- `@Table`
- `@Id`
- `@GeneratedValue`
- `@Column`
- Custom table names
- Custom column names
- Column constraints
- `nullable`
- `length`
- Entity persistence
- Entity retrieval
- Repository testing with `@DataJpaTest`

---

## What Is Entity Mapping?

Entity mapping is the process of defining how a Java object corresponds to a database record.

JPA provides annotations that describe this relationship.

For example:

```java
@Entity
@Table(name = "people")
public class Person {
    ...
}
```

tells JPA that the `Person` Java class represents an entity that should be persisted using the `people` database table.

A simplified mapping looks like:

```text
Java Entity                     Database Table

Person                          people
  │                               │
  ├── id       ────────────────→  id
  │
  ├── name     ────────────────→  full_name
  │
  └── age      ────────────────→  age
```

JPA uses this mapping information when creating SQL statements and converting database rows back into Java objects.

---

## `@Entity`

The `@Entity` annotation identifies a Java class as a JPA entity.

```java
@Entity
public class Person {
    ...
}
```

Once a class is an entity, JPA can manage instances of that class and persist them to a database.

An entity normally represents a concept in the application's domain.

In this example:

```text
Person
```

represents a person stored in the database.

---

## `@Table`

The `@Table` annotation allows the database table name to be specified explicitly.

```java
@Entity
@Table(name = "people")
public class Person {
    ...
}
```

This maps:

```text
Person
```

to:

```text
people
```

Without `@Table`, JPA can determine a default table name according to its naming strategy.

Using `@Table` is useful when the desired table name needs to be explicit.

---

## `@Id`

Every JPA entity must have an identifier.

The `@Id` annotation identifies the entity's primary key:

```java
@Id
private Long id;
```

The identifier allows JPA to distinguish one entity instance from another.

For example:

```text
Person
────────────────────
id = 1
name = John Doe
age = 30

Person
────────────────────
id = 2
name = Jane Doe
age = 25
```

The `id` uniquely identifies each row.

---

## `@GeneratedValue`

The `@GeneratedValue` annotation specifies how the entity identifier should be generated.

This example uses:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

With `GenerationType.IDENTITY`, the database is responsible for generating the identifier.

For example:

```text
save(Person)
     ↓
Database generates ID
     ↓
Person receives generated ID
```

This means application code does not need to manually assign the identifier.

---

## `@Column`

The `@Column` annotation controls how an entity field is mapped to a database column.

For example:

```java
@Column(name = "full_name", nullable = false, length = 100)
private String name;
```

This defines several pieces of mapping information:

```text
Java property: name
Database column: full_name
Nullable: false
Maximum length: 100
```

---

## Custom Column Names

By default, JPA can derive a database column name from the Java property name.

This example explicitly specifies a different column name:

```java
@Column(name = "full_name")
private String name;
```

The mapping becomes:

```text
Person.name
    ↓
people.full_name
```

This demonstrates that Java property names do not have to match database column names.

---

## `nullable`

The `nullable` attribute controls whether the database column should allow `NULL` values.

For example:

```java
@Column(nullable = false)
private int age;
```

means that the column should not allow null values.

The name mapping uses the same constraint:

```java
@Column(name = "full_name", nullable = false, length = 100)
private String name;
```

The `full_name` column is therefore mapped as non-nullable.

---

## `length`

The `length` attribute can be used to specify the expected length of a string column.

For example:

```java
@Column(name = "full_name", length = 100)
private String name;
```

This communicates that the column should support values up to 100 characters.

For a string property:

```text
Java String
     ↓
full_name VARCHAR(100)
```

The exact SQL type can depend on the database and JPA provider.

---

## Complete Entity

Putting the mappings together:

```java
@Entity
@Table(name = "people")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
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

The complete mapping can be visualized as:

```text
┌──────────────────────────────┐
│ Person Java Entity           │
├──────────────────────────────┤
│ id : Long                    │
│ name : String                │
│ age : int                    │
└──────────────┬───────────────┘
               │
               │ JPA Mapping
               ↓
┌──────────────────────────────┐
│ people Database Table        │
├──────────────────────────────┤
│ id                            │
│ full_name                     │
│ age                           │
└──────────────────────────────┘
```

---

## Repository

The example uses a simple Spring Data repository:

```java
public interface PersonRepository extends CrudRepository<Person, Long> {
}
```

No custom repository methods are required.

The purpose of this example is to demonstrate entity mapping, while `CrudRepository` provides the persistence operations used by the tests.

---

## Saving a Mapped Entity

The repository can persist a `Person`:

```java
Person person =
        personRepository.save(new Person("John Doe", 30));
```

When the entity is saved, JPA uses the mapping metadata to determine how the object should be persisted.

Conceptually:

```text
Person object
     ↓
JPA entity mapping
     ↓
people table
     ↓
Database row
```

The database-generated ID is then assigned to the entity.

---

## Retrieving a Mapped Entity

The entity can be retrieved using its generated identifier:

```java
Person person = personRepository.findById(savedPerson.getId()).orElseThrow();
```

JPA uses the entity mapping to convert the database row back into a `Person` object.

Conceptually:

```text
Database row
     ↓
JPA entity mapping
     ↓
Person object
```

---

## Entity Mapping Flow

The overall persistence flow can be simplified as:

```text
Java Object
     │
     │ @Entity
     │ @Table
     │ @Column
     ↓
JPA Entity Metadata
     │
     ↓
JPA Provider
     │
     ↓
SQL
     │
     ↓
Database Table
```

When reading data:

```text
Database Table
     │
     ↓
SQL Result
     │
     ↓
JPA Provider
     │
     ↓
Entity Mapping
     │
     ↓
Java Object
```

---

## Common Mapping Annotations

This example introduces several fundamental JPA annotations:

| Annotation        | Purpose                               |
|-------------------|---------------------------------------|
| `@Entity`         | Marks a class as a JPA entity         |
| `@Table`          | Configures the database table mapping |
| `@Id`             | Identifies the entity's primary key   |
| `@GeneratedValue` | Configures identifier generation      |
| `@Column`         | Configures a field-to-column mapping  |

These annotations form the foundation of JPA entity mapping.

---

## Why Entity Mapping Matters

Spring Data repositories operate on domain entities.

For example:

```java
Person
```

is a Java representation of data stored in the database.

The mapping tells JPA how these two worlds relate:

```text
Object-Oriented Model       Relational Model

Person                 →    people
id                     →    id
name                   →    full_name
age                    →    age
```

Understanding this mapping becomes especially important when working with:

- Existing databases
- Legacy schemas
- Custom table names
- Custom column names
- Database constraints
- Relationships
- Complex domain models

---

## What This Example Does Not Cover

This example intentionally focuses on basic entity mapping.

It does not cover:

- Entity relationships
- `@OneToOne`
- `@OneToMany`
- `@ManyToOne`
- `@ManyToMany`
- Embedded objects
- Inheritance mapping
- Enumerations
- Custom converters
- Composite identifiers
- Entity lifecycle callbacks

Relationships will be explored in the next example.

---

## Running the Tests

From the project root:

```bash
mvn -pl 02-spring-data/entity-mapping clean verify
```

Or run the complete project test suite:

```bash
mvn clean verify
```

---

## Key Takeaways

- `@Entity` marks a Java class as a JPA entity.
- `@Table` configures the entity's database table.
- `@Id` identifies the entity's primary key.
- `@GeneratedValue` configures automatic ID generation.
- `@Column` controls field-to-column mapping.
- Java property names do not have to match database column names.
- `nullable` and `length` provide column mapping constraints.
- JPA uses mapping metadata to translate between Java objects and database rows.
- Spring Data repositories can persist and retrieve mapped entities without manually writing SQL.
- Entity mapping is the foundation for more advanced JPA features such as relationships.

---

## Next Example

The next example will explore **Entity Relationships** and how JPA maps associations between entities using annotations.
