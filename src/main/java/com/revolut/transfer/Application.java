package com.revolut.transfer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.transfer.account.handler.CreateAccountHandler;
import com.revolut.transfer.account.handler.GetAccountHandler;
import com.revolut.transfer.account.repository.AccountRepository;
import com.revolut.transfer.account.service.AccountService;
import com.revolut.transfer.exception.NotFoundException;
import com.revolut.transfer.transaction.handler.CreateCreditTransactionHandler;
import com.revolut.transfer.transaction.repository.TransactionRepository;
import io.javalin.Javalin;
import io.javalin.core.validation.JavalinValidation;
import io.javalin.plugin.json.JavalinJackson;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.zalando.problem.ProblemModule;

import javax.sql.DataSource;
import java.util.UUID;

final class Application {

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
            config.contextPath = "/api/v1";
            config.defaultContentType = "application/json";
        });

        app.exception(NotFoundException.class, new NotFoundException.Handler());

        var accountService = new AccountService(new AccountRepository(jooq), new TransactionRepository(jooq));

        app.post("/accounts", new CreateAccountHandler(accountService));
        app.get("/accounts/:id", new GetAccountHandler(accountService));

        app.post("/accounts/:id/credit-transactions", new CreateCreditTransactionHandler(accountService));

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
                        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                        .registerModule(new ProblemModule()));
    }
}
