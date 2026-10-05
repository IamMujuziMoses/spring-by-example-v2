package com.springbyexample.v2.transactions.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springbyexample.v2.transactions.entity.Account;
import com.springbyexample.v2.transactions.entity.Transfer;
import com.springbyexample.v2.transactions.repository.AccountRepository;
import com.springbyexample.v2.transactions.repository.TransferRepository;

/**
 * @author Mujuzi Moses
 */
@Service
public class TransferService {

    private final AccountRepository accountRepository;

    private final TransferRepository transferRepository;

    public TransferService(AccountRepository accountRepository, TransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
    }

    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        fromAccount.withdraw(amount);
        toAccount.deposit(amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transferRepository.save(new Transfer(fromAccount, toAccount, amount));
    }

    @Transactional
    public void transferAndFail(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        fromAccount.withdraw(amount);
        toAccount.deposit(amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transferRepository.save(new Transfer(fromAccount, toAccount, amount));

        throw new IllegalStateException("Transfer failed");
    }
}
