package com.springbyexample.v2.springtestcontainers.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springbyexample.v2.springtestcontainers.Message;

/**
 * Provides persistence operations for messages.
 *
 * @author Mujuzi Moses
 */
public interface MessageRepository extends JpaRepository<Message, Long> {
}
