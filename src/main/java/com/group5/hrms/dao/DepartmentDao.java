package com.group5.hrms.dao;

import com.group5.hrms.config.Database;
import com.group5.hrms.model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentDao {
    public List<Department> findAll(boolean includeInactive) throws SQLException {
        String sql = """
                SELECT d.department_id, d.department_code, d.department_name, d.status,
                       d.created_at, d.updated_at,
                       (SELECT COUNT(*) FROM employees e
                        WHERE e.department_id = d.department_id AND e.status = 'ACTIVE') AS employee_count
                FROM departments d
                WHERE (? = 1 OR d.status = 'ACTIVE')
                ORDER BY d.department_name
                """;
        List<Department> departments = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, includeInactive ? 1 : 0);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) departments.add(map(rs));
            }
        }
        return departments;
    }

    public Optional<Department> findById(long id) throws SQLException {
        String sql = """
                SELECT d.department_id, d.department_code, d.department_name, d.status,
                       d.created_at, d.updated_at,
                       (SELECT COUNT(*) FROM employees e
                        WHERE e.department_id = d.department_id AND e.status = 'ACTIVE') AS employee_count
                FROM departments d
                WHERE d.department_id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public boolean existsCodeOrName(String code, String name, Long excludeId) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM departments
                WHERE (LOWER(department_code) = LOWER(?) OR LOWER(department_name) = LOWER(?))
                  AND (? IS NULL OR department_id <> ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            statement.setString(2, name);
            if (excludeId == null) {
                statement.setNull(3, java.sql.Types.BIGINT);
                statement.setNull(4, java.sql.Types.BIGINT);
            } else {
                statement.setLong(3, excludeId);
                statement.setLong(4, excludeId);
            }
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public long create(String code, String name) throws SQLException {
        String sql = "INSERT INTO departments(department_code, department_name) VALUES (?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, code.trim());
            statement.setString(2, name.trim());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void update(long id, String code, String name) throws SQLException {
        String sql = """
                UPDATE departments
                SET department_code = ?, department_name = ?, updated_at = CURRENT_TIMESTAMP
                WHERE department_id = ? AND status = 'ACTIVE'
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code.trim());
            statement.setString(2, name.trim());
            statement.setLong(3, id);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Department not found or inactive");
            }
        }
    }

    public void softDelete(long id) throws SQLException {
        String sql = """
                UPDATE departments
                SET status = 'INACTIVE', updated_at = CURRENT_TIMESTAMP
                WHERE department_id = ? AND status = 'ACTIVE'
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Department not found or already inactive");
            }
        }
    }

    private Department map(ResultSet rs) throws SQLException {
        Department department = new Department();
        department.setId(rs.getLong("department_id"));
        department.setCode(rs.getString("department_code"));
        department.setName(rs.getString("department_name"));
        department.setStatus(rs.getString("status"));
        department.setEmployeeCount(rs.getInt("employee_count"));
        department.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        department.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return department;
    }
}
