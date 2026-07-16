package com.group5.hrms.dao;

import com.group5.hrms.config.Database;
import com.group5.hrms.model.PayrollBatch;
import com.group5.hrms.model.Payslip;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PayrollDao {
    public List<PayrollBatch> findAll() throws SQLException {
        String sql = """
                SELECT batch_id, period_label, period_start, period_end, status, total_amount,
                       employee_count, locked, rejection_reason, approved_by, approved_at, created_at
                FROM payroll_batches
                ORDER BY period_start DESC, batch_id DESC
                """;
        List<PayrollBatch> batches = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) batches.add(mapBatch(rs));
        }
        return batches;
    }

    public Optional<PayrollBatch> findById(long id) throws SQLException {
        String sql = """
                SELECT batch_id, period_label, period_start, period_end, status, total_amount,
                       employee_count, locked, rejection_reason, approved_by, approved_at, created_at
                FROM payroll_batches WHERE batch_id = ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return Optional.of(mapBatch(rs));
            }
        }
        return Optional.empty();
    }

    public List<Payslip> findPayslips(long batchId) throws SQLException {
        String sql = """
                SELECT p.payslip_id, p.batch_id, p.employee_id, u.full_name, d.department_name,
                       p.gross_pay, p.net_pay, p.bank_account, p.published
                FROM payslips p
                JOIN employees e ON e.employee_id = p.employee_id
                JOIN users u ON u.user_id = e.user_id
                LEFT JOIN departments d ON d.department_id = e.department_id
                WHERE p.batch_id = ?
                ORDER BY u.full_name
                """;
        List<Payslip> payslips = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, batchId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Payslip payslip = new Payslip();
                    payslip.setId(rs.getLong("payslip_id"));
                    payslip.setBatchId(rs.getLong("batch_id"));
                    payslip.setEmployeeId(rs.getLong("employee_id"));
                    payslip.setEmployeeName(rs.getString("full_name"));
                    payslip.setDepartmentName(rs.getString("department_name"));
                    payslip.setGrossPay(rs.getBigDecimal("gross_pay"));
                    payslip.setNetPay(rs.getBigDecimal("net_pay"));
                    payslip.setBankAccount(rs.getString("bank_account"));
                    payslip.setPublished(rs.getBoolean("published"));
                    payslips.add(payslip);
                }
            }
        }
        return payslips;
    }

    public int countSubmitted() throws SQLException {
        String sql = "SELECT COUNT(*) FROM payroll_batches WHERE status = 'SUBMITTED'";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public BigDecimal sumSubmittedAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM payroll_batches WHERE status = 'SUBMITTED'";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    public void finalApprove(long batchId, long approvedBy) throws SQLException {
        String sql = """
                UPDATE payroll_batches
                SET status = 'APPROVED_FINAL', locked = 1, approved_by = ?, approved_at = CURRENT_TIMESTAMP,
                    rejection_reason = NULL, updated_at = CURRENT_TIMESTAMP
                WHERE batch_id = ? AND status = 'SUBMITTED' AND locked = 0
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, approvedBy);
            statement.setLong(2, batchId);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Payroll batch cannot be approved");
            }
        }
        publishPayslips(batchId);
    }

    public void reject(long batchId, long approvedBy, String reason) throws SQLException {
        String sql = """
                UPDATE payroll_batches
                SET status = 'REJECTED', approved_by = ?, approved_at = CURRENT_TIMESTAMP,
                    rejection_reason = ?, updated_at = CURRENT_TIMESTAMP
                WHERE batch_id = ? AND status = 'SUBMITTED' AND locked = 0
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, approvedBy);
            statement.setString(2, reason.trim());
            statement.setLong(3, batchId);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("Payroll batch cannot be rejected");
            }
        }
    }

    private void publishPayslips(long batchId) throws SQLException {
        String sql = "UPDATE payslips SET published = 1 WHERE batch_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, batchId);
            statement.executeUpdate();
        }
    }

    public List<Long> findEmployeeUserIds(long batchId) throws SQLException {
        String sql = """
                SELECT e.user_id FROM payslips p
                JOIN employees e ON e.employee_id = p.employee_id
                WHERE p.batch_id = ?
                """;
        List<Long> ids = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, batchId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) ids.add(rs.getLong(1));
            }
        }
        return ids;
    }

    private PayrollBatch mapBatch(ResultSet rs) throws SQLException {
        PayrollBatch batch = new PayrollBatch();
        batch.setId(rs.getLong("batch_id"));
        batch.setPeriodLabel(rs.getString("period_label"));
        batch.setPeriodStart(rs.getDate("period_start").toLocalDate());
        batch.setPeriodEnd(rs.getDate("period_end").toLocalDate());
        batch.setStatus(rs.getString("status"));
        batch.setTotalAmount(rs.getBigDecimal("total_amount"));
        batch.setEmployeeCount(rs.getInt("employee_count"));
        batch.setLocked(rs.getBoolean("locked"));
        batch.setRejectionReason(rs.getString("rejection_reason"));
        long approvedBy = rs.getLong("approved_by");
        if (!rs.wasNull()) batch.setApprovedBy(approvedBy);
        if (rs.getTimestamp("approved_at") != null) {
            batch.setApprovedAt(rs.getTimestamp("approved_at").toLocalDateTime());
        }
        batch.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return batch;
    }
}
