package com.revolut.transfer.account.handler;

import com.revolut.transfer.account.dto.GetAccountResponseDto;
import com.revolut.transfer.account.service.AccountService;
import com.revolut.transfer.util.ContextUtils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public final class GetAccountHandler implements Handler {

    private final AccountService accountService;

    public GetAccountHandler(final AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void handle(@NotNull final Context ctx) {
        var id = ContextUtils.getIdPathParam(ctx);

        ctx.json(GetAccountResponseDto.of(accountService.findById(id)));
    }
}
