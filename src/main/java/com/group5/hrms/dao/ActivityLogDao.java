package com.group5.hrms.dao;

import com.group5.hrms.config.Database;
import com.group5.hrms.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {
    public void log(Long actorUserId, String action, String entityType, Long entityId, String details) throws SQLException {
        String sql = """
                INSERT INTO dbo.activity_logs(actor_user_id, action_name, entity_type, entity_id, details)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (actorUserId == null) statement.setNull(1, Types.BIGINT);
            else statement.setLong(1, actorUserId);
            statement.setString(2, action);
            statement.setString(3, entityType);
            if (entityId == null) statement.setNull(4, Types.BIGINT);
            else statement.setLong(4, entityId);
            statement.setString(5, details);
            statement.executeUpdate();
        }
    }

    public List<ActivityLog> search(String keyword, String actionName, int page, int pageSize) throws SQLException {
        int offset = Math.max(page - 1, 0) * pageSize;
        String sql = """
                SELECT a.activity_id, a.actor_user_id, u.full_name, a.action_name, a.entity_type,
                       a.entity_id, a.details, a.created_at
                FROM dbo.activity_logs a
                LEFT JOIN dbo.users u ON u.user_id = a.actor_user_id
                WHERE (? IS NULL OR a.action_name = ?)
                  AND (? IS NULL OR a.action_name LIKE ? OR a.details LIKE ?
                       OR CAST(a.actor_user_id AS NVARCHAR(20)) LIKE ? OR u.full_name LIKE ?)
                ORDER BY a.created_at DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindSearch(statement, keyword, actionName);
            statement.setInt(8, offset);
            statement.setInt(9, pageSize);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) logs.add(map(rs));
            }
        }
        return logs;
    }

    public int count(String keyword, String actionName) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM dbo.activity_logs a
                LEFT JOIN dbo.users u ON u.user_id = a.actor_user_id
                WHERE (? IS NULL OR a.action_name = ?)
                  AND (? IS NULL OR a.action_name LIKE ? OR a.details LIKE ?
                       OR CAST(a.actor_user_id AS NVARCHAR(20)) LIKE ? OR u.full_name LIKE ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindSearch(statement, keyword, actionName);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<ActivityLog> recent(int limit) throws SQLException {
        return search(null, null, 1, limit);
    }

    private void bindSearch(PreparedStatement statement, String keyword, String actionName) throws SQLException {
        if (actionName == null || actionName.isBlank()) {
            statement.setNull(1, Types.VARCHAR);
            statement.setNull(2, Types.VARCHAR);
        } else {
            statement.setString(1, actionName);
            statement.setString(2, actionName);
        }
        if (keyword == null || keyword.isBlank()) {
            statement.setNull(3, Types.VARCHAR);
            statement.setNull(4, Types.VARCHAR);
            statement.setNull(5, Types.VARCHAR);
            statement.setNull(6, Types.VARCHAR);
            statement.setNull(7, Types.VARCHAR);
        } else {
            String like = "%" + keyword.trim() + "%";
            statement.setString(3, keyword);
            statement.setString(4, like);
            statement.setString(5, like);
            statement.setString(6, like);
            statement.setString(7, like);
        }
    }

    private ActivityLog map(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setId(rs.getLong("activity_id"));
        long actorId = rs.getLong("actor_user_id");
        if (!rs.wasNull()) log.setActorUserId(actorId);
        log.setActorName(rs.getString("full_name"));
        log.setActionName(rs.getString("action_name"));
        log.setEntityType(rs.getString("entity_type"));
        long entityId = rs.getLong("entity_id");
        if (!rs.wasNull()) log.setEntityId(entityId);
        log.setDetails(rs.getString("details"));
        log.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return log;
    }
}
