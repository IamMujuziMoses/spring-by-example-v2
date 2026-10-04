# Entity Relationships

This example demonstrates how **Spring Data JPA** and **Jakarta Persistence** model relationships between entities.

The example focuses on the four fundamental JPA relationship types:

- `@OneToOne`
- `@OneToMany`
- `@ManyToOne`
- `@ManyToMany`

The goal is to understand how related entities are mapped, persisted, and retrieved through a Spring Data repository.

## What This Example Demonstrates

- Mapping a one-to-one relationship
- Mapping a one-to-many relationship
- Mapping a many-to-one relationship
- Mapping a many-to-many relationship
- Using `@JoinColumn`
- Using `@JoinTable`
- Using `mappedBy`
- Understanding the owning side of a relationship
- Maintaining both sides of a bidirectional relationship
- Persisting related entities
- Retrieving related entities
- Testing entity relationships with `@DataJpaTest`

---

## Domain Model

The example uses four entities:

```text
Person
 ├── @OneToOne ─────── Address
 ├── @OneToMany ────── PhoneNumber
 └── @ManyToMany ───── Role

PhoneNumber
 └── @ManyToOne ────── Person
```

This allows the example to demonstrate all four fundamental relationship types without introducing unrelated persistence features.

---

## What Are Entity Relationships?

In a relational database, data is commonly distributed across multiple tables.

For example:

```text
people
    |
    +---- addresses
    |
    +---- phone_numbers
    |
    +---- person_role ---- roles
```

JPA allows these relationships to be represented as Java object relationships.

Instead of manually joining tables in application code, an entity can reference another entity directly:

```java
person.getAddress();
```

or:

```java
person.getPhoneNumbers();
```

or:

```java
person.getRoles();
```

JPA translates these object relationships into the appropriate database operations.

---

## `@OneToOne`

A one-to-one relationship means that one entity is associated with one other entity.

In this example:

```text
Person ───────── Address
```

A `Person` has one `Address`.

### Mapping

The relationship is defined in `Person`:

```java
@OneToOne
@JoinColumn(name = "address_id")
private Address address;
```

The `@JoinColumn` tells JPA that the `Person` table contains a foreign key column named:

```text
address_id
```

Conceptually, the database relationship looks like:

```text
people
+----+-----------+------------+
| id | name      | address_id |
+----+-----------+------------+
| 1  | John Doe  | 1          |
+----+-----------+------------+

addresses
+----+---------+---------+
| id | city    | country |
+----+---------+---------+
| 1  | Kampala | Uganda  |
+----+---------+---------+
```

### Setting the Relationship

The relationship can be established by assigning an address:

```java
Address address = new Address("Kampala", "Uganda");

Person person = new Person("John Doe");
person.setAddress(address);
```

The person can then access the address through:

```java
person.getAddress();
```

### Testing

The test verifies that the relationship survives persistence and retrieval:

```java
Address address = addressRepository.save(new Address("Kampala", "Uganda"));

Person person = new Person("John Doe");
person.setAddress(address);

Person savedPerson = personRepository.save(person);
Person result = personRepository.findById(savedPerson.getId()).orElseThrow();

assertThat(result.getAddress()).isNotNull();
assertThat(result.getAddress().getId()).isEqualTo(address.getId());
```

---

## `@OneToMany`

A one-to-many relationship means that one entity is associated with multiple entities.

In this example:

```text
Person ─────────< PhoneNumber
```

A person can have multiple phone numbers.

For example:

```text
John Doe
 ├── +256700000001
 └── +256700000002
```

### Mapping

The `Person` entity contains:

```java
@OneToMany(mappedBy = "person")
private List<PhoneNumber> phoneNumbers = new ArrayList<>();
```

The `mappedBy` attribute refers to the field that owns the relationship in `PhoneNumber`:

```java
@ManyToOne
private Person person;
```

This makes the relationship bidirectional.

### Adding a Phone Number

The `Person` entity provides a helper method:

```java
public void addPhoneNumber(PhoneNumber phoneNumber) {
    phoneNumbers.add(phoneNumber);
    phoneNumber.setPerson(this);
}
```

Notice that two things happen:

1. The phone number is added to the person's collection.
2. The phone number is associated with the person.

This keeps both sides of the bidirectional relationship synchronized.

### Testing

```java
Person person = personRepository.save(new Person("John Doe"));

PhoneNumber firstNumber = new PhoneNumber("+256700000001");
PhoneNumber secondNumber = new PhoneNumber("+256700000002");

person.addPhoneNumber(firstNumber);
person.addPhoneNumber(secondNumber);

phoneNumberRepository.save(firstNumber);
phoneNumberRepository.save(secondNumber);

Person result = personRepository.findById(person.getId()).orElseThrow();

assertThatIterable(result.getPhoneNumbers()).extracting(PhoneNumber::getNumber)
        .containsExactlyInAnyOrder("+256700000001", "+256700000002");
```

---

## `@ManyToOne`

A many-to-one relationship is the inverse perspective of the one-to-many relationship.

In this example:

```text
PhoneNumber ─────────> Person
```

Multiple phone numbers can belong to one person.

For example:

```text
+256700000001 ──┐
                ├──> John Doe
+256700000002 ──┘
```

### Mapping

The `PhoneNumber` entity contains:

```java
@ManyToOne
private Person person;
```

This is the owning side of the relationship.

The corresponding `Person` mapping uses:

```java
@OneToMany(mappedBy = "person")
private List<PhoneNumber> phoneNumbers = new ArrayList<>();
```

The value of `mappedBy` matches the `person` field in `PhoneNumber`.

### Setting the Relationship

```java
PhoneNumber phoneNumber = new PhoneNumber("+256700000001");

phoneNumber.setPerson(person);
```

### Testing

```java
PhoneNumber savedPhoneNumber = phoneNumberRepository.save(phoneNumber);
PhoneNumber result = phoneNumberRepository.findById(savedPhoneNumber.getId()).orElseThrow();

assertThat(result.getPerson()).isNotNull();
assertThat(result.getPerson().getId()).isEqualTo(person.getId());
assertThat(result.getPerson().getName()).isEqualTo("John Doe");
```

The test also demonstrates that the associated `Person` can be navigated from the `PhoneNumber`.

---

## `@ManyToMany`

A many-to-many relationship means that multiple instances of one entity can be associated with multiple instances of another entity.

In this example:

```text
Person >────────< Role
```

A person can have multiple roles, and a role can be associated with multiple people.

For example:

```text
John Doe
 ├── Developer
 └── Administrator

Jane Doe
 ├── Developer
 └── Manager
```

### Mapping

The `Person` entity uses:

```java
@ManyToMany
@JoinTable(name = "person_role", joinColumns = @JoinColumn(name = "person_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
private List<Role> roles = new ArrayList<>();
```

Unlike the previous relationships, a many-to-many relationship requires an intermediate join table.

The resulting database structure is conceptually:

```text
people
+----+----------+
| id | name     |
+----+----------+
| 1  | John Doe |
+----+----------+

roles
+----+---------------+
| id | name          |
+----+---------------+
| 1  | Developer     |
| 2  | Administrator |
+----+---------------+

person_role
+-----------+---------+
| person_id | role_id |
+-----------+---------+
| 1         | 1       |
| 1         | 2       |
+-----------+---------+
```

### Adding Roles

The `Person` entity provides:

```java
public void addRole(Role role) {
    roles.add(role);
}
```

A person can then be assigned multiple roles:

```java
Role developer = new Role("Developer");
Role administrator = new Role("Administrator");

Person person = new Person("John Doe");

person.addRole(developer);
person.addRole(administrator);
```

### Testing

The relationship test verifies both the role names and the persisted role IDs:

```java
assertThatIterable(result.getRoles()).extracting(Role::getName)
        .containsExactlyInAnyOrder("Developer","Administrator");
assertThatIterable(result.getRoles()).extracting(Role::getId)
        .containsExactlyInAnyOrder(developer.getId(),administrator.getId());
```

The ID assertion is useful because it verifies that the actual persisted `Role` entities are associated with the `Person`, rather than merely verifying their names.

---

## Owning Side and `mappedBy`

Understanding the owning side is important when working with bidirectional relationships.

For the `Person` and `PhoneNumber` relationship:

```java
@OneToMany(mappedBy = "person")
private List<PhoneNumber> phoneNumbers;
```

`Person` is the inverse side.

The owning side is:

```java
@ManyToOne
private Person person;
```

The `mappedBy` value:

```text
person
```

refers to the field in `PhoneNumber`.

Conceptually:

```text
Person
  |
  | inverse side
  |
  ↓
phoneNumbers

PhoneNumber
  |
  | owning side
  |
  ↓
person
```

The owning side is responsible for the relationship's foreign-key mapping.

---

## `@JoinColumn`

`@JoinColumn` identifies the database column used to associate entities.

The one-to-one mapping uses:

```java
@JoinColumn(name = "address_id")
```

This means the `Person` table contains a column called:

```text
address_id
```

which references the associated `Address`.

---

## `@JoinTable`

Many-to-many relationships generally require an intermediate table.

This example explicitly defines one:

```java
@JoinTable(name = "person_role", joinColumns = @JoinColumn(name = "person_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
```

The resulting join table contains:

```text
person_role
+-----------+---------+
| person_id | role_id |
+-----------+---------+
```

This table connects rows from `people` and `roles`.

---

## Relationship Summary

| Annotation    | Relationship                  | Example                |
|---------------|-------------------------------|------------------------|
| `@OneToOne`   | One entity → one entity       | `Person → Address`     |
| `@OneToMany`  | One entity → many entities    | `Person → PhoneNumber` |
| `@ManyToOne`  | Many entities → one entity    | `PhoneNumber → Person` |
| `@ManyToMany` | Many entities ↔ many entities | `Person ↔ Role`        |

---

## Relationship Direction

Relationships can be unidirectional or bidirectional.

### Unidirectional

Only one entity knows about the other:

```text
Person ─────> Address
```

The `Person` can access the `Address`, but the `Address` does not reference the `Person`.

### Bidirectional

Both entities reference each other:

```text
Person <────> PhoneNumber
```

`Person` has:

```java
List<PhoneNumber> phoneNumbers;
```

and `PhoneNumber` has:

```java
Person person;
```

Bidirectional relationships can be useful, but they require care when keeping both sides synchronized.

---

## Cascade and Orphan Removal

This example intentionally does **not** configure:

```java
cascade = ...
```

or:

```java
orphanRemoval = true
```

These features control entity lifecycle behavior and are separate concepts from relationship mapping.

For example:

```text
Relationship Mapping
        +
Cascade
        +
Orphan Removal
```

are related concepts, but they answer different questions.

This example focuses specifically on **how entities are related**.

---

## Testing Entity Relationships

The example uses Spring Boot's `@DataJpaTest`:

```java
@DataJpaTest
class PersonRepositoryTest {
}
```

This provides a focused test environment for JPA-related components.

The tests verify that relationships can be:

1. Created
2. Persisted
3. Retrieved
4. Navigated through the entity model

For example:

```text
Create entities
      ↓
Establish relationship
      ↓
Persist entities
      ↓
Load entity from repository
      ↓
Navigate relationship
      ↓
Assert related entity
```

---

## Repository

The repositories use Spring Data's `CrudRepository`:

```java
public interface PersonRepository extends CrudRepository<Person, Long> {
}
```

The other entities also have repositories:

```text
PersonRepository
AddressRepository
PhoneNumberRepository
RoleRepository
```

These repositories allow the tests to persist and retrieve the entities without manually implementing data-access code.

---

## What This Example Does Not Cover

This example intentionally focuses on basic relationship mapping.

The following topics are not covered here:

- Cascade types
- `orphanRemoval`
- Lazy vs eager fetching
- Fetch joins
- Entity graphs
- Relationship ownership beyond the basic `mappedBy` example
- Transaction propagation
- Advanced join-table configuration
- Composite keys
- Ordered relationships
- Collection-specific mappings

These concepts can be explored independently without making the fundamental relationship mappings harder to understand.

---

## Running the Tests

From the project root:

```bash
mvn -pl 02-spring-data/relationships clean verify
```

Or run the entire project:

```bash
mvn clean verify
```

---

## Key Takeaways

- JPA provides annotations for modelling relationships between entities.
- `@OneToOne` represents one-to-one relationships.
- `@OneToMany` represents one-to-many relationships.
- `@ManyToOne` represents many-to-one relationships.
- `@ManyToMany` represents many-to-many relationships.
- `@JoinColumn` defines a relationship column.
- `@JoinTable` defines an intermediate join table.
- `mappedBy` identifies the inverse side of a bidirectional relationship.
- The owning side controls the relationship mapping.
- Bidirectional relationships should keep both sides synchronized.
- Relationship mapping is separate from cascade and orphan-removal behavior.
- Spring Data repositories can be used to persist and retrieve entities with relationships.
- `@DataJpaTest` provides a focused way to test JPA relationships.

The key idea is:

```text
Java Object Relationships
          ↓
       JPA Mapping
          ↓
   Relational Database
          ↓
 Spring Data Repository
          ↓
 Persisted Object Graph
```

Understanding these mappings provides the foundation for more advanced Spring Data topics such as transactions, pagination, specifications, projections, auditing, and custom repositories.

## Next Example

The next example will explore **Transactions** and how Spring manages transactional boundaries around database operations.