package com.springbyexample.v2.transactions.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.transactions.entity.Account;
import com.springbyexample.v2.transactions.entity.Transfer;
import com.springbyexample.v2.transactions.repository.AccountRepository;
import com.springbyexample.v2.transactions.repository.TransferRepository;

/**
 * @author Mujuzi Moses
 */
@SpringBootTest
public class TransferServiceTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    @BeforeEach
    void cleanDatabase() {
        transferRepository.deleteAll();
        accountRepository.deleteAll();
    }


    @Test
    void transfer_shouldMoveMoneyBetweenAccounts() {
        Account source = accountRepository.save(new Account("John", new BigDecimal("100.00")));
        Account destination = accountRepository.save(new Account("Jane", new BigDecimal("50.00")));

        // The service method runs inside a transaction.
        transferService.transfer(source.getId(), destination.getId(), new BigDecimal("25.00"));

        // Reload the accounts to verify the committed transaction state.
        Account updatedSource = accountRepository.findById(source.getId()).orElseThrow();
        Account updatedDestination = accountRepository.findById(destination.getId()).orElseThrow();

        assertThat(updatedSource.getId()).isEqualTo(source.getId());
        assertThat(updatedSource.getOwner()).isEqualTo("John");
        assertThat(updatedSource.getBalance()).isEqualByComparingTo("75.00");

        assertThat(updatedDestination.getId()).isEqualTo(destination.getId());
        assertThat(updatedDestination.getOwner()).isEqualTo("Jane");
        assertThat(updatedDestination.getBalance()).isEqualByComparingTo("75.00");

        assertThat(transferRepository.count()).isEqualTo(1);

        Transfer transfer = transferRepository.findAll().iterator().next();

        assertThat(transfer.getId()).isNotNull();
        assertThat(transfer.getFromAccount().getId()).isEqualTo(source.getId());
        assertThat(transfer.getToAccount().getId()).isEqualTo(destination.getId());
        assertThat(transfer.getAmount()).isEqualByComparingTo("25.00");
    }

    @Test
    void transfer_shouldRejectTransferWhenFundsAreInsufficient() {
        Account source = accountRepository.save(new Account("John", new BigDecimal("10.00")));
        Account destination = accountRepository.save(new Account("Jane", new BigDecimal("50.00")));

        // The withdrawal fails before any transfer can be completed.
        assertThatThrownBy(() -> transferService.transfer(source.getId(), destination.getId(),
                new BigDecimal("25.00"))).isInstanceOf(IllegalStateException.class).hasMessage("Insufficient funds");

        // Verify that neither account nor the transfer record was changed.
        Account updatedSource = accountRepository.findById(source.getId()).orElseThrow();
        Account updatedDestination = accountRepository.findById(destination.getId()).orElseThrow();

        assertThat(updatedSource.getBalance()).isEqualByComparingTo("10.00");
        assertThat(updatedDestination.getBalance()).isEqualByComparingTo("50.00");
        assertThat(transferRepository.count()).isZero();
    }

    @Test
    void transfer_shouldRollbackWhenTransactionFails() {
        Account source = accountRepository.save(new Account("John", new BigDecimal("100.00")));
        Account destination = accountRepository.save(new Account("Jane", new BigDecimal("50.00")));

        // The transfer changes multiple entities before an exception occurs.
        assertThatThrownBy(() -> transferService.transferAndFail(source.getId(), destination.getId(),
                new BigDecimal("25.00"))).isInstanceOf(IllegalStateException.class).hasMessage("Transfer failed");

        // Even though the changes happened before the failure, the transaction should have rolled everything back.
        Account updatedSource = accountRepository.findById(source.getId()).orElseThrow();
        Account updatedDestination = accountRepository.findById(destination.getId()).orElseThrow();

        assertThat(updatedSource.getBalance()).isEqualByComparingTo("100.00");
        assertThat(updatedDestination.getBalance()).isEqualByComparingTo("50.00");
        assertThat(transferRepository.count()).isZero();
    }
}
