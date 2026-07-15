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
        apply(event, "/db/04-admin-upgrade.sql", "Admin");
        apply(event, "/db/05-hrm-manager.sql", "HR Manager");
    }

    private void apply(ServletContextEvent event, String resource, String label) {
        try (InputStream input = getClass().getResourceAsStream(resource)) {
            if (input == null) throw new IllegalStateException("Missing database migration resource: " + resource);
            String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection connection = Database.getConnection()) {
                for (String batch : script.split("(?im)^\\s*GO\\s*$")) {
                    if (batch.isBlank()) continue;
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(batch);
                    }
                }
            }
            event.getServletContext().log(label + " database migration completed");
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Cannot apply " + label + " database migration", e);
        }
    }
}
