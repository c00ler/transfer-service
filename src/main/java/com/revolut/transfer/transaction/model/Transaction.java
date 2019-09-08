package com.revolut.transfer.transaction.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.util.UUID;

/**
 * Analog of a {@code sealed} class in Kotlin, because of a private constructor.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
public abstract class Transaction {

    private final UUID id;

    private final UUID accountId;

    private final Long amount;

    public static class Credit extends Transaction {

        public Credit(final UUID id, final UUID accountId, final Long amount) {
            super(id, accountId, checkPositiveAmount(amount));
        }

        private static Long checkPositiveAmount(final Long amount) {
            Validate.isTrue(amount != null && amount > 0, "amount must be positive");

            return amount;
        }
    }

    public static class Debit extends Transaction {

        public Debit(final UUID id, final UUID accountId, final Long amount) {
            super(id, accountId, checkNegativeAmount(amount));
        }

        private static Long checkNegativeAmount(final Long amount) {
            Validate.isTrue(amount != null && amount < 0, "amount must be negative");

            return amount;
        }
    }
}
