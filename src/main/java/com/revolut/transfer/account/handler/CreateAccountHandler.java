package com.revolut.transfer.account.handler;

import com.revolut.transfer.Application;
import com.revolut.transfer.account.service.AccountService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

public final class CreateAccountHandler implements Handler {

    private final AccountService accountService;

    public CreateAccountHandler(final AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void handle(@NotNull final Context ctx) {
        var account = accountService.createAccount();

        var location = String.format("%s/accounts/%s", Application.CONTEXT_PATH, account.getId());
        ctx.status(HttpStatus.CREATED_201).header(HttpHeader.LOCATION.asString(), location);
    }
}
