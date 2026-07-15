package com.group5.hrms.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@WebListener
public class DatabaseMigration implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try (InputStream input = getClass().getResourceAsStream("/db/04-admin-upgrade.sql")) {
            if (input == null) throw new IllegalStateException("Missing database migration resource");
            String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection connection = Database.getConnection()) {
                for (String batch : script.split("(?im)^\\s*GO\\s*$")) {
                    if (batch.isBlank()) continue;
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(batch);
                    }
                }
            }
            event.getServletContext().log("Admin database migration completed");
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Cannot apply Admin database migration", e);
        }
    }
}
