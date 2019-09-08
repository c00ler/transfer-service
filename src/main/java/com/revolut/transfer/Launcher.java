package com.revolut.transfer;

import com.revolut.transfer.util.FlywayUtils;
import org.h2.jdbcx.JdbcConnectionPool;

/**
 * Service entry point.
 */
public final class Launcher {

    public static void main(String[] args) {
        var dataSource = JdbcConnectionPool.create("jdbc:h2:mem:revolut", "sa", "");
        FlywayUtils.migrate(dataSource);

        var port = Integer.parseInt(System.getProperty("server.port", "7000"));
        var app = new Application(dataSource, port);
        app.start();

        Runtime.getRuntime().addShutdownHook(new Thread(app::stop, "ShutdownHook"));
    }
}
