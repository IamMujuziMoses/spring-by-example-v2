package com.springbyexample.v2.auditing.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.springbyexample.v2.auditing.config.AuditingConfig;
import com.springbyexample.v2.auditing.entity.Person;

/**
 * @author Mujuzi Moses
 */
@DataJpaTest
@Import(AuditingConfig.class)
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    void save_shouldPopulateCreationAuditFields() {
        Person person = personRepository.save(new Person("John", 30));

        assertThat(person.getName()).isEqualTo("John");
        assertThat(person.getAge()).isEqualTo(30);

        assertThat(person.getId()).isNotNull();
        assertThat(person.getCreatedDate()).isNotNull();
        assertThat(person.getLastModifiedDate()).isNotNull();
    }

    @Test
    void save_shouldPopulateCreationAndModificationDates() {
        Person person = personRepository.save(new Person("John", 30));

        Instant createdDate = person.getCreatedDate();
        Instant originalLastModifiedDate = person.getLastModifiedDate();

        person.changeName("John Doe");

        Person updatedPerson = personRepository.save(person);

        assertThat(person.getName()).isEqualTo("John Doe");
        assertThat(person.getAge()).isEqualTo(30);

        assertThat(updatedPerson.getCreatedDate()).isEqualTo(createdDate);
        assertThat(updatedPerson.getLastModifiedDate()).isNotNull();
        assertThat(updatedPerson.getLastModifiedDate()).isAfterOrEqualTo(originalLastModifiedDate);
    }

    @Test
    void save_shouldPopulateCreatedAndModifiedAuditors() {
        Person person = personRepository.save(new Person("John", 30));

        assertThat(person.getName()).isEqualTo("John");
        assertThat(person.getAge()).isEqualTo(30);

        assertThat(person.getCreatedBy()).isEqualTo("system");
        assertThat(person.getLastModifiedBy()).isEqualTo("system");
    }
}
