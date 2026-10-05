package com.springbyexample.v2.specifications.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.IterableAssert.assertThatIterable;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.springbyexample.v2.specifications.entity.Person;
import com.springbyexample.v2.specifications.specification.PersonSpecifications;

/**
 * @author Mujuzi Moses
 */
@DataJpaTest
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    void findAll_shouldMatchNameSpecification() {
        personRepository.save(new Person("John", 30));
        Person jane = personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));

        List<Person> people = personRepository.findAll(PersonSpecifications.hasName("Jane"));

        assertThatIterable(people).extracting(Person::getName).containsExactly("Jane");
        assertThatIterable(people).extracting(Person::getId).containsExactly(jane.getId());
    }

    @Test
    void findAll_shouldMatchNameContainsSpecification() {
        personRepository.save(new Person("John", 30));
        Person jane = personRepository.save(new Person("Jane", 25));
        Person janet = personRepository.save(new Person("Janet", 35));

        List<Person> people = personRepository.findAll(PersonSpecifications.nameContains("Jan"));

        assertThatIterable(people).extracting(Person::getName).containsExactlyInAnyOrder("Jane", "Janet");
        assertThatIterable(people).extracting(Person::getId).containsExactlyInAnyOrder(jane.getId(), janet.getId());
    }

    @Test
    void findAll_shouldMatchAgeGreaterThanSpecification() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));

        List<Person> people = personRepository.findAll(PersonSpecifications.ageGreaterThan(30));

        assertThatIterable(people).extracting(Person::getName).containsExactly("Peter");
        assertThatIterable(people).extracting(Person::getAge).containsExactly(40);
    }

    @Test
    void findAll_shouldMatchAgeLessThanSpecification() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));

        List<Person> people = personRepository.findAll(PersonSpecifications.ageLessThan(30));

        assertThatIterable(people).extracting(Person::getName).containsExactly("Jane");
        assertThatIterable(people).extracting(Person::getAge).containsExactly(25);
    }

    @Test
    void findAll_shouldMatchAgeBetweenSpecification() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));
        personRepository.save(new Person("Mary", 35));

        List<Person> people = personRepository.findAll(PersonSpecifications.ageBetween(30, 40));

        assertThatIterable(people).extracting(Person::getName).containsExactlyInAnyOrder("John", "Peter", "Mary");
    }

    @Test
    void findAll_shouldCombineSpecificationsWithAnd() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));
        personRepository.save(new Person("Janet", 35));

        // Combine two reusable predicates into a single query condition.
        Specification<Person> specification = PersonSpecifications.nameContains("Jan")
                .and(PersonSpecifications.ageGreaterThan(30));

        List<Person> people = personRepository.findAll(specification);

        assertThatIterable(people).extracting(Person::getName).containsExactly("Janet");
    }

    @Test
    void findAll_shouldCombineSpecificationsWithOr() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));

        // OR allows either specification to match.
        Specification<Person> specification = PersonSpecifications.hasName("John").or(PersonSpecifications.hasName("Peter"));

        List<Person> people = personRepository.findAll(specification);

        assertThatIterable(people).extracting(Person::getName).containsExactlyInAnyOrder("John", "Peter");
    }

    @Test
    void count_shouldReturnNumberOfMatchingPeople() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));

        long count = personRepository.count(PersonSpecifications.ageGreaterThan(25));

        assertThat(count).isEqualTo(2);
    }

    @Test
    void exists_shouldReturnWhetherMatchingPersonExists() {
        personRepository.save(new Person("John", 30));

        boolean exists = personRepository.exists(PersonSpecifications.hasName("John"));

        assertThat(exists).isTrue();
    }

    @Test
    void findAll_shouldSupportSpecificationWithPagination() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        personRepository.save(new Person("Peter", 40));
        personRepository.save(new Person("Mary", 35));
        personRepository.save(new Person("David", 28));

        // Specifications can be combined with Spring Data pagination.
        Page<Person> page = personRepository.findAll(PersonSpecifications.ageGreaterThan(25),
                PageRequest.of(0, 2, Sort.by("id").ascending()));

        assertThatIterable(page.getContent()).extracting(Person::getName).containsExactly("John", "Peter");
        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }
}
