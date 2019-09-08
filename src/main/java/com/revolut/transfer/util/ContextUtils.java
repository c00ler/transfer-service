package com.revolut.transfer.util;

import io.javalin.http.Context;

import java.util.UUID;

public final class ContextUtils {

    private ContextUtils() {
    }

    /**
     * Convenience method to extract {@code :id} path parameter as {@code UUID} from Javalin context.
     *
     * @param ctx Javalin context
     * @return value of {@code :id} path parameter as {@code UUID} or throws an exception
     */
    public static UUID getIdPathParam(final Context ctx) {
        return ctx.pathParam("id", UUID.class).get();
    }
}
