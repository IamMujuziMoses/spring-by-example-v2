package com.springbyexample.v2.integrationbetweenspringtechnologies.inventory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

/**
 * Repository for accessing product inventory.
 *
 * @author Mujuzi Moses
 */
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select inventory
            from InventoryItem inventory
            where inventory.productId = :productId
            """)
    Optional<InventoryItem> findByProductIdForUpdate(@Param("productId") Long productId);
}
