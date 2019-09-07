package com.revolut.transfer.util;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public final class FlywayUtils {

    private FlywayUtils() {
    }

    public static void migrate(final DataSource dataSource) {
        var flyway = Flyway.configure().dataSource(dataSource).load();

        flyway.migrate();
    }
}
