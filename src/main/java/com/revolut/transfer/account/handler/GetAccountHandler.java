package com.revolut.transfer.account.handler;

import com.revolut.transfer.account.dto.GetAccountResponseDto;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class GetAccountHandler implements Handler {

    @Override
    public void handle(@NotNull final Context ctx) {
        var id = ctx.pathParam("id", UUID.class).get();

        ctx.json(new GetAccountResponseDto().setId(id).setBalance(0L));
    }
}
