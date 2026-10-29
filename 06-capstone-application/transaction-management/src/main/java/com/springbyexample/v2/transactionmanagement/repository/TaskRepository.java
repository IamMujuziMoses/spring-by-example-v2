package com.springbyexample.v2.transactionmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springbyexample.v2.transactionmanagement.domain.Task;

/**
 * Provides persistence operations for tasks.
 *
 * @author Mujuzi Moses
 */
public interface TaskRepository extends JpaRepository<Task, Long> {
}

