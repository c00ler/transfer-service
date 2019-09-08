package com.revolut.transfer.transaction.repository;

import com.revolut.transfer.exception.InsufficientFundsException;
import com.revolut.transfer.exception.NotFoundException;
import com.revolut.transfer.persistence.jooq.Tables;
import com.revolut.transfer.persistence.jooq.tables.records.AccountRecord;
import com.revolut.transfer.transaction.model.Transaction;
import com.revolut.transfer.transaction.model.Transfer;
import com.revolut.transfer.transaction.model.TransferState;
import org.apache.commons.lang3.Validate;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.util.UUID;

public final class TransactionRepository {

    private final DSLContext jooq;

    public TransactionRepository(final DSLContext jooq) {
        this.jooq = jooq;
    }

    public Long getBalance(final UUID accountId) {
        return jooq.transactionResult(c -> {
            var transactionCtx = DSL.using(c);

            return getBalance(transactionCtx, accountId);
        });
    }

    public void persistTransfer(final Transfer transfer) {
        jooq.insertInto(Tables.TRANSFER)
                .set(Tables.TRANSFER.ID, transfer.getId())
                .set(Tables.TRANSFER.SOURCE_ACCOUNT_ID, transfer.getSourceAccountId())
                .set(Tables.TRANSFER.TARGET_ACCOUNT_ID, transfer.getTargetAccountId())
                .set(Tables.TRANSFER.DEBIT_TRANSACTION_ID, transfer.getDebitTransactionId())
                .set(Tables.TRANSFER.CREDIT_TRANSACTION_ID, transfer.getCreditTransactionId())
                .set(Tables.TRANSFER.AMOUNT, transfer.getAmount())
                .set(Tables.TRANSFER.STATE, transfer.getState())
                .execute();
    }

    public void updateTransferState(final UUID id, final TransferState state) {
        var count =
                jooq.update(Tables.TRANSFER)
                        .set(Tables.TRANSFER.STATE, state)
                        .where(Tables.TRANSFER.ID.eq(id))
                        .execute();

        Validate.validState(count > 0, "Transfer [transferId=%s] not found", id);
    }

    public void createCreditTransaction(final Transaction.Credit creditTransaction) {
        jooq.transaction(c -> {
            var transactionCtx = DSL.using(c);

            // I choose not to lock an account in the database here. The risk of not locking an account is that
            // in some rare cases credit transaction may fail due insufficient funds. Taking into the account that
            // likelihood of such conditions is very low, I decided to optimize for performance here.

            persist(transactionCtx, creditTransaction);
        });
    }

    public void createDebitTransaction(final Transaction.Debit debitTransaction) {
        jooq.transaction(c -> {
            var transactionCtx = DSL.using(c);

            var accountId = debitTransaction.getAccountId();

            // Lock account in the database, so there is only one debit transaction at a time
            lockAccount(transactionCtx, accountId);
            var currentBalance = getBalance(transactionCtx, accountId);
            if (Math.abs(debitTransaction.getAmount()) > currentBalance) {
                throw new InsufficientFundsException(
                        String.format("account [accountId=%s] doesn't have enough funds to complete transaction",
                                accountId));
            }

            persist(transactionCtx, debitTransaction);
        });
    }

    // Static methods to ensure that they use only provided DSLContext for execution.
    // If methods are not static then there is a risk that developer can use DSLContext
    // defined in this class and then methods will be executed in their own transaction.

    private static AccountRecord lockAccount(final DSLContext ctx, final UUID accountId) {
        return ctx.selectFrom(Tables.ACCOUNT)
                .where(Tables.ACCOUNT.ID.eq(accountId))
                .forUpdate()
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("account [accountId=%s] not found", accountId)));
    }

    private static Long getBalance(final DSLContext ctx, final UUID accountId) {
        // Balance query optimisation is not in the scope of the task
        return ctx.select(DSL.coalesce(DSL.sum(Tables.TRANSACTION.AMOUNT), BigDecimal.ZERO))
                .from(Tables.TRANSACTION)
                .where(Tables.TRANSACTION.ACCOUNT_ID.eq(accountId))
                .fetchOneInto(Long.class);
    }

    private static void persist(final DSLContext ctx, final Transaction transaction) {
        ctx.insertInto(Tables.TRANSACTION)
                .set(Tables.TRANSACTION.ID, transaction.getId())
                .set(Tables.TRANSACTION.ACCOUNT_ID, transaction.getAccountId())
                .set(Tables.TRANSACTION.AMOUNT, transaction.getAmount())
                .execute();
    }
}
