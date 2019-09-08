package com.revolut.transfer.transaction.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    @Test
    void creditTransactionShouldThrowIfBalanceIsNegative() {
        assertThatThrownBy(() -> new Transaction.Credit(UUID.randomUUID(), UUID.randomUUID(), -100_00L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be positive");
    }

    @Test
    void creditTransactionShouldThrowIfBalanceIsZero() {
        assertThatThrownBy(() -> new Transaction.Credit(UUID.randomUUID(), UUID.randomUUID(), 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be positive");
    }

    @Test
    void debitTransactionShouldThrowIfBalanceIsPositive() {
        assertThatThrownBy(() -> new Transaction.Debit(UUID.randomUUID(), UUID.randomUUID(), 100_00L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be negative");
    }

    @Test
    void debitTransactionShouldThrowIfBalanceIsZero() {
        assertThatThrownBy(() -> new Transaction.Debit(UUID.randomUUID(), UUID.randomUUID(), 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be negative");
    }
}
