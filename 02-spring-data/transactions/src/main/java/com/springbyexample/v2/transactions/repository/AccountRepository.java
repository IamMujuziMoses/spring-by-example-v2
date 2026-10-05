package com.springbyexample.v2.transactions.repository;

import org.springframework.data.repository.CrudRepository;

import com.springbyexample.v2.transactions.entity.Account;

/**
 * @author Mujuzi Moses
 */
public interface AccountRepository extends CrudRepository<Account, Long> {
}