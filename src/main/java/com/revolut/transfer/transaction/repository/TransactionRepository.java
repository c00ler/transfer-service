package com.revolut.transfer.transaction.repository;

import com.revolut.transfer.persistence.jooq.Tables;
import com.revolut.transfer.transaction.model.Transaction;
import org.jooq.DSLContext;

public final class TransactionRepository {

    private final DSLContext jooq;

    public TransactionRepository(final DSLContext jooq) {
        this.jooq = jooq;
    }

    public int insert(final Transaction transaction) {
        return jooq.insertInto(Tables.TRANSACTION)
                .set(Tables.TRANSACTION.ID, transaction.getId())
                .set(Tables.TRANSACTION.ACCOUNT_ID, transaction.getAccountId())
                .set(Tables.TRANSACTION.AMOUNT, transaction.getAmount())
                .execute();
    }
}
