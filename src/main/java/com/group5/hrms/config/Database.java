package com.group5.hrms.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
            + "databaseName=HRMS_Demo;"
            + "encrypt=true;"
            + "trustServerCertificate=true";

    private static final String USER = "sa";
    private static final String PASSWORD = "sa";

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
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}