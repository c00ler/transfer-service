package com.revolut.transfer.transaction.service;

import com.revolut.transfer.account.model.Account;
import com.revolut.transfer.exception.InsufficientFundsException;
import com.revolut.transfer.transaction.model.Transaction;
import com.revolut.transfer.transaction.model.Transfer;
import com.revolut.transfer.transaction.model.TransferState;
import com.revolut.transfer.transaction.repository.TransactionRepository;
import com.revolut.transfer.util.NumberUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class TransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;

    public TransactionService(final TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void creditAccount(final UUID transactionId, final Account account, final Long amount) {
        var creditTransaction = new Transaction.Credit(transactionId, account.getId(), amount);
        transactionRepository.createCreditTransaction(creditTransaction);
        LOG.info("Account [accountId={}] was credited [amount={}]", account.getId(), amount);
    }

    /**
     * Transfers money between source and target accounts. Transfer id must be provided from the outside to make it
     * idempotent.
     * <p>
     * Recovery from the failures is outside of the scope of the task.
     *
     * @param transferId    transfer id
     * @param sourceAccount account to debit
     * @param targetAccount account to credit
     * @param amount        amount to transfer
     */
    public void makeTransfer(
            final UUID transferId, final Account sourceAccount, final Account targetAccount, final Long amount) {
        Validate.isTrue(NumberUtils.isGreaterThan(amount, 0L), "amount must be positive");
        Validate.isTrue(!sourceAccount.getId().equals(targetAccount.getId()),
                "source and target accounts must be different");

        var transfer = Transfer.newTransfer(transferId, sourceAccount, targetAccount, amount);
        // Persist transfer to the database, to we can recover it in case of any failures
        transactionRepository.persistTransfer(transfer);
        LOG.info("New transfer [transferId={}] created", transferId);

        makeDebitTransaction(transfer);
        LOG.info("Account [accountId={}] was debited [amount={}]", sourceAccount.getId(), amount);

        makeCreditTransaction(transfer);
        LOG.info("Account [accountId={}] was credited [amount={}]", targetAccount.getId(), amount);
    }

    private void makeDebitTransaction(final Transfer transfer) {
        var debitTransaction = new Transaction.Debit(
                transfer.getDebitTransactionId(), transfer.getSourceAccountId(), -1 * transfer.getAmount());
        try {
            transactionRepository.createDebitTransaction(debitTransaction);
            // Update state in the database. If there will be any failure it can be retried in the background.
            // All operations are idempotent, because transaction ids are generated and stored together
            // with transfer
            transactionRepository.updateTransferState(transfer.getId(), TransferState.SOURCE_CHARGED);
        } catch (InsufficientFundsException e) {
            // Transfer is considered failed and do not need to be recovered
            transactionRepository.updateTransferState(transfer.getId(), TransferState.INSUFFICIENT_FUNDS);

            throw e;
        }
    }

    private void makeCreditTransaction(final Transfer transfer) {
        var creditTransaction = new Transaction.Credit(
                transfer.getCreditTransactionId(), transfer.getTargetAccountId(), transfer.getAmount());
        transactionRepository.createCreditTransaction(creditTransaction);
        transactionRepository.updateTransferState(transfer.getId(), TransferState.COMPLETED);
    }
}
