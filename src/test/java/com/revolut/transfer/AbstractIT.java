package com.revolut.transfer;

import com.revolut.transfer.util.FlywayUtils;
import io.restassured.RestAssured;
import org.h2.jdbcx.JdbcConnectionPool;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;

public abstract class AbstractIT {

    private static final DataSource DATA_SOURCE = JdbcConnectionPool.create("jdbc:h2:mem:revolut", "sa", "");

    protected static final DSLContext JOOQ = DSL.using(DATA_SOURCE, SQLDialect.H2);

    private static final int PORT = findFreePort();

    private static Application application;

    static {
        FlywayUtils.migrate(DATA_SOURCE);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = PORT;
        RestAssured.basePath = "/api/v1";
    }

    @BeforeAll
    static void startApplication() {
        application = new Application(DATA_SOURCE, PORT);
        application.start();
    }

    @AfterAll
    static void stopServer() {
        if (application != null) {
            application.stop();
        }
    }

    private static int findFreePort() {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
