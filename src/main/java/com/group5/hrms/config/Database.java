package com.group5.hrms.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    private static final String DEFAULT_URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=HRMS_Demo;encrypt=true;trustServerCertificate=true";

    private Database() {
    }

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "Không tìm thấy SQL Server JDBC Driver: " + e.getMessage()
            );
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                env("HRMS_DB_URL", DEFAULT_URL),
                env("HRMS_DB_USER", "sa"),
                env("HRMS_DB_PASSWORD", "sa"));
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
