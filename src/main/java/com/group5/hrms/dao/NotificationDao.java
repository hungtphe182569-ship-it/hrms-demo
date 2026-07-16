package com.group5.hrms.dao;

import com.group5.hrms.config.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NotificationDao {
    public void notifyUser(long userId, String title, String message) throws SQLException {
        String sql = "INSERT INTO notifications(user_id, title, message) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, title);
            statement.setString(3, message);
            statement.executeUpdate();
        }
    }

    public void notifyAllEmployees(String title, String message) throws SQLException {
        String sql = """
                INSERT INTO notifications(user_id, title, message)
                SELECT e.user_id, ?, ?
                FROM employees e
                JOIN users u ON u.user_id = e.user_id
                WHERE e.status = 'ACTIVE' AND u.status = 'ACTIVE'
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, message);
            statement.executeUpdate();
        }
    }

    public void notifyAdmins(String title, String message) throws SQLException {
        String sql = """
                INSERT INTO notifications(user_id, title, message)
                SELECT DISTINCT u.user_id, ?, ?
                FROM users u
                JOIN user_roles ur ON ur.user_id = u.user_id
                JOIN roles r ON r.role_id = ur.role_id
                WHERE u.status = 'ACTIVE' AND r.role_name = 'Admin'
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, message);
            statement.executeUpdate();
        }
    }
}
