package com.group5.hrms.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;

public final class Database {

    // Local SQL Server Express — use host:port after TCP/IP is enabled.
    // Override with HRMS_DB_URL / HRMS_DB_USER / HRMS_DB_PASSWORD if needed.
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
        String url = env("HRMS_DB_URL", DEFAULT_URL);
        if (url.toLowerCase(Locale.ROOT).contains("integratedsecurity=true")) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(
                url,
                env("HRMS_DB_USER", "sa"),
                env("HRMS_DB_PASSWORD", "sa"));
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
