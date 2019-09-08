package com.revolut.transfer.exception;

import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.net.URI;

public final class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(final String message) {
        super(message);
    }

    public static final class Handler implements ExceptionHandler<InsufficientFundsException> {

        private static final Logger LOG = LoggerFactory.getLogger("exceptions");

        @Override
        public void handle(@NotNull final InsufficientFundsException e, @NotNull final Context ctx) {
            LOG.warn("Insufficient funds: {}", e.getMessage());

            var problem =
                    Problem.builder()
                            .withType(URI.create("https://revolut.com/insufficient-funds"))
                            .withTitle("Insufficient Funds")
                            .withStatus(Status.BAD_REQUEST)
                            .build();

            ctx.json(problem).status(HttpStatus.BAD_REQUEST_400);
        }
    }
}
