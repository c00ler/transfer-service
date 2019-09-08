package com.revolut.transfer.account.service;

import com.revolut.transfer.account.model.Account;
import com.revolut.transfer.account.repository.AccountRepository;
import com.revolut.transfer.exception.NotFoundException;
import com.revolut.transfer.transaction.model.Transaction;
import com.revolut.transfer.transaction.repository.TransactionRepository;
import com.revolut.transfer.util.NumberUtils;
import org.apache.commons.lang3.Validate;
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

        accountRepository.insert(account);
        LOG.info("New account [accountId={}] successfully created", account.getId());

        return account;
    }

    public Account findById(final UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("account [accountId=%s] not found", id)));
    }

    public Transaction createCreditTransaction(final UUID id, final UUID accountId, final Long amount) {
        Validate.isTrue(NumberUtils.isGreaterThan(amount, 0L), "amount must be positive");

        var transaction = new Transaction(id, accountId, amount);
        transactionRepository.insert(transaction);
        LOG.info("New credit transaction [transactionId={}] for account [accountId={}] successfully created",
                accountId, id);

        return transaction;
    }
}
