package com.springbyexample.v2.customrepositories.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.IterableAssert.assertThatIterable;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.springbyexample.v2.customrepositories.entity.Person;

/**
 * @author Mujuzi Moses
 */
@DataJpaTest
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    void save_shouldPersistPerson() {
        Person person = personRepository.save(new Person("John", 30));

        assertThat(person.getId()).isNotNull();
        assertThat(person.getName()).isEqualTo("John");
        assertThat(person.getAge()).isEqualTo(30);
    }

    @Test
    void search_shouldFindPeopleByName() {
        personRepository.save(new Person("John Doe", 30));
        personRepository.save(new Person("Jane Smith", 25));
        personRepository.save(new Person("Johnny Brown", 40));

        List<Person> people = personRepository.search("john");

        assertThatIterable(people).extracting(Person::getName).containsExactlyInAnyOrder("John Doe", "Johnny Brown");
    }

    @Test
    void search_shouldBeCaseInsensitive() {
        personRepository.save(new Person("John Doe", 30));
        personRepository.save(new Person("Jane Smith", 25));

        List<Person> people = personRepository.search("JOHN");

        assertThatIterable(people).extracting(Person::getName).containsExactly("John Doe");
    }

    @Test
    void search_shouldReturnEmptyResultWhenNothingMatches() {
        personRepository.save(new Person("John Doe", 30));

        List<Person> people = personRepository.search("Peter");

        assertThatIterable(people).isEmpty();
    }
}
