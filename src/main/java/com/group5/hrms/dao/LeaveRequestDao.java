package com.group5.hrms.dao;

import com.group5.hrms.config.Database;
import com.group5.hrms.model.LeaveRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LeaveRequestDao {
    public List<LeaveRequest> findByStatus(String status, String leaveType) throws SQLException {
        String sql = """
                SELECT lr.leave_id, lr.employee_id, u.full_name, d.department_name, lr.leave_type,
                       lr.start_date, lr.end_date, lr.days_count, lr.reason, lr.status,
                       lr.rejection_reason, lr.decided_by, lr.decided_at, lr.previous_status, lr.created_at
                FROM dbo.leave_requests lr
                JOIN dbo.employees e ON e.employee_id = lr.employee_id
                JOIN dbo.users u ON u.user_id = e.user_id
                LEFT JOIN dbo.departments d ON d.department_id = e.department_id
                WHERE (? IS NULL OR lr.status = ?)
                  AND (? IS NULL OR lr.leave_type = ?)
                ORDER BY lr.created_at DESC
                """;
        List<LeaveRequest> requests = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindNullable(statement, 1, 2, status);
            bindNullable(statement, 3, 4, leaveType);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) requests.add(map(rs));
            }
        }
        return requests;
    }

    public Optional<LeaveRequest> findById(long id) throws SQLException {
        String sql = """
                SELECT lr.leave_id, lr.employee_id, u.full_name, d.department_name, lr.leave_type,
                       lr.start_date, lr.end_date, lr.days_count, lr.reason, lr.status,
                       lr.rejection_reason, lr.decided_by, lr.decided_at, lr.previous_status, lr.created_at
                FROM dbo.leave_requests lr
                JOIN dbo.employees e ON e.employee_id = lr.employee_id
                JOIN dbo.users u ON u.user_id = e.user_id
                LEFT JOIN dbo.departments d ON d.department_id = e.department_id
                WHERE lr.leave_id = ?
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

    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.leave_requests WHERE status = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void decide(long leaveId, String newStatus, String rejectionReason, long decidedBy) throws SQLException {
        String sql = """
                UPDATE dbo.leave_requests
                SET previous_status = status,
                    status = ?,
                    rejection_reason = ?,
                    decided_by = ?,
                    decided_at = SYSDATETIME(),
                    updated_at = SYSDATETIME()
                WHERE leave_id = ? AND status = 'PENDING_HRM'
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus);
            if (rejectionReason == null || rejectionReason.isBlank()) statement.setNull(2, Types.NVARCHAR);
            else statement.setString(2, rejectionReason.trim());
            statement.setLong(3, decidedBy);
            statement.setLong(4, leaveId);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Leave request not found or not pending");
            }
        }
    }

    public void revert(long leaveId) throws SQLException {
        String sql = """
                UPDATE dbo.leave_requests
                SET status = 'PENDING_HRM',
                    rejection_reason = NULL,
                    decided_by = NULL,
                    decided_at = NULL,
                    previous_status = status,
                    updated_at = SYSDATETIME()
                WHERE leave_id = ?
                  AND status IN ('APPROVED','REJECTED')
                  AND decided_at IS NOT NULL
                  AND decided_at >= DATEADD(HOUR, -24, SYSDATETIME())
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, leaveId);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Cannot revert: window expired or request invalid");
            }
        }
    }

    public long findUserIdByLeave(long leaveId) throws SQLException {
        String sql = """
                SELECT e.user_id FROM dbo.leave_requests lr
                JOIN dbo.employees e ON e.employee_id = lr.employee_id
                WHERE lr.leave_id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, leaveId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new SQLException("Leave request not found");
                return rs.getLong(1);
            }
        }
    }

    private void bindNullable(PreparedStatement statement, int a, int b, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(a, Types.VARCHAR);
            statement.setNull(b, Types.VARCHAR);
        } else {
            statement.setString(a, value);
            statement.setString(b, value);
        }
    }

    private LeaveRequest map(ResultSet rs) throws SQLException {
        LeaveRequest leave = new LeaveRequest();
        leave.setId(rs.getLong("leave_id"));
        leave.setEmployeeId(rs.getLong("employee_id"));
        leave.setEmployeeName(rs.getString("full_name"));
        leave.setDepartmentName(rs.getString("department_name"));
        leave.setLeaveType(rs.getString("leave_type"));
        leave.setStartDate(rs.getDate("start_date").toLocalDate());
        leave.setEndDate(rs.getDate("end_date").toLocalDate());
        leave.setDaysCount(rs.getInt("days_count"));
        leave.setReason(rs.getString("reason"));
        leave.setStatus(rs.getString("status"));
        leave.setRejectionReason(rs.getString("rejection_reason"));
        long decidedBy = rs.getLong("decided_by");
        if (!rs.wasNull()) leave.setDecidedBy(decidedBy);
        if (rs.getTimestamp("decided_at") != null) {
            leave.setDecidedAt(rs.getTimestamp("decided_at").toLocalDateTime());
        }
        leave.setPreviousStatus(rs.getString("previous_status"));
        leave.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return leave;
    }
}
