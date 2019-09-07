package com.revolut.transfer;

import com.revolut.transfer.util.FlywayUtils;
import org.h2.jdbcx.JdbcConnectionPool;

public final class Launcher {

    public static void main(String[] args) {
        var dataSource = JdbcConnectionPool.create("jdbc:h2:mem:revolut", "sa", "");
        FlywayUtils.migrate(dataSource);

        new Application(dataSource, 8080).start();
    }

}
