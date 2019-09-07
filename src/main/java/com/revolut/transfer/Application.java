package com.revolut.transfer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.transfer.account.handler.GetAccountHandler;
import io.javalin.Javalin;
import io.javalin.core.validation.JavalinValidation;
import io.javalin.plugin.json.JavalinJackson;
import org.zalando.problem.ProblemModule;

import java.util.UUID;

final class Application {

    private Javalin app;

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
        app.get("/accounts/:id", new GetAccountHandler());

        app.start(7000);
    }

    void stop() {
        if (app != null) {
            app.stop();
        }
    }
}
