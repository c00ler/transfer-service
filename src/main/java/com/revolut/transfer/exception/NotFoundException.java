package com.revolut.transfer.exception;

import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public final class NotFoundException extends RuntimeException {

    public NotFoundException(final String message) {
        super(message);
    }

    public static final class Handler implements ExceptionHandler<NotFoundException> {

        private static final Logger LOG = LoggerFactory.getLogger("exceptions");

        @Override
        public void handle(@NotNull final NotFoundException e, @NotNull final Context ctx) {
            LOG.warn("Entity not found: {}", e.getMessage());

            ctx.json(Problem.valueOf(Status.NOT_FOUND)).status(HttpStatus.NOT_FOUND_404);
        }
    }
}
