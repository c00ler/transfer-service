package com.revolut.transfer.account.service;

import com.revolut.transfer.AbstractIT;
import com.revolut.transfer.account.repository.AccountRepository;
import com.revolut.transfer.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountServiceIT extends AbstractIT {

    private final AccountService underTest =
            new AccountService(new AccountRepository(JOOQ), new TransactionRepository(JOOQ));

    @Nested
    class CreateCreditTransaction {

        @Test
        void shouldThrowIfAmountIsNegative() {
            assertThatThrownBy(() -> underTest.createCreditTransaction(UUID.randomUUID(), UUID.randomUUID(), -100L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("amount must be positive");
        }

        @Test
        void shouldCreateTransaction() {
            var amount = 100L;

            // Transactions must reference an account, so it has to be created before the transaction
            var account = underTest.createAccount();
            var transaction = underTest.createCreditTransaction(UUID.randomUUID(), account.getId(), amount);

            assertThat(underTest.findById(transaction.getAccountId()).getBalance()).isEqualTo(amount);
        }
    }
}
