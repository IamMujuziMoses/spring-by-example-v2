package com.springbyexample.v2.databasetesting.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.springbyexample.v2.databasetesting.entity.Person;

/**
 * @author Mujuzi Moses
 */
public interface PersonRepository extends CrudRepository<Person, Long> {

    List<Person> findByAgeGreaterThan(int age);

    @Query("""
            select p
            from Person p
            where lower(p.name) like lower(concat('%', :text, '%'))
            """)
    List<Person> searchByName(@Param("text") String text);
}
