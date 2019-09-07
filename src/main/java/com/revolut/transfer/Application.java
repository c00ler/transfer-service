package com.revolut.transfer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.transfer.account.handler.CreateAccountHandler;
import com.revolut.transfer.account.handler.GetAccountHandler;
import com.revolut.transfer.account.repository.AccountRepository;
import com.revolut.transfer.account.service.AccountService;
import com.revolut.transfer.exception.NotFoundException;
import io.javalin.Javalin;
import io.javalin.core.validation.JavalinValidation;
import io.javalin.plugin.json.JavalinJackson;
import org.eclipse.jetty.http.HttpStatus;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.Status;

import javax.sql.DataSource;
import java.util.UUID;

final class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private Javalin app;

    private final DataSource dataSource;

    private final int port;

    Application(final DataSource dataSource, final int port) {
        this.dataSource = dataSource;
        this.port = port;
    }

    void start() {
        JavalinValidation.register(UUID.class, UUID::fromString);
        JavalinJackson.configure(
                new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                        .registerModule(new ProblemModule()));

        app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.contextPath = "/api/v1";
            config.defaultContentType = "application/json";
        });

        app.exception(NotFoundException.class, (e, ctx) -> {
            LOG.warn("Entity not found: {}", e.getMessage());

            ctx.json(Problem.valueOf(Status.BAD_REQUEST)).status(HttpStatus.BAD_REQUEST_400);
        });

        var jooq = DSL.using(dataSource, SQLDialect.H2);

        var accountService = new AccountService(new AccountRepository(jooq));

        app.post("/accounts", new CreateAccountHandler(accountService));
        app.get("/accounts/:id", new GetAccountHandler(accountService));

        app.start(port);
    }

    void stop() {
        if (app != null) {
            app.stop();
        }
    }
}
