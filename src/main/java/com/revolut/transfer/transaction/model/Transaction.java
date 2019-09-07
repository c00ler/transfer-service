package com.revolut.transfer.transaction.model;

import lombok.Value;

import java.util.UUID;

@Value
public final class Transaction {

    UUID id;

    UUID accountId;

    Long amount;
}
