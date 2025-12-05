package com.dbvs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private final String url;
    private final String user;
    private final String password;

    public DbConnection(
            String url,
            String user,
            String password
    ) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() {
        loadDriver();
        Connection pgConn = null;
        try {
            pgConn = DriverManager.getConnection(url, user, password) ;
        }
        catch (SQLException e) {
            System.out.println("Couldn't connect to database!");
            System.exit(1);
        }
        System.out.println("Successfully connected to Postgres Database");

        return pgConn;
    }

    private void loadDriver()
    {
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (ClassNotFoundException cnfe) {
            System.out.println("Couldn't find driver class!");
            System.exit(1);
        }
    }
}
