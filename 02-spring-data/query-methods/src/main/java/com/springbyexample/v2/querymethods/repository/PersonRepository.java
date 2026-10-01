package com.springbyexample.v2.querymethods.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.springbyexample.v2.querymethods.entity.Person;

/**
 * @author Mujuzi Moses
 */
public interface PersonRepository extends CrudRepository<Person, Long> {

    List<Person> findByName(String name);

    List<Person> findByNameContaining(String text);

    List<Person> findByNameStartingWith(String prefix);

    List<Person> findByAgeGreaterThan(int age);

    List<Person> findByAgeLessThan(int age);

    List<Person> findByAgeBetween(int minimum, int maximum);
}
