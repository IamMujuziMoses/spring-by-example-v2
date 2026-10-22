package com.springbyexample.v2.springintegrationtesting.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springbyexample.v2.springintegrationtesting.Message;

/**
 * @author Mujuzi Moses
 */
public interface MessageRepository extends JpaRepository<Message, Long> {
}
