package com.revolut.transfer.account.repository;

import com.revolut.transfer.account.model.Account;
import com.revolut.transfer.persistence.jooq.Tables;
import org.jooq.DSLContext;

import java.util.Optional;
import java.util.UUID;

public final class AccountRepository {

    private final DSLContext jooq;

    public AccountRepository(final DSLContext jooq) {
        this.jooq = jooq;
    }

    public int persist(final Account account) {
        return jooq.insertInto(Tables.ACCOUNT)
                .set(Tables.ACCOUNT.ID, account.getId())
                .execute();
    }

    // Currently this method just checks if account exist in the database. It will be more useful
    // when account table will contain more information about the account
    public Optional<Account> findById(final UUID id) {
        return jooq.selectFrom(Tables.ACCOUNT)
                .where(Tables.ACCOUNT.ID.eq(id))
                .fetchOptional(r -> Account.of(r.getId()));
    }
}
