package com.springbyexample.v2.relationships.repository;

import org.springframework.data.repository.CrudRepository;

import com.springbyexample.v2.relationships.entity.Address;

/**
 * @author Mujuzi Moses
 */
public interface AddressRepository extends CrudRepository<Address, Long> {
}