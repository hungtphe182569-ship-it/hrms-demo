package com.group5.hrms.dao;

import com.group5.hrms.config.Database;
import com.group5.hrms.model.EmployeeRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeRequestDao {
    public List<EmployeeRequest> findPending() throws SQLException {
        return findByWhere("er.status IN ('PENDING_HR','PENDING_HR_MANAGER')");
    }

    public List<EmployeeRequest> findProcessed() throws SQLException {
        return findByWhere("er.status NOT IN ('PENDING_HR','PENDING_HR_MANAGER')");
    }

    public Optional<EmployeeRequest> findById(long id) throws SQLException {
        String sql = baseSelect() + " WHERE er.request_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public String approve(long requestId, long hrmId) throws SQLException {
        EmployeeRequest request = findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!List.of("PENDING_HR", "PENDING_HR_MANAGER").contains(request.getStatus())) {
            throw new IllegalArgumentException("Already processed");
        }
        String targetStatus = request.requiresAdminApproval() ? "PENDING_ADMIN" : "APPROVED";
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement("""
                        UPDATE employee_requests
                        SET status = ?, decided_by = ?, decided_at = CURRENT_TIMESTAMP,
                            rejection_reason = NULL, updated_at = CURRENT_TIMESTAMP
                        WHERE request_id = ? AND status IN ('PENDING_HR','PENDING_HR_MANAGER')
                        """)) {
                    statement.setString(1, targetStatus);
                    statement.setLong(2, hrmId);
                    statement.setLong(3, requestId);
                    if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Already processed");
                }
                if ("APPROVED".equals(targetStatus)) {
                    applyApprovedChange(connection, request);
                }
                connection.commit();
                return targetStatus;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void reject(long requestId, long hrmId, String reason) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     UPDATE employee_requests
                     SET status = 'REJECTED', rejection_reason = ?, decided_by = ?,
                         decided_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
                     WHERE request_id = ? AND status IN ('PENDING_HR','PENDING_HR_MANAGER')
                     """)) {
            statement.setString(1, reason.trim());
            statement.setLong(2, hrmId);
            statement.setLong(3, requestId);
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Already processed");
        }
    }

    private void applyApprovedChange(Connection connection, EmployeeRequest request) throws SQLException {
        if ("SALARY_CHANGE".equals(request.getCategory()) && request.getProposedSalary() != null) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE employees SET base_salary = ? WHERE employee_id = ?
                    """)) {
                statement.setBigDecimal(1, request.getProposedSalary());
                statement.setLong(2, request.getEmployeeId());
                statement.executeUpdate();
            }
        } else if ("TRANSFER".equals(request.getCategory()) && request.getTargetDepartmentId() != null) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE employees SET department_id = ? WHERE employee_id = ?
                    """)) {
                statement.setLong(1, request.getTargetDepartmentId());
                statement.setLong(2, request.getEmployeeId());
                statement.executeUpdate();
            }
        } else if ("PROMOTION".equals(request.getCategory())) {
            throw new IllegalStateException("Promotion must be forwarded to Admin");
        }
    }

    private List<EmployeeRequest> findByWhere(String where) throws SQLException {
        String sql = baseSelect() + " WHERE " + where + " ORDER BY er.created_at DESC";
        List<EmployeeRequest> requests = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) requests.add(map(rs));
        }
        return requests;
    }

    private String baseSelect() {
        return """
                SELECT er.request_id, er.employee_id, e.user_id AS requester_user_id,
                       u.full_name, d.department_name, er.category, er.title, er.description,
                       er.status, er.target_department_id, td.department_name AS target_department_name,
                       er.proposed_salary, er.proposed_position, er.rejection_reason,
                       er.decided_by, er.decided_at, er.created_at
                FROM employee_requests er
                JOIN employees e ON e.employee_id = er.employee_id
                JOIN users u ON u.user_id = e.user_id
                LEFT JOIN departments d ON d.department_id = e.department_id
                LEFT JOIN departments td ON td.department_id = er.target_department_id
                """;
    }

    private EmployeeRequest map(ResultSet rs) throws SQLException {
        EmployeeRequest request = new EmployeeRequest();
        request.setId(rs.getLong("request_id"));
        request.setEmployeeId(rs.getLong("employee_id"));
        request.setRequesterUserId(rs.getLong("requester_user_id"));
        request.setRequesterName(rs.getString("full_name"));
        request.setDepartmentName(rs.getString("department_name"));
        request.setCategory(rs.getString("category"));
        request.setTitle(rs.getString("title"));
        request.setDescription(rs.getString("description"));
        request.setStatus(rs.getString("status"));
        long targetDepartmentId = rs.getLong("target_department_id");
        if (!rs.wasNull()) request.setTargetDepartmentId(targetDepartmentId);
        request.setTargetDepartmentName(rs.getString("target_department_name"));
        request.setProposedSalary(rs.getBigDecimal("proposed_salary"));
        request.setProposedPosition(rs.getString("proposed_position"));
        request.setRejectionReason(rs.getString("rejection_reason"));
        long decidedBy = rs.getLong("decided_by");
        if (!rs.wasNull()) request.setDecidedBy(decidedBy);
        if (rs.getTimestamp("decided_at") != null) {
            request.setDecidedAt(rs.getTimestamp("decided_at").toLocalDateTime());
        }
        request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return request;
    }
}
