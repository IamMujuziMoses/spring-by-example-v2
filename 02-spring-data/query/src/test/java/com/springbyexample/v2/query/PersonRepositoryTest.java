package com.springbyexample.v2.query;

import static org.assertj.core.api.IterableAssert.assertThatIterable;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.springbyexample.v2.query.entity.Person;
import com.springbyexample.v2.query.repository.PersonRepository;

/**
 * @author Mujuzi Moses
 */
@DataJpaTest
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    void findPeopleById_shouldReturnMatchingPeople() {
        Person john = personRepository.save(new Person("John", 30));
        Person jane = personRepository.save(new Person("Jane", 25));

        List<Person> people = personRepository.findPeopleById(jane.getId());

        assertThatIterable(people).extracting(Person::getName).containsExactly("Jane");
        assertThatIterable(people).extracting(Person::getId).containsExactly(jane.getId());
    }

    @Test
    void findPeopleByName_shouldReturnMatchingPeople() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));

        List<Person> people = personRepository.findPeopleByName("John");

        assertThatIterable(people).extracting(Person::getName).containsExactly("John");
    }


    @Test
    void searchByName_shouldReturnPeopleContainingText() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Johnny", 35));
        personRepository.save(new Person("Jane", 25));

        List<Person> people = personRepository.searchByName("John");

        assertThatIterable(people).extracting(Person::getName).containsExactlyInAnyOrder("John", "Johnny");
    }

    @Test
    void findPeopleOlderThan_shouldReturnPeopleAboveAge() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));

        List<Person> people = personRepository.findPeopleOlderThan(30);

        assertThatIterable(people).extracting(Person::getName).containsExactly("Peter");
        assertThatIterable(people).extracting(Person::getAge).containsExactly(40);
    }
}
