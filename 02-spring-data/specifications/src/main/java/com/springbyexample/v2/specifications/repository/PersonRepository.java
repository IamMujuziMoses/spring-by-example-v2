package com.springbyexample.v2.specifications.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import com.springbyexample.v2.specifications.entity.Person;

/**
 * @author Mujuzi Moses
 */
public interface PersonRepository extends CrudRepository<Person, Long>, JpaSpecificationExecutor<Person> {
}
