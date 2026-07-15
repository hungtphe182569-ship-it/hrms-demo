package com.group5.hrms.dao;

import com.group5.hrms.config.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PermissionDao {
    public boolean hasPermission(long accountId, String permissionCode) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            if (!tableExists(connection, "role_permissions")) return false;
            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT 1
                    FROM dbo.user_roles ur
                    JOIN dbo.role_permissions rp ON rp.role_id = ur.role_id
                    JOIN dbo.permissions p ON p.permission_id = rp.permission_id
                    WHERE ur.user_id = ? AND p.permission_code = ?
                    """)) {
                statement.setLong(1, accountId);
                statement.setString(2, permissionCode);
                try (ResultSet rs = statement.executeQuery()) { return rs.next(); }
            }
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM sys.tables WHERE name = ?")) {
            statement.setString(1, tableName);
            try (ResultSet rs = statement.executeQuery()) { return rs.next(); }
        }
    }
}
