package com.revolut.transfer.transaction.model;

import com.revolut.transfer.account.model.Account;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.UUID;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Transfer {

    UUID id;

    UUID sourceAccountId;

    UUID targetAccountId;

    UUID debitTransactionId;

    UUID creditTransactionId;

    Long amount;

    TransferState state;

    public static Transfer newTransfer(
            final UUID id, final Account sourceAccount, final Account targetAccount, final Long amount) {
        return new Transfer(
                id,
                sourceAccount.getId(),
                targetAccount.getId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                amount,
                TransferState.NEW);
    }
}
