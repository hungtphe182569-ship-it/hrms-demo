package com.group5.hrms.dao;

import com.group5.hrms.config.Database;
import com.group5.hrms.model.AuditEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuditDao {
    public List<AuditEntry> findRecent(int limit) throws SQLException {
        String sql = """
                SELECT TOP (?) a.audit_id, a.action_name, a.reason, a.performed_at,
                       target.username AS account_name, actor.username AS performed_by
                FROM dbo.account_audit_log a
                JOIN dbo.users target ON target.user_id = a.account_id
                JOIN dbo.users actor ON actor.user_id = a.performed_by
                ORDER BY a.performed_at DESC, a.audit_id DESC
                """;
        List<AuditEntry> entries = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(1, Math.min(limit, 20)));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    AuditEntry entry = new AuditEntry();
                    entry.setId(rs.getLong("audit_id"));
                    entry.setAccountName(rs.getString("account_name"));
                    entry.setAction(rs.getString("action_name"));
                    entry.setReason(rs.getString("reason"));
                    entry.setPerformedBy(rs.getString("performed_by"));
                    entry.setPerformedAt(rs.getTimestamp("performed_at").toLocalDateTime());
                    entries.add(entry);
                }
            }
        }
        return entries;
    }
}
