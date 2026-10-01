package com.springbyexample.v2.querymethods;

import static org.assertj.core.api.IterableAssert.assertThatIterable;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.springbyexample.v2.querymethods.entity.Person;
import com.springbyexample.v2.querymethods.repository.PersonRepository;

/**
 * @author Mujuzi Moses
 */
@DataJpaTest
class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    void findByName_shouldReturnPeopleWithMatchingName() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));

        List<Person> people = personRepository.findByName("John");

        assertThatIterable(people).extracting(Person::getName).containsExactly("John");
    }

    @Test
    void findByNameContaining_shouldReturnPeopleContainingText() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Johnny", 35));
        personRepository.save(new Person("Jane", 25));

        List<Person> people = personRepository.findByNameContaining("John");

        assertThatIterable(people).extracting(Person::getName).containsExactlyInAnyOrder("John", "Johnny");
    }

    @Test
    void findByNameStartingWith_shouldReturnPeopleStartingWithPrefix() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Johnny", 35));
        personRepository.save(new Person("Jane", 25));

        List<Person> people = personRepository.findByNameStartingWith("Jo");

        assertThatIterable(people).extracting(Person::getName).containsExactlyInAnyOrder("John", "Johnny");
    }

    @Test
    void findByAgeGreaterThan_shouldReturnPeopleAboveAge() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));

        List<Person> people = personRepository.findByAgeGreaterThan(30);

        assertThatIterable(people).extracting(Person::getName).containsExactly("Peter");
    }

    @Test
    void findByAgeLessThan_shouldReturnPeopleBelowAge() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));

        List<Person> people = personRepository.findByAgeLessThan(30);

        assertThatIterable(people).extracting(Person::getName).containsExactly("Jane");
    }

    @Test
    void findByAgeBetween_shouldReturnPeopleWithinAgeRange() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));

        List<Person> people = personRepository.findByAgeBetween(25, 35);

        assertThatIterable(people).extracting(Person::getName).containsExactly("John", "Jane");
    }
}
