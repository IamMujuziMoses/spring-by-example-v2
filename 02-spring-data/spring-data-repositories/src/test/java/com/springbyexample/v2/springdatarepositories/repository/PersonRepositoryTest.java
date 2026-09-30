package com.springbyexample.v2.springdatarepositories.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.springbyexample.v2.springdatarepositories.entity.Person;


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
    void deleteById_shouldRemovePerson() {
        Person savedPerson = personRepository.save(new Person("John"));
        personRepository.deleteById(savedPerson.getId());

        assertThat(personRepository.findById(savedPerson.getId())).isEmpty();
    }
}
