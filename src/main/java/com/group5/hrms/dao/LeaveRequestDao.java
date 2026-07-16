package com.group5.hrms.dao;

import com.group5.hrms.config.Database;
import com.group5.hrms.model.LeaveRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LeaveRequestDao {
    public List<LeaveRequest> findByStatus(String status, String leaveType) throws SQLException {
        String sql = """
                SELECT lr.leave_id, lr.employee_id, u.full_name, d.department_name, e.leave_balance_days, lr.leave_type,
                       lr.start_date, lr.end_date, lr.days_count, lr.reason, lr.status,
                       lr.rejection_reason, lr.decided_by, lr.decided_at, lr.previous_status, lr.created_at
                FROM leave_requests lr
                JOIN employees e ON e.employee_id = lr.employee_id
                JOIN users u ON u.user_id = e.user_id
                LEFT JOIN departments d ON d.department_id = e.department_id
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

    public List<LeaveRequest> findPending(String leaveType) throws SQLException {
        String sql = """
                SELECT lr.leave_id, lr.employee_id, u.full_name, d.department_name, e.leave_balance_days, lr.leave_type,
                       lr.start_date, lr.end_date, lr.days_count, lr.reason, lr.status,
                       lr.rejection_reason, lr.decided_by, lr.decided_at, lr.previous_status, lr.created_at
                FROM leave_requests lr
                JOIN employees e ON e.employee_id = lr.employee_id
                JOIN users u ON u.user_id = e.user_id
                LEFT JOIN departments d ON d.department_id = e.department_id
                WHERE lr.status IN ('PENDING_HRM','PENDING_HR_MANAGER','PENDING_DEPARTMENT_MANAGER')
                  AND (? IS NULL OR lr.leave_type = ?)
                ORDER BY lr.created_at DESC
                """;
        List<LeaveRequest> requests = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindNullable(statement, 1, 2, leaveType);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) requests.add(map(rs));
            }
        }
        return requests;
    }

    public Optional<LeaveRequest> findById(long id) throws SQLException {
        String sql = """
                SELECT lr.leave_id, lr.employee_id, u.full_name, d.department_name, e.leave_balance_days, lr.leave_type,
                       lr.start_date, lr.end_date, lr.days_count, lr.reason, lr.status,
                       lr.rejection_reason, lr.decided_by, lr.decided_at, lr.previous_status, lr.created_at
                FROM leave_requests lr
                JOIN employees e ON e.employee_id = lr.employee_id
                JOIN users u ON u.user_id = e.user_id
                LEFT JOIN departments d ON d.department_id = e.department_id
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
        String sql = "SELECT COUNT(*) FROM leave_requests WHERE status = ?";
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
                UPDATE leave_requests
                SET previous_status = status,
                    status = ?,
                    rejection_reason = ?,
                    decided_by = ?,
                    decided_at = CURRENT_TIMESTAMP,
                    updated_at = CURRENT_TIMESTAMP
                WHERE leave_id = ? AND status IN ('PENDING_HRM','PENDING_HR_MANAGER','PENDING_DEPARTMENT_MANAGER')
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus);
            if (rejectionReason == null || rejectionReason.isBlank()) statement.setNull(2, Types.VARCHAR);
            else statement.setString(2, rejectionReason.trim());
            statement.setLong(3, decidedBy);
            statement.setLong(4, leaveId);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Leave request not found or not pending");
            }
        }
    }

    public void approve(long leaveId, long decidedBy, boolean overrideBalance, boolean allowPastDate)
            throws SQLException {
        String selectSql = """
                SELECT lr.employee_id, lr.days_count, lr.end_date, lr.status, e.leave_balance_days
                FROM leave_requests lr
                JOIN employees e ON e.employee_id = lr.employee_id
                WHERE lr.leave_id = ?
                FOR UPDATE
                """;
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long employeeId;
                int days;
                BigDecimal balance;
                java.sql.Date endDate;
                String status;
                try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                    statement.setLong(1, leaveId);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Leave request not found");
                        employeeId = rs.getLong("employee_id");
                        days = rs.getInt("days_count");
                        endDate = rs.getDate("end_date");
                        status = rs.getString("status");
                        balance = rs.getBigDecimal("leave_balance_days");
                    }
                }
                if (!List.of("PENDING_HRM", "PENDING_HR_MANAGER", "PENDING_DEPARTMENT_MANAGER").contains(status)) {
                    throw new IllegalArgumentException("Already processed");
                }
                if (endDate != null && endDate.toLocalDate().isBefore(java.time.LocalDate.now()) && !allowPastDate) {
                    throw new IllegalArgumentException("Dates in the past");
                }
                BigDecimal required = BigDecimal.valueOf(days);
                if (balance != null && balance.compareTo(required) < 0 && !overrideBalance) {
                    throw new IllegalArgumentException("Not enough balance");
                }
                try (PreparedStatement statement = connection.prepareStatement("""
                        UPDATE leave_requests
                        SET previous_status = status,
                            status = 'APPROVED',
                            rejection_reason = NULL,
                            decided_by = ?,
                            decided_at = CURRENT_TIMESTAMP,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE leave_id = ?
                        """)) {
                    statement.setLong(1, decidedBy);
                    statement.setLong(2, leaveId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("""
                        UPDATE employees
                        SET leave_balance_days = leave_balance_days - ?
                        WHERE employee_id = ?
                        """)) {
                    statement.setBigDecimal(1, required);
                    statement.setLong(2, employeeId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void revert(long leaveId) throws SQLException {
        String selectSql = """
                SELECT employee_id, days_count, status
                FROM leave_requests
                WHERE leave_id = ?
                  AND status IN ('APPROVED','REJECTED')
                  AND decided_at IS NOT NULL
                  AND decided_at >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 24 HOUR)
                FOR UPDATE
                """;
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Long employeeId = null;
                int days = 0;
                String status = null;
                try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                    statement.setLong(1, leaveId);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            employeeId = rs.getLong("employee_id");
                            days = rs.getInt("days_count");
                            status = rs.getString("status");
                        }
                    }
                }
                if (employeeId == null) throw new SQLException("Cannot revert: window expired or request invalid");
                try (PreparedStatement statement = connection.prepareStatement("""
                        UPDATE leave_requests
                        SET status = 'PENDING_HRM',
                            rejection_reason = NULL,
                            decided_by = NULL,
                            decided_at = NULL,
                            previous_status = status,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE leave_id = ?
                        """)) {
                    statement.setLong(1, leaveId);
                    statement.executeUpdate();
                }
                if ("APPROVED".equals(status)) {
                    try (PreparedStatement statement = connection.prepareStatement("""
                            UPDATE employees
                            SET leave_balance_days = leave_balance_days + ?
                            WHERE employee_id = ?
                            """)) {
                        statement.setBigDecimal(1, BigDecimal.valueOf(days));
                        statement.setLong(2, employeeId);
                        statement.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public long findUserIdByLeave(long leaveId) throws SQLException {
        String sql = """
                SELECT e.user_id FROM leave_requests lr
                JOIN employees e ON e.employee_id = lr.employee_id
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
        leave.setLeaveBalanceDays(rs.getBigDecimal("leave_balance_days"));
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
