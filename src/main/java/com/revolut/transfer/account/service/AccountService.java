package com.revolut.transfer.account.service;

import com.revolut.transfer.account.model.Account;
import com.revolut.transfer.account.repository.AccountRepository;
import com.revolut.transfer.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class AccountService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    public AccountService(final AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount() {
        var account = new Account(UUID.randomUUID());

        accountRepository.insert(account);
        LOG.info("New account id={} successfully created", account.getId());

        return account;
    }

    public Account findById(final UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("account id=%s not found", id)));
    }
}
