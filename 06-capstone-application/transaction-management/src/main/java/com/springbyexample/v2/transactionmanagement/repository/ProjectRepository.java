package com.springbyexample.v2.transactionmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springbyexample.v2.transactionmanagement.domain.Project;

/**
 * Provides persistence operations for projects.
 *
 * @author Mujuzi Moses
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {
}

