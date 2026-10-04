package com.springbyexample.v2.entitymapping.repository;

import org.springframework.data.repository.CrudRepository;

import com.springbyexample.v2.entitymapping.entity.Person;

/**
 * @author Mujuzi Moses
 */
public interface PersonRepository extends CrudRepository<Person, Long> {
}
