package org.cduggan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private static final String DB_URL_ENV = "DB_URL";
    private static final String DB_USER_ENV = "DB_USER";
    private static final String DB_PASSWORD_ENV = "DB_PASSWORD";
    private DatabaseConnection() {
//        try {
//            String dbUrl = Optional.ofNullable(System.getenv(DB_URL_ENV))
//                    .orElseThrow(() -> new IllegalArgumentException("DB_URL environment variable not set"));
//            String dbUser = Optional.ofNullable(System.getenv(DB_USER_ENV))
//                    .orElseThrow(() -> new IllegalArgumentException("DB_USER environment variable not set"));
//            String dbPassword = Optional.ofNullable(System.getenv(DB_PASSWORD_ENV))
//                    .orElseThrow(() -> new IllegalArgumentException("DB_PASSWORD environment variable not set"));
//
//            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
//            System.out.println("Database connection established successfully.");
//        } catch (SQLException e) {
//            System.err.println("Database connection failed: " + e.getMessage());
//        }
    }

    // Accessor
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Failed to close database connection: " + e.getMessage());
            }
        }
    }
}
