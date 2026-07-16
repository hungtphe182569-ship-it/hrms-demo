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
import java.util.ArrayList;
import java.util.List;

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
                for (String statementSql : splitStatements(script)) {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(statementSql);
                    } catch (SQLException e) {
                        if (isIgnorableMigrationError(e)) {
                            event.getServletContext().log(
                                    label + " database migration skipped already-applied statement: " + e.getMessage());
                            continue;
                        }
                        throw e;
                    }
                }
            }
            event.getServletContext().log(label + " database migration completed");
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Cannot apply " + label + " database migration", e);
        }
    }

    private List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);
            char next = i + 1 < script.length() ? script.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (c == '\n' || c == '\r') {
                    inLineComment = false;
                    current.append(c);
                }
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && c == '-' && next == '-') {
                inLineComment = true;
                i++;
                continue;
            }
            if (!inSingleQuote && !inDoubleQuote && c == '/' && next == '*') {
                inBlockComment = true;
                i++;
                continue;
            }

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            }

            if (c == ';' && !inSingleQuote && !inDoubleQuote) {
                addStatement(statements, current);
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        addStatement(statements, current);
        return statements;
    }

    private void addStatement(List<String> statements, StringBuilder current) {
        String statement = current.toString().trim();
        if (!statement.isBlank()) {
            statements.add(statement);
        }
    }

    private boolean isIgnorableMigrationError(SQLException e) {
        int errorCode = e.getErrorCode();
        return errorCode == 1050   // table exists
                || errorCode == 1060   // duplicate column
                || errorCode == 1061   // duplicate index/key name
                || errorCode == 1062   // duplicate data from legacy non-IGNORE statements
                || errorCode == 1826;  // duplicate foreign key constraint name
    }
}
