package com.springbyexample.v2.customrepositories.repository;

import java.util.List;

import com.springbyexample.v2.customrepositories.entity.Person;

/**
 * @author Mujuzi Moses
 */
public interface PersonRepositoryCustom {

    List<Person> search(String text);
}
