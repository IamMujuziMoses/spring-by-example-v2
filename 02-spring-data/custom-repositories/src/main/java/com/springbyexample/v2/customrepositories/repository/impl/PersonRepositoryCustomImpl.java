package com.springbyexample.v2.customrepositories.repository.impl;

import java.util.List;

import com.springbyexample.v2.customrepositories.entity.Person;
import com.springbyexample.v2.customrepositories.repository.PersonRepositoryCustom;

import jakarta.persistence.EntityManager;

/**
 * @author Mujuzi Moses
 */
public class PersonRepositoryCustomImpl
        implements PersonRepositoryCustom {

    private final EntityManager entityManager;

    public PersonRepositoryCustomImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Person> search(String text) {
        return entityManager.createQuery(
                        """
                        select p
                        from Person p
                        where lower(p.name) like lower(:text)
                        """,
                        Person.class)
                .setParameter("text", "%" + text + "%")
                .getResultList();
    }
}
