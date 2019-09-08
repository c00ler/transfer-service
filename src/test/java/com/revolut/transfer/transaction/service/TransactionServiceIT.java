package com.revolut.transfer.transaction.service;

import com.revolut.transfer.AbstractIT;
import com.revolut.transfer.account.model.Account;
import com.revolut.transfer.account.repository.AccountRepository;
import com.revolut.transfer.account.service.AccountService;
import com.revolut.transfer.exception.InsufficientFundsException;
import com.revolut.transfer.persistence.jooq.Tables;
import com.revolut.transfer.transaction.model.TransferState;
import com.revolut.transfer.transaction.repository.TransactionRepository;
import org.jooq.exception.DataAccessException;
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

    @Nested
    class MakeTransfer {

        @Test
        void shouldThrowIfAmountIsNegative() {
            var sourceAccount = Account.of(UUID.randomUUID());
            var targetAccount = Account.of(UUID.randomUUID());

            assertThatThrownBy(() -> underTest.makeTransfer(UUID.randomUUID(), sourceAccount, targetAccount, -100_00L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("amount must be positive");
        }

        @Test
        void shouldThrowIfSourceAccountDoesNotExist() {
            var sourceAccount = Account.of(UUID.randomUUID());
            var targetAccount = accountService.createAccount();

            assertThatThrownBy(() -> underTest.makeTransfer(UUID.randomUUID(), sourceAccount, targetAccount, 100_00L))
                    .isInstanceOf(DataAccessException.class)
                    .hasMessageContaining("constraint violation")
                    .hasMessageContaining("FK_TRANSFER_SOURCE_ACCOUNT_ID");
        }

        @Test
        void shouldThrowIfTargetAccountDoesNotExist() {
            var sourceAccount = accountService.createAccount();
            var targetAccount = Account.of(UUID.randomUUID());

            assertThatThrownBy(() -> underTest.makeTransfer(UUID.randomUUID(), sourceAccount, targetAccount, 100_00L))
                    .isInstanceOf(DataAccessException.class)
                    .hasMessageContaining("constraint violation")
                    .hasMessageContaining("FK_TRANSFER_TARGET_ACCOUNT_ID");
        }

        @Test
        void shouldThrowAndSetInsufficientFundsState() {
            var sourceAccount = accountService.createAccount();
            var targetAccount = accountService.createAccount();

            underTest.creditAccount(UUID.randomUUID(), sourceAccount, 100_00L);

            var transferId = UUID.randomUUID();
            assertThatThrownBy(() -> underTest.makeTransfer(transferId, sourceAccount, targetAccount, 200_00L))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining(sourceAccount.getId().toString());

            assertThat(getTransferState(transferId)).isEqualTo(TransferState.INSUFFICIENT_FUNDS);
        }

        @Test
        void shouldMoveMoneyAndSetCompletedState() {
            var sourceAccount = accountService.createAccount();
            var targetAccount = accountService.createAccount();

            var amount = 100_00L;
            underTest.creditAccount(UUID.randomUUID(), sourceAccount, amount);

            var transferId = UUID.randomUUID();
            underTest.makeTransfer(transferId, sourceAccount, targetAccount, amount);

            assertThat(accountService.findById(sourceAccount.getId()).getBalance()).isEqualTo(0L);
            assertThat(accountService.findById(targetAccount.getId()).getBalance()).isEqualTo(amount);
            assertThat(getTransferState(transferId)).isEqualTo(TransferState.COMPLETED);
        }

        private TransferState getTransferState(final UUID transferId) {
            return JOOQ.selectFrom(Tables.TRANSFER)
                    .where(Tables.TRANSFER.ID.eq(transferId))
                    .fetchOne(Tables.TRANSFER.STATE);
        }
    }
}
