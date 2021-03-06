package com.revolut.transfer.account.model;

import lombok.Value;

import java.util.UUID;

@Value
public final class Account {

    UUID id;

    // For simplicity all amounts are in cents
    Long balance;

    public static Account of(final UUID id) {
        return new Account(id, 0L);
    }
}
