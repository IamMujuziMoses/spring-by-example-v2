package com.springbyexample.v2.projections.repository;

import static org.assertj.core.api.IterableAssert.assertThatIterable;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.springbyexample.v2.projections.entity.Person;
import com.springbyexample.v2.projections.projection.PersonNameProjection;
import com.springbyexample.v2.projections.projection.PersonSummary;

/**
 * @author Mujuzi Moses
 */
@DataJpaTest
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Test
    void findByName_shouldReturnInterfaceProjection() {
        personRepository.save(new Person("John", 30));
        Person jane = personRepository.save(new Person("Jane", 25));

        List<PersonNameProjection> people = personRepository.findByName("Jane");

        assertThatIterable(people).extracting(PersonNameProjection::getId).containsExactly(jane.getId());
        assertThatIterable(people).extracting(PersonNameProjection::getName).containsExactly(jane.getName());
    }

    @Test
    void findByAgeGreaterThan_shouldReturnDtoProjection() {
        personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        Person peter = personRepository.save(new Person("Peter", 40));

        List<PersonSummary> people = personRepository.findByAgeGreaterThan(30);

        assertThatIterable(people).extracting(PersonSummary::id).containsExactly(peter.getId());
        assertThatIterable(people).extracting(PersonSummary::name).containsExactly(peter.getName());
        assertThatIterable(people).extracting(PersonSummary::age).containsExactly(peter.getAge());
    }

    @Test
    void findByAgeGreaterThan_shouldSupportDynamicProjection() {
        Person john = personRepository.save(new Person("John", 30));
        personRepository.save(new Person("Jane", 25));
        Person peter = personRepository.save(new Person("Peter", 40));

        List<PersonNameProjection> names = personRepository.findByAgeGreaterThan(25, PersonNameProjection.class);

        assertThatIterable(names).extracting(PersonNameProjection::getId)
                .containsExactlyInAnyOrder(john.getId(), peter.getId());
        assertThatIterable(names).extracting(PersonNameProjection::getName)
                .containsExactlyInAnyOrder(john.getName(), peter.getName());

        List<PersonSummary> summaries = personRepository.findByAgeGreaterThan(25, PersonSummary.class);

        assertThatIterable(summaries).extracting(PersonSummary::id)
                .containsExactlyInAnyOrder(john.getId(), peter.getId());
        assertThatIterable(summaries).extracting(PersonSummary::name)
                .containsExactlyInAnyOrder(john.getName(), peter.getName());
        assertThatIterable(summaries).extracting(PersonSummary::age)
                .containsExactlyInAnyOrder(john.getAge(), peter.getAge());
    }
}
