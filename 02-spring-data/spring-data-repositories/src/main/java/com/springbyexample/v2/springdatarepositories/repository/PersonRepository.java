package com.springbyexample.v2.springdatarepositories.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import com.springbyexample.v2.springdatarepositories.entity.Person;


/**
 * @author Mujuzi Moses
 */
public interface PersonRepository extends Repository<Person, Long> {

    Person save(Person person);

    Optional<Person> findById(Long id);

    void deleteById(Long id);
}
