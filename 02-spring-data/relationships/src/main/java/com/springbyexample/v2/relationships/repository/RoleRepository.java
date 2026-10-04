package com.springbyexample.v2.relationships.repository;

import org.springframework.data.repository.CrudRepository;

import com.springbyexample.v2.relationships.entity.Role;

/**
 * @author Mujuzi Moses
 */
public interface RoleRepository extends CrudRepository<Role, Long> {
}