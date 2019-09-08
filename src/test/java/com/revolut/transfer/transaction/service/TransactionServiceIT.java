package com.revolut.transfer.transaction.service;

import com.revolut.transfer.AbstractIT;
import com.revolut.transfer.account.model.Account;
import com.revolut.transfer.account.repository.AccountRepository;
import com.revolut.transfer.account.service.AccountService;
import com.revolut.transfer.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionServiceIT extends AbstractIT {

    private final AccountService accountService =
            new AccountService(new AccountRepository(JOOQ), new TransactionRepository(JOOQ));

    private final TransactionService underTest = new TransactionService(new TransactionRepository(JOOQ));

    @Nested
    class CreditAccount {

        @Test
        void shouldThrowIfAmountIsNegative() {
            var account = Account.of(UUID.randomUUID());
            assertThatThrownBy(() -> underTest.creditAccount(UUID.randomUUID(), account, -100_00L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("amount must be positive");
        }

        @Test
        void shouldCreateCreditTransaction() {
            var amount = 100_00L;

            // Transactions must reference an account, so it has to be created before the transaction
            var account = accountService.createAccount();
            underTest.creditAccount(UUID.randomUUID(), account, amount);

            assertThat(accountService.findById(account.getId()).getBalance()).isEqualTo(amount);
        }
    }
}
