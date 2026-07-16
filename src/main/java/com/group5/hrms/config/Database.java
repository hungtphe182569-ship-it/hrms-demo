package com.group5.hrms.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    // Local MySQL 8.x. Override with HRMS_DB_URL / HRMS_DB_USER / HRMS_DB_PASSWORD if needed.
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/HRMS_Demo"
            + "?useUnicode=true&characterEncoding=utf8"
            + "&serverTimezone=Asia/Ho_Chi_Minh&useSSL=false&allowPublicKeyRetrieval=true";

    private Database() {
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "Khong tim thay MySQL JDBC Driver: " + e.getMessage()
            );
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                env("HRMS_DB_URL", DEFAULT_URL),
                env("HRMS_DB_USER", "root"),
                env("HRMS_DB_PASSWORD", ""));
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
