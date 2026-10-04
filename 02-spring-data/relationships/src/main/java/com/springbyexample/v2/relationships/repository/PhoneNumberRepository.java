package com.springbyexample.v2.relationships.repository;

import org.springframework.data.repository.CrudRepository;

import com.springbyexample.v2.relationships.entity.PhoneNumber;

/**
 * @author Mujuzi Moses
 */
public interface PhoneNumberRepository extends CrudRepository<PhoneNumber, Long> {
}
