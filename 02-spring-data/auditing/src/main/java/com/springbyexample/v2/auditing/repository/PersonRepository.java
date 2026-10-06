package com.springbyexample.v2.auditing.repository;

import org.springframework.data.repository.CrudRepository;

import com.springbyexample.v2.auditing.entity.Person;

/**
 * @author Mujuzi Moses
 */
public interface PersonRepository extends CrudRepository<Person, Long> {
}
