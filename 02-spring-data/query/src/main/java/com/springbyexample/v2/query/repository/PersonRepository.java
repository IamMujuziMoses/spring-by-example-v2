package com.springbyexample.v2.query.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.springbyexample.v2.query.entity.Person;

/**
 * @author Mujuzi Moses
 */
public interface PersonRepository extends CrudRepository<Person, Long> {

    @Query("select p from Person p where p.id = :id")
    List<Person> findPeopleById(@Param("id") Long id);

     @Query("select p from Person p where p.name = :name")
    List<Person> findPeopleByName(@Param("name") String name);

    @Query("select p from Person p where p.name like %:text%")
    List<Person> searchByName(@Param("text") String text);

    @Query("select p from Person p where p.age > :age")
    List<Person> findPeopleOlderThan(@Param("age") int age);
}
