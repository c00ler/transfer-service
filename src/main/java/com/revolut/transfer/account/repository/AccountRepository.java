package com.revolut.transfer.account.repository;

import com.revolut.transfer.account.model.Account;
import com.revolut.transfer.persistence.jooq.Tables;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public final class AccountRepository {

    private final DSLContext jooq;

    public AccountRepository(final DSLContext jooq) {
        this.jooq = jooq;
    }

    public int insert(final Account account) {
        return jooq.insertInto(Tables.ACCOUNT)
                .set(Tables.ACCOUNT.ID, account.getId())
                .execute();
    }

    public Optional<Account> findById(final UUID id) {
        return jooq.selectFrom(Tables.ACCOUNT)
                .where(Tables.ACCOUNT.ID.eq(id))
                .fetchOptional(r -> {
                    var accountId = r.getId();

                    // Balance query optimisation is not in the scope of the task
                    var balance =
                            jooq.select(DSL.coalesce(DSL.sum(Tables.TRANSACTION.AMOUNT), BigDecimal.ZERO))
                                    .from(Tables.TRANSACTION)
                                    .where(Tables.TRANSACTION.ACCOUNT_ID.eq(accountId))
                                    .fetchOneInto(Long.class);

                    return new Account(accountId, balance);
                });
    }
}
