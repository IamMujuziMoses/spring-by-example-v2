package com.springbyexample.v2.integrationbetweenspringtechnologies.catalog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for accessing catalog products.
 *
 * @author Mujuzi Moses
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByActiveTrue();

    Optional<Product> findByIdAndActiveTrue(Long id);
}
