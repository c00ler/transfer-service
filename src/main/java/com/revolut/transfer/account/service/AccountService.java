package com.revolut.transfer.account.service;

import com.revolut.transfer.account.model.Account;
import com.revolut.transfer.account.repository.AccountRepository;
import com.revolut.transfer.exception.NotFoundException;
import com.revolut.transfer.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class AccountService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    public AccountService(
            final AccountRepository accountRepository, final TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Account createAccount() {
        var account = Account.of(UUID.randomUUID());

        accountRepository.persist(account);
        LOG.info("Account [accountId={}] successfully created", account.getId());

        return account;
    }

    public Account findById(final UUID id) {
        return accountRepository.findById(id)
                .map(a -> {
                    var balance = transactionRepository.getBalance(id);
                    return new Account(id, balance);
                })
                .orElseThrow(() -> new NotFoundException(String.format("account [accountId=%s] not found", id)));
    }
}
