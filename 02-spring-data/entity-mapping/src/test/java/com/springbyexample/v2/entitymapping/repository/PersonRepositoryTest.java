package com.springbyexample.v2.entitymapping.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.springbyexample.v2.entitymapping.entity.Person;

/**
 * @author Mujuzi Moses
 */
@DataJpaTest
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    void save_shouldPersistMappedEntity() {
        Person person = personRepository.save(new Person("John Doe", 30));

        assertThat(person.getId()).isNotNull();
        assertThat(person.getName()).isEqualTo("John Doe");
        assertThat(person.getAge()).isEqualTo(30);
    }

    @Test
    void findById_shouldReturnMappedEntity() {
        Person savedPerson = personRepository.save(new Person("John Doe", 30));
        Person person = personRepository.findById(savedPerson.getId()).orElseThrow();

        assertThat(person.getId()).isEqualTo(savedPerson.getId());
        assertThat(person.getName()).isEqualTo("John Doe");
        assertThat(person.getAge()).isEqualTo(30);
    }
}
