package com.group5.hrms.dao;

import com.group5.hrms.config.Database;
import com.group5.hrms.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RoleDao {
    public List<Role> findAll() throws SQLException {
        String sql = """
                SELECT r.role_id, r.role_name, r.description, r.created_at,
                       COUNT(CASE WHEN u.status <> 'DELETED' THEN 1 END) AS assigned_users
                FROM roles r
                LEFT JOIN user_roles ur ON ur.role_id = r.role_id
                LEFT JOIN users u ON u.user_id = ur.user_id
                GROUP BY r.role_id, r.role_name, r.description, r.created_at
                ORDER BY r.role_name
                """;
        List<Role> roles = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) roles.add(map(rs));
        }
        return roles;
    }

    public boolean exists(long roleId) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM roles WHERE role_id = ?")) {
            statement.setLong(1, roleId);
            try (ResultSet rs = statement.executeQuery()) { return rs.next(); }
        }
    }

    public boolean existsName(String roleName, Long excludedId) throws SQLException {
        String sql = "SELECT 1 FROM roles WHERE role_name = ? AND (? IS NULL OR role_id <> ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roleName);
            if (excludedId == null) {
                statement.setNull(2, java.sql.Types.BIGINT);
                statement.setNull(3, java.sql.Types.BIGINT);
            } else {
                statement.setLong(2, excludedId);
                statement.setLong(3, excludedId);
            }
            try (ResultSet rs = statement.executeQuery()) { return rs.next(); }
        }
    }

    public long create(String name, String description) throws SQLException {
        String sql = "INSERT INTO roles(role_name, description) VALUES (?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Cannot read generated role ID");
                return keys.getLong(1);
            }
        }
    }

    public void update(long roleId, String name, String description) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE roles SET role_name = ?, description = ? WHERE role_id = ?")) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setLong(3, roleId);
            if (statement.executeUpdate() != 1) throw new IllegalArgumentException("Role not found");
        }
    }

    public void delete(long roleId) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM user_roles WHERE role_id = ?";
        try (Connection connection = Database.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(countSql)) {
                statement.setLong(1, roleId);
                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) throw new IllegalStateException("Role is assigned to users");
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM roles WHERE role_id = ?")) {
                statement.setLong(1, roleId);
                if (statement.executeUpdate() != 1) throw new IllegalArgumentException("Role not found");
            }
        }
    }

    private Role map(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getLong("role_id"));
        role.setName(rs.getString("role_name"));
        role.setDescription(rs.getString("description"));
        role.setAssignedUsers(rs.getInt("assigned_users"));
        role.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return role;
    }
}
