package com.revolut.transfer.transaction.controller;

import com.revolut.transfer.account.service.AccountService;
import com.revolut.transfer.transaction.controller.dto.CreateTransferRequest;
import com.revolut.transfer.transaction.service.TransactionService;
import com.revolut.transfer.util.NumberUtils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

public final class CreateTransferHandler implements Handler {

    private final AccountService accountService;

    private final TransactionService transactionService;

    public CreateTransferHandler(
            final AccountService accountService, final TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Override
    public void handle(@NotNull final Context ctx) {
        var requestBody =
                ctx.bodyValidator(CreateTransferRequest.class)
                        .check(r -> r.getId() != null, "id must not be null")
                        .check(r -> r.getSourceAccountId() != null, "source_account_id must not be null")
                        .check(r -> r.getTargetAccountId() != null, "target_account_id must not be null")
                        .check(r -> NumberUtils.isGreaterThan(r.getAmount(), 0L), "amount must be positive")
                        .get();

        var sourceAccount = accountService.findById(requestBody.getSourceAccountId());
        var targetAccount = accountService.findById(requestBody.getTargetAccountId());

        transactionService.makeTransfer(requestBody.getId(), sourceAccount, targetAccount, requestBody.getAmount());

        ctx.status(HttpStatus.OK_200);
    }
}
