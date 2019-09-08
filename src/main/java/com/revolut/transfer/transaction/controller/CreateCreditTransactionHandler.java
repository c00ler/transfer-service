package com.revolut.transfer.transaction.controller;

import com.revolut.transfer.account.service.AccountService;
import com.revolut.transfer.transaction.controller.dto.CreateCreditTransactionRequest;
import com.revolut.transfer.transaction.service.TransactionService;
import com.revolut.transfer.util.ContextUtils;
import com.revolut.transfer.util.NumberUtils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

public final class CreateCreditTransactionHandler implements Handler {

    private final AccountService accountService;

    private final TransactionService transactionService;

    public CreateCreditTransactionHandler(
            final AccountService accountService, final TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Override
    public void handle(@NotNull final Context ctx) {
        var id = ContextUtils.getIdPathParam(ctx);
        var requestBody =
                ctx.bodyValidator(CreateCreditTransactionRequest.class)
                        .check(r -> r.getId() != null, "id must not be null")
                        .check(r -> NumberUtils.isGreaterThan(r.getAmount(), 0L), "amount must be positive")
                        .get();

        var account = accountService.findById(id);
        transactionService.creditAccount(requestBody.getId(), account, requestBody.getAmount());

        ctx.status(HttpStatus.OK_200);
    }
}
