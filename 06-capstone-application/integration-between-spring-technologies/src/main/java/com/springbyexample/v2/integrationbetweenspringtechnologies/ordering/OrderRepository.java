package com.springbyexample.v2.integrationbetweenspringtechnologies.ordering;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for accessing customer orders.
 *
 * @author Mujuzi Moses
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByCustomerUsernameOrderByCreatedAtDesc(String customerUsername);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findById(@NonNull Long orderId);
}
