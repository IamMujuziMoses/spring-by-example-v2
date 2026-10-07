package com.springbyexample.v2.databasetesting.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.IterableAssert.assertThatIterable;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

import com.springbyexample.v2.databasetesting.entity.Person;

/**
 * @author Mujuzi Moses
 */
@DataJpaTest
@AutoConfigureTestDatabase
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    void save_shouldPersistAndRetrievePerson() {
        Person person = personRepository.save(new Person("John", 30));

        assertThat(person.getId()).isNotNull();

        Person result = personRepository.findById(person.getId()).orElseThrow();

        assertThat(result.getId()).isEqualTo(person.getId());
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getAge()).isEqualTo(30);
    }

    @Test
    void findByAgeGreaterThan_shouldQueryDatabase() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));

        List<Person> people = personRepository.findByAgeGreaterThan(30);

        assertThatIterable(people).extracting(Person::getName).containsExactly("Peter");
        assertThatIterable(people).extracting(Person::getAge).containsExactly(40);
    }

    @Test
    void searchByName_shouldExecuteJpqlAgainstDatabase() {
        personRepository.save(new Person("John Doe", 30));
        personRepository.save(new Person("Jane Smith", 25));
        personRepository.save(new Person("Johnny Brown", 40));

        List<Person> people = personRepository.searchByName("john");

        assertThatIterable(people).extracting(Person::getName).containsExactlyInAnyOrder("John Doe", "Johnny Brown");
    }

    @Test
    @Sql("classpath:test-data/people.sql")
    void findAll_shouldLoadSqlTestData() {
        Iterable<Person> people = personRepository.findAll();

        assertThatIterable(people).extracting(Person::getName).containsExactlyInAnyOrder("John", "Jane");
        assertThatIterable(people).extracting(Person::getAge).containsExactlyInAnyOrder(30, 25);
    }

    @Test
    void database_shouldStartEmpty() {
        assertThat(personRepository.count()).isZero();
    }
}
