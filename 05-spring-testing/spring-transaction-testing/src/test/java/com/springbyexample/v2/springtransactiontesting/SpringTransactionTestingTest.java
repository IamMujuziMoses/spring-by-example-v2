package com.springbyexample.v2.springtransactiontesting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mujuzi Moses
 */
@SpringBootTest
public class SpringTransactionTestingTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void shouldRollBackDatabaseChangesAfterTest() {
        jdbcTemplate.update("INSERT INTO transaction_records (message) VALUES (?)", "Created during test");

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transaction_records", Long.class);

        assertEquals(1L, count);
    }

    @AfterTransaction
    void shouldVerifyDatabaseChangesWereRolledBack() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transaction_records", Long.class);

        assertEquals(0L, count);
    }
}
