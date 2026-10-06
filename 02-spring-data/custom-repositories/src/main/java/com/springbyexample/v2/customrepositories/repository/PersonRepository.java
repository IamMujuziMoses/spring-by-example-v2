package com.springbyexample.v2.customrepositories.repository;

import org.springframework.data.repository.CrudRepository;

import com.springbyexample.v2.customrepositories.entity.Person;

/**
 * @author Mujuzi Moses
 */
public interface PersonRepository extends CrudRepository<Person, Long>, PersonRepositoryCustom {
}
