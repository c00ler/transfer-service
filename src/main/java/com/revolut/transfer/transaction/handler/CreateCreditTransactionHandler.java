package com.revolut.transfer.transaction.handler;

import com.revolut.transfer.account.service.AccountService;
import com.revolut.transfer.transaction.dto.CreateCreditTransactionRequestDto;
import com.revolut.transfer.util.ContextUtils;
import com.revolut.transfer.util.NumberUtils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

public final class CreateCreditTransactionHandler implements Handler {

    private final AccountService accountService;

    public CreateCreditTransactionHandler(final AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void handle(@NotNull final Context ctx) {
        var id = ContextUtils.getIdPathParam(ctx);
        var requestBody =
                ctx.bodyValidator(CreateCreditTransactionRequestDto.class)
                        .check(r -> r.getId() != null, "id must not be null")
                        .check(r -> NumberUtils.isGreaterThan(r.getAmount(), 0L), "amount must be positive")
                        .get();

        var account = accountService.findById(id);
        accountService.createCreditTransaction(requestBody.getId(), account.getId(), requestBody.getAmount());

        ctx.status(HttpStatus.CREATED_201);
    }
}
