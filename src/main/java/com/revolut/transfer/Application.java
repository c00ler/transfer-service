package com.revolut.transfer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.revolut.transfer.account.controller.CreateAccountHandler;
import com.revolut.transfer.account.controller.GetAccountHandler;
import com.revolut.transfer.account.repository.AccountRepository;
import com.revolut.transfer.account.service.AccountService;
import com.revolut.transfer.exception.DataAccessExceptionHandler;
import com.revolut.transfer.exception.InsufficientFundsException;
import com.revolut.transfer.exception.NotFoundException;
import com.revolut.transfer.transaction.controller.CreateCreditTransactionHandler;
import com.revolut.transfer.transaction.controller.CreateTransferHandler;
import com.revolut.transfer.transaction.repository.TransactionRepository;
import com.revolut.transfer.transaction.service.TransactionService;
import io.javalin.Javalin;
import io.javalin.core.validation.JavalinValidation;
import io.javalin.plugin.json.JavalinJackson;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.zalando.problem.ProblemModule;

import javax.sql.DataSource;
import java.util.UUID;

public final class Application {

    public static final String CONTEXT_PATH = "/api/v1";

    private Javalin app;

    private final DSLContext jooq;

    private final int port;

    Application(final DataSource dataSource, final int port) {
        this.jooq = DSL.using(dataSource, SQLDialect.H2);
        this.port = port;
    }

    void start() {
        configureGlobalJavalinSettings();

        app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.contextPath = CONTEXT_PATH;
            config.defaultContentType = "application/json";
        });

        app.exception(NotFoundException.class, new NotFoundException.Handler());
        app.exception(InsufficientFundsException.class, new InsufficientFundsException.Handler());
        app.exception(DataAccessException.class, new DataAccessExceptionHandler());

        // Create and wire all the dependencies.
        var accountRepository = new AccountRepository(jooq);
        var transactionRepository = new TransactionRepository(jooq);

        var accountService = new AccountService(accountRepository, transactionRepository);
        var transactionService = new TransactionService(transactionRepository);

        app.post("/accounts", new CreateAccountHandler(accountService));
        app.get("/accounts/:id", new GetAccountHandler(accountService));
        app.post("/accounts/:id/credit-transactions",
                new CreateCreditTransactionHandler(accountService, transactionService));
        app.post("/transfers", new CreateTransferHandler(accountService, transactionService));

        app.start(port);
    }

    void stop() {
        if (app != null) {
            app.stop();
        }
    }

    private static void configureGlobalJavalinSettings() {
        JavalinValidation.register(UUID.class, UUID::fromString);
        JavalinJackson.configure(
                new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                        .registerModule(new ProblemModule()));
    }
}
