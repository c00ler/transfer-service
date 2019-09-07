package com.revolut.transfer.account.handler;

import com.revolut.transfer.account.dto.GetAccountResponseDto;
import com.revolut.transfer.account.service.AccountService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class GetAccountHandler implements Handler {

    private final AccountService accountService;

    public GetAccountHandler(final AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void handle(@NotNull final Context ctx) {
        var id = ctx.pathParam("id", UUID.class).get();

        ctx.json(GetAccountResponseDto.of(accountService.findById(id)));
    }
}
