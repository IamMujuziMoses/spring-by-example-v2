package com.springbyexample.v2.specifications.specification;

import org.springframework.data.jpa.domain.Specification;

import com.springbyexample.v2.specifications.entity.Person;

/**
 * @author Mujuzi Moses
 */
public final class PersonSpecifications {

    private PersonSpecifications() {
    }

    public static Specification<Person> hasName(String name) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("name"), name);
    }

    public static Specification<Person> nameContains(String text) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("name"), "%" + text + "%");
    }

    public static Specification<Person> ageGreaterThan(int age) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("age"), age);
    }

    public static Specification<Person> ageLessThan(int age) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("age"), age);
    }

    public static Specification<Person> ageBetween(int minimum, int maximum) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("age"), minimum, maximum);
    }
}
