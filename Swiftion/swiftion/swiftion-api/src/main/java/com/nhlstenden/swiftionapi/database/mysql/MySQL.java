package com.nhlstenden.swiftionapi.database.mysql;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;

public class MySQL {
    private final Dotenv env;
    private Connection conn;

    public MySQL() {
        this.env = Dotenv.load();
        this.conn = null;
    }

    /**
     * get a MySQL database connection
     *
     * @return open MySQL db connection
     */
    public Connection getConnection(String role) {
        String username = null;
        String password = null;

        if (role.equalsIgnoreCase("2")) {
            username = this.env.get("mysql_username");
            password = this.env.get("mysql_password");
        }else {
            username = this.env.get("mysql_username_user");
            password = this.env.get("mysql_password_user");
        }
        try {
            this.conn = DriverManager.getConnection(
                    String.format(
                            "jdbc:mariadb://%s:%s/%s?allowPublicKeyRetrieval=true&allowMultiQueries=true",
                            this.env.get("mysql_host"), this.env.get("mysql_port"),
                            this.env.get("mysql_database")
                    ),
                    username,
                    password
            );

            // System.out.println("Connected to the database with User: " + username + " and Password: " + password);
        } catch (Exception e) {
            System.err.println(e);
        }
        return this.conn;
    }
}