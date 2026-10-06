package com.springbyexample.v2.projections.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.springbyexample.v2.projections.entity.Person;
import com.springbyexample.v2.projections.projection.PersonNameProjection;
import com.springbyexample.v2.projections.projection.PersonSummary;

/**
 * @author Mujuzi Moses
 */
public interface PersonRepository extends CrudRepository<Person, Long> {

    List<PersonNameProjection> findByName(String name);

    List<PersonSummary> findByAgeGreaterThan(int age);

    <T> List<T> findByAgeGreaterThan(int age, Class<T> type);
}
