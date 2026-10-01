package com.springbyexample.v2.crudrepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.IterableAssert.assertThatIterable;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.springbyexample.v2.crudrepository.entity.Person;
import com.springbyexample.v2.crudrepository.repository.PersonRepository;

/**
 * @author Mujuzi Moses
 */
@DataJpaTest
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    void save_shouldPersistPerson() {
        Person person = personRepository.save(new Person("John"));

        assertThat(person.getId()).isNotNull();
        assertThat(person.getName()).isEqualTo("John");
    }

    @Test
    void findById_shouldReturnPerson() {
        Person savedPerson = personRepository.save(new Person("John"));
        Optional<Person> result = personRepository.findById(savedPerson.getId());

        assertThat(result).isPresent().get().extracting(Person::getName).isEqualTo("John");
    }

    @Test
    void findAll_shouldReturnAllPeople() {
        personRepository.save(new Person("John"));
        personRepository.save(new Person("Jane"));

        Iterable<Person> people = personRepository.findAll();

        assertThatIterable(people).extracting(Person::getName).containsExactlyInAnyOrder("John", "Jane");
    }

    @Test
    void existsById_shouldReturnTrueForExistingPerson() {
        Person savedPerson = personRepository.save(new Person("John"));

        assertThat(personRepository.existsById(savedPerson.getId())).isTrue();
    }

    @Test
    void count_shouldReturnNumberOfPeople() {
        personRepository.save(new Person("John"));
        personRepository.save(new Person("Jane"));

        assertThat(personRepository.count()).isEqualTo(2);
    }

    @Test
    void deleteById_shouldRemovePerson() {
        Person savedPerson = personRepository.save(new Person("John"));
        personRepository.deleteById(savedPerson.getId());

        assertThat(personRepository.findById(savedPerson.getId())).isEmpty();
    }

    @Test
    void delete_shouldRemovePerson() {
        Person savedPerson = personRepository.save(new Person("John"));
        personRepository.delete(savedPerson);

        assertThat(personRepository.findById(savedPerson.getId())).isEmpty();
    }
}
