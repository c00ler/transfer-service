package com.revolut.transfer;

import io.javalin.Javalin;

public class Launcher {

    public static void main(String[] args) {
        var app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        }).start(7000);

        app.get("/", ctx -> ctx.result("Hello World"));
    }

}
