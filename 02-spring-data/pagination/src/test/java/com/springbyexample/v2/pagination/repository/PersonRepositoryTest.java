package com.springbyexample.v2.pagination.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.IterableAssert.assertThatIterable;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.springbyexample.v2.pagination.entity.Person;
import com.springbyexample.v2.pagination.repositorry.PersonRepository;

/**
 * @author Mujuzi Moses
 */

@DataJpaTest
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    void findAll_shouldReturnRequestedPage() {
        Person john = personRepository.save(new Person("John", 30));
        Person jane = personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));
        personRepository.save(new Person("Mary", 35));
        personRepository.save(new Person("David", 28));

        // Request the first two records using a zero-based page number.
        Page<Person> page = personRepository.findAll(PageRequest.of(0, 2, Sort.by("id").ascending()));

        // Page contains only the records belonging to the requested page.
        assertThatIterable(page.getContent()).extracting(Person::getName).containsExactly("John", "Jane");
        assertThatIterable(page.getContent()).extracting(Person::getId).containsExactly(john.getId(), jane.getId());
        assertThatIterable(page.getContent()).extracting(Person::getAge).containsExactly(john.getAge(), jane.getAge());

        assertThat(page.getNumber()).isZero();
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void findAll_shouldReturnSecondPage() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        Person peter = personRepository.save(new Person("Peter", 40));
        Person mary = personRepository.save(new Person("Mary", 35));
        personRepository.save(new Person("David", 28));

        // Page numbers are zero-based, so 1 represents the second page.
        Page<Person> page = personRepository.findAll(PageRequest.of(1, 2, Sort.by("id").ascending()));

        assertThatIterable(page.getContent()).extracting(Person::getName).containsExactly("Peter", "Mary");
        assertThatIterable(page.getContent()).extracting(Person::getId).containsExactly(peter.getId(), mary.getId());
        assertThatIterable(page.getContent()).extracting(Person::getAge).containsExactly(peter.getAge(), mary.getAge());

        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.hasPrevious()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void findAll_shouldReturnLastPage() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));
        personRepository.save(new Person("Mary", 35));
        Person david = personRepository.save(new Person("David", 28));

        // The final page contains the remaining record.
        Page<Person> page = personRepository.findAll(PageRequest.of(2, 2, Sort.by("id").ascending()));

        assertThatIterable(page.getContent()).extracting(Person::getName).containsExactly("David");
        assertThatIterable(page.getContent()).extracting(Person::getId).containsExactly(david.getId());
        assertThatIterable(page.getContent()).extracting(Person::getAge).containsExactly(david.getAge());

        assertThat(page.getNumber()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.getNumberOfElements()).isEqualTo(1);
        assertThat(page.hasPrevious()).isTrue();
        assertThat(page.hasNext()).isFalse();
        assertThat(page.isLast()).isTrue();
    }
}
