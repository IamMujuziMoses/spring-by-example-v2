package com.springbyexample.v2.transactions.repository;

import org.springframework.data.repository.CrudRepository;

import com.springbyexample.v2.transactions.entity.Transfer;

/**
 * @author Mujuzi Moses
 */
public interface TransferRepository extends CrudRepository<Transfer, Long> {
}