package com.group5.hrms.dao;

import com.group5.hrms.config.Database;
import com.group5.hrms.model.CompanyEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompanyEventDao {
    public List<CompanyEvent> findAll() throws SQLException {
        String sql = """
                SELECT event_id, title, event_type, start_at, end_at, description, created_by, created_at
                FROM dbo.company_events
                ORDER BY start_at
                """;
        List<CompanyEvent> events = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) events.add(map(rs));
        }
        return events;
    }

    public List<CompanyEvent> upcoming(int limit) throws SQLException {
        String sql = """
                SELECT TOP (?) event_id, title, event_type, start_at, end_at, description, created_by, created_at
                FROM dbo.company_events
                WHERE end_at >= SYSDATETIME()
                ORDER BY start_at
                """;
        List<CompanyEvent> events = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) events.add(map(rs));
            }
        }
        return events;
    }

    public Optional<CompanyEvent> findById(long id) throws SQLException {
        String sql = """
                SELECT event_id, title, event_type, start_at, end_at, description, created_by, created_at
                FROM dbo.company_events WHERE event_id = ?
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

    public long create(String title, String type, LocalDateTime start, LocalDateTime end, String description, long createdBy)
            throws SQLException {
        String sql = """
                INSERT INTO dbo.company_events(title, event_type, start_at, end_at, description, created_by)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, title.trim());
            statement.setString(2, type);
            statement.setTimestamp(3, Timestamp.valueOf(start));
            statement.setTimestamp(4, Timestamp.valueOf(end));
            statement.setString(5, description);
            statement.setLong(6, createdBy);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void update(long id, String title, String type, LocalDateTime start, LocalDateTime end, String description)
            throws SQLException {
        String sql = """
                UPDATE dbo.company_events
                SET title = ?, event_type = ?, start_at = ?, end_at = ?, description = ?, updated_at = SYSDATETIME()
                WHERE event_id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title.trim());
            statement.setString(2, type);
            statement.setTimestamp(3, Timestamp.valueOf(start));
            statement.setTimestamp(4, Timestamp.valueOf(end));
            statement.setString(5, description);
            statement.setLong(6, id);
            if (statement.executeUpdate() == 0) throw new SQLException("Event not found");
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM dbo.company_events WHERE event_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            if (statement.executeUpdate() == 0) throw new SQLException("Event not found");
        }
    }

    private CompanyEvent map(ResultSet rs) throws SQLException {
        CompanyEvent event = new CompanyEvent();
        event.setId(rs.getLong("event_id"));
        event.setTitle(rs.getString("title"));
        event.setEventType(rs.getString("event_type"));
        event.setStartAt(rs.getTimestamp("start_at").toLocalDateTime());
        event.setEndAt(rs.getTimestamp("end_at").toLocalDateTime());
        event.setDescription(rs.getString("description"));
        event.setCreatedBy(rs.getLong("created_by"));
        event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return event;
    }
}
