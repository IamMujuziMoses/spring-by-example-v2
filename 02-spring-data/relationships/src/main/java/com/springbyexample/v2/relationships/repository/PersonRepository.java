package com.springbyexample.v2.relationships.repository;

import org.springframework.data.repository.CrudRepository;

import com.springbyexample.v2.relationships.entity.Person;

/**
 * @author Mujuzi Moses
 */
public interface PersonRepository extends CrudRepository<Person, Long> {
}
