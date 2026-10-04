package com.springbyexample.v2.relationships.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.IterableAssert.assertThatIterable;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.springbyexample.v2.relationships.entity.Address;
import com.springbyexample.v2.relationships.entity.Person;
import com.springbyexample.v2.relationships.entity.PhoneNumber;
import com.springbyexample.v2.relationships.entity.Role;

/**
 * @author Mujuzi Moses
 */
@DataJpaTest
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PhoneNumberRepository phoneNumberRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void oneToOne_shouldAssociatePersonWithAddress() {
        Address address = addressRepository.save(new Address("Kampala", "Uganda"));

        Person person = new Person("John Doe");
        person.setAddress(address);

        Person savedPerson = personRepository.save(person);
        Person result = personRepository.findById(savedPerson.getId()).orElseThrow();

        assertThat(result.getAddress()).isNotNull();
        assertThat(result.getAddress().getId()).isEqualTo(address.getId());
        assertThat(result.getAddress().getCity()).isEqualTo("Kampala");
        assertThat(result.getAddress().getCountry()).isEqualTo("Uganda");
    }

    @Test
    void oneToMany_shouldAssociatePersonWithMultiplePhoneNumbers() {
        Person person = personRepository.save(new Person("John Doe"));

        PhoneNumber firstNumber = new PhoneNumber("+256700000001");
        PhoneNumber secondNumber = new PhoneNumber("+256700000002");

        person.addPhoneNumber(firstNumber);
        person.addPhoneNumber(secondNumber);

        phoneNumberRepository.save(firstNumber);
        phoneNumberRepository.save(secondNumber);

        Person result = personRepository.findById(person.getId()).orElseThrow();

        assertThatIterable(result.getPhoneNumbers()).extracting(PhoneNumber::getNumber)
                .containsExactlyInAnyOrder("+256700000001", "+256700000002");
    }

    @Test
    void manyToOne_shouldAssociatePhoneNumberWithPerson() {
        Person person = personRepository.save(new Person("John Doe"));

        PhoneNumber phoneNumber = new PhoneNumber("+256700000001");
        phoneNumber.setPerson(person);

        PhoneNumber savedPhoneNumber = phoneNumberRepository.save(phoneNumber);
        PhoneNumber result = phoneNumberRepository.findById(savedPhoneNumber.getId()).orElseThrow();

        assertThat(result.getPerson()).isNotNull();
        assertThat(result.getPerson().getId()).isEqualTo(person.getId());
        assertThat(result.getPerson().getName()).isEqualTo("John Doe");
    }

    @Test
    void manyToMany_shouldAssociatePersonWithMultipleRoles() {
        Role developer = roleRepository.save(new Role("Developer"));
        Role administrator = roleRepository.save(new Role("Administrator"));

        Person person = new Person("John Doe");
        person.addRole(developer);
        person.addRole(administrator);

        Person savedPerson = personRepository.save(person);
        Person result = personRepository.findById(savedPerson.getId()).orElseThrow();

        assertThatIterable(result.getRoles()).extracting(Role::getName)
                .containsExactlyInAnyOrder("Developer", "Administrator");
        assertThatIterable(result.getRoles()).extracting(Role::getId)
                .containsExactlyInAnyOrder(developer.getId(), administrator.getId());
    }

    @Test
    void relationships_shouldBePersistedTogether() {
        Address address = addressRepository.save(new Address("Kampala", "Uganda"));

        Role developer = roleRepository.save(new Role("Developer"));
        Role administrator = roleRepository.save(new Role("Administrator"));

        Person person = new Person("John Doe");
        person.setAddress(address);

        PhoneNumber firstNumber = new PhoneNumber("+256700000001");
        PhoneNumber secondNumber = new PhoneNumber("+256700000002");

        person.addPhoneNumber(firstNumber);
        person.addPhoneNumber(secondNumber);
        person.addRole(developer);
        person.addRole(administrator);

        phoneNumberRepository.save(firstNumber);
        phoneNumberRepository.save(secondNumber);

        Person savedPerson = personRepository.save(person);
        Person result = personRepository.findById(savedPerson.getId()).orElseThrow();

        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getAddress().getCity()).isEqualTo("Kampala");
        assertThatIterable(result.getPhoneNumbers()).extracting(PhoneNumber::getNumber)
                .containsExactlyInAnyOrder("+256700000001", "+256700000002");
        assertThatIterable(result.getRoles()).extracting(Role::getName)
                .containsExactlyInAnyOrder("Developer", "Administrator");
    }
}
