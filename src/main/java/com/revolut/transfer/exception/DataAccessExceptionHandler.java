package com.revolut.transfer.exception;

import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
import org.eclipse.jetty.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.sql.SQLIntegrityConstraintViolationException;

public final class DataAccessExceptionHandler implements ExceptionHandler<DataAccessException> {

    private static final Logger LOG = LoggerFactory.getLogger("exceptions");

    @Override
    public void handle(@NotNull final DataAccessException e, @NotNull final Context ctx) {
        var cause = e.getCause();
        if (cause instanceof SQLIntegrityConstraintViolationException) {
            LOG.warn("Constraint violation: {}", cause.getMessage());

            ctx.json(Problem.valueOf(Status.CONFLICT)).status(HttpStatus.CONFLICT_409);
        } else {
            LOG.error("Uncaught database exception", e);

            ctx.json(Problem.valueOf(Status.INTERNAL_SERVER_ERROR)).status(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }
}
