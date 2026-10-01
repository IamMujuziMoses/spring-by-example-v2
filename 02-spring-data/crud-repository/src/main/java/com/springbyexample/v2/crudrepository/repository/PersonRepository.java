package com.springbyexample.v2.crudrepository.repository;

import org.springframework.data.repository.CrudRepository;

import com.springbyexample.v2.crudrepository.entity.Person;

/**
 * @author Mujuzi Moses
 */
public interface PersonRepository extends CrudRepository<Person, Long> {
}
