package com.group5.hrms.dao;

import com.group5.hrms.config.Database;
import com.group5.hrms.model.DashboardStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportDao {
    public DashboardStats loadDashboardStats() throws SQLException {
        DashboardStats stats = new DashboardStats();
        try (Connection connection = Database.getConnection()) {
            loadTotals(connection, stats);
            stats.setAccountsByStatus(loadMap(connection, """
                    SELECT status AS label, COUNT(*) AS total
                    FROM users GROUP BY status ORDER BY status
                    """));
            stats.setAccountsByRole(loadMap(connection, """
                    SELECT r.role_name AS label, COUNT(u.user_id) AS total
                    FROM roles r
                    LEFT JOIN user_roles ur ON ur.role_id = r.role_id
                    LEFT JOIN users u ON u.user_id = ur.user_id AND u.status <> 'DELETED'
                    GROUP BY r.role_name ORDER BY r.role_name
                    """));
            if (tableExists(connection, "permissions") && tableExists(connection, "role_permissions")) {
                stats.setPermissionsByRole(loadMap(connection, """
                        SELECT r.role_name AS label, COUNT(rp.permission_id) AS total
                        FROM roles r
                        LEFT JOIN role_permissions rp ON rp.role_id = r.role_id
                        GROUP BY r.role_name ORDER BY r.role_name
                        """));
                stats.setTotalPermissions(loadCount(connection, "SELECT COUNT(*) FROM permissions"));
            }
            if (tableExists(connection, "attendance")) {
                stats.setAttendanceByStatus(loadMap(connection, """
                        SELECT status AS label, COUNT(*) AS total
                        FROM attendance GROUP BY status ORDER BY status
                        """));
                stats.setAttendanceRecords(loadCount(connection, "SELECT COUNT(*) FROM attendance"));
            }
        }
        return stats;
    }

    private void loadTotals(Connection connection, DashboardStats stats) throws SQLException {
        String sql = """
                SELECT
                    COUNT(*) AS total_accounts,
                    SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_accounts,
                    SUM(CASE WHEN status = 'LOCKED' THEN 1 ELSE 0 END) AS locked_accounts,
                    SUM(CASE WHEN status = 'DELETED' THEN 1 ELSE 0 END) AS deleted_accounts,
                    (SELECT COUNT(*) FROM roles) AS total_roles
                FROM users
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            stats.setTotalAccounts(rs.getInt("total_accounts"));
            stats.setActiveAccounts(rs.getInt("active_accounts"));
            stats.setLockedAccounts(rs.getInt("locked_accounts"));
            stats.setDeletedAccounts(rs.getInt("deleted_accounts"));
            stats.setTotalRoles(rs.getInt("total_roles"));
        }
    }

    private Map<String, Integer> loadMap(Connection connection, String sql) throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.put(rs.getString("label"), rs.getInt("total"));
        }
        return result;
    }

    private int loadCount(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?")) {
            statement.setString(1, tableName);
            try (ResultSet rs = statement.executeQuery()) { return rs.next(); }
        }
    }
}
