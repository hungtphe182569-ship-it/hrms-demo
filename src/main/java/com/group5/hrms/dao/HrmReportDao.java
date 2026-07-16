package com.group5.hrms.dao;

import com.group5.hrms.config.Database;
import com.group5.hrms.model.HrmReportData;
import com.group5.hrms.model.HrmReportFilter;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HrmReportDao {
    public Optional<HrmReportData> findCached(String reportType, HrmReportFilter filter) throws SQLException {
        String sql = """
                SELECT report_id, report_type, report_payload, generated_at, cache_expires_at
                FROM HR_REPORT
                WHERE report_type = ?
                  AND ((? IS NULL AND report_year IS NULL) OR report_year = ?)
                  AND ((? IS NULL AND date_from IS NULL) OR date_from = ?)
                  AND ((? IS NULL AND date_to IS NULL) OR date_to = ?)
                  AND ((? IS NULL AND review_period IS NULL) OR review_period = ?)
                  AND ((? IS NULL AND department_id IS NULL) OR department_id = ?)
                  AND cache_expires_at > CURRENT_TIMESTAMP
                ORDER BY generated_at DESC
                LIMIT 1
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setString(index++, reportType);
            index = bindNullableInt(statement, index, filter.getYear());
            index = bindNullableDate(statement, index, filter.getDateFrom());
            index = bindNullableDate(statement, index, filter.getDateTo());
            index = bindNullableString(statement, index, filter.getReviewPeriod());
            bindNullableLong(statement, index, filter.getDepartmentId());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    HrmReportData data = deserialize(rs.getString("report_payload"));
                    data.setReportId(rs.getLong("report_id"));
                    data.setReportType(rs.getString("report_type"));
                    data.setGeneratedAt(rs.getTimestamp("generated_at").toLocalDateTime());
                    return Optional.of(data);
                }
            }
        }
        return Optional.empty();
    }

    public HrmReportData load(String reportType, HrmReportFilter filter) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            return switch (reportType) {
                case "ATTENDANCE" -> attendance(connection, filter);
                case "LEAVE" -> leave(connection, filter);
                case "PAYROLL" -> payroll(connection, filter);
                case "PERFORMANCE" -> performance(connection, filter);
                default -> overview(connection, filter);
            };
        }
    }

    public long saveReport(HrmReportData data, HrmReportFilter filter) throws SQLException {
        String sql = """
                INSERT INTO HR_REPORT(
                    report_type, report_year, date_from, date_to, review_period, department_id,
                    report_payload, generated_at, cache_expires_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        LocalDateTime generatedAt = data.getGeneratedAt() == null ? LocalDateTime.now() : data.getGeneratedAt();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, data.getReportType());
            if (filter.getYear() == null) statement.setNull(2, Types.INTEGER);
            else statement.setInt(2, filter.getYear());
            if (filter.getDateFrom() == null) statement.setNull(3, Types.DATE);
            else statement.setDate(3, Date.valueOf(filter.getDateFrom()));
            if (filter.getDateTo() == null) statement.setNull(4, Types.DATE);
            else statement.setDate(4, Date.valueOf(filter.getDateTo()));
            if (filter.getReviewPeriod() == null) statement.setNull(5, Types.VARCHAR);
            else statement.setString(5, filter.getReviewPeriod());
            if (filter.getDepartmentId() == null) statement.setNull(6, Types.BIGINT);
            else statement.setLong(6, filter.getDepartmentId());
            statement.setString(7, serialize(data));
            statement.setTimestamp(8, Timestamp.valueOf(generatedAt));
            statement.setTimestamp(9, Timestamp.valueOf(generatedAt.plusMinutes(10)));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Cannot read generated HR_REPORT ID");
                long reportId = keys.getLong(1);
                data.setReportId(reportId);
                return reportId;
            }
        }
    }

    public void recordAudit(long actorId, String actionCode, Long reportId, String reportType,
                            HrmReportFilter filter, String resultStatus) throws SQLException {
        String sql = """
                INSERT INTO AUDIT_LOG(
                    actor_id, action_code, report_id, report_type, filter_snapshot, result_status, occurred_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, actorId);
            statement.setString(2, actionCode);
            if (reportId == null) statement.setNull(3, Types.BIGINT);
            else statement.setLong(3, reportId);
            statement.setString(4, reportType);
            statement.setString(5, filter.snapshot());
            statement.setString(6, resultStatus);
            statement.executeUpdate();
        }
    }

    private HrmReportData overview(Connection connection, HrmReportFilter filter) throws SQLException {
        HrmReportData data = base("OVERVIEW", "Overview Report", connection, filter);
        List<Object> params = new ArrayList<>();
        String deptCondition = departmentCondition("e", filter, params);
        try (PreparedStatement statement = prepare(connection, """
                SELECT COUNT(*) AS employees,
                       COALESCE(AVG(e.training_progress), 0) AS avg_training,
                       COALESCE(SUM(e.base_salary), 0) AS salary_pool
                FROM employees e
                WHERE e.status = 'ACTIVE'
                """ + deptCondition, params);
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            data.getSummaryMetrics().put("Active employees", String.valueOf(rs.getInt("employees")));
            data.getSummaryMetrics().put("Avg training", Math.round(rs.getDouble("avg_training")) + "%");
            data.getSummaryMetrics().put("Salary pool", money(rs.getBigDecimal("salary_pool")));
        }

        data.getSummaryMetrics().put("Pending leaves", String.valueOf(count(connection,
                "SELECT COUNT(*) FROM leave_requests lr JOIN employees e ON e.employee_id = lr.employee_id "
                        + "WHERE lr.status IN ('PENDING_HRM','PENDING_HR_MANAGER','PENDING_DEPARTMENT_MANAGER')"
                        + departmentCondition("e", filter, new ArrayList<>()), filter.getDepartmentId())));
        data.getSummaryMetrics().put("Payroll batches", String.valueOf(count(connection,
                "SELECT COUNT(DISTINCT b.batch_id) FROM payroll_batches b "
                        + "LEFT JOIN payslips p ON p.batch_id = b.batch_id "
                        + "LEFT JOIN employees e ON e.employee_id = p.employee_id WHERE b.status = 'SUBMITTED'"
                        + departmentCondition("e", filter, new ArrayList<>()), filter.getDepartmentId())));

        params = new ArrayList<>();
        String sql = """
                SELECT d.department_name, COUNT(e.employee_id) AS employees,
                       COALESCE(SUM(e.base_salary), 0) AS salary_pool,
                       COALESCE(AVG(e.training_progress), 0) AS avg_training
                FROM departments d
                LEFT JOIN employees e ON e.department_id = d.department_id AND e.status = 'ACTIVE'
                WHERE d.status = 'ACTIVE'
                """;
        if (filter.getDepartmentId() != null) {
            sql += " AND d.department_id = ?";
            params.add(filter.getDepartmentId());
        }
        sql += " GROUP BY d.department_name ORDER BY d.department_name";
        try (PreparedStatement statement = prepare(connection, sql, params);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String dept = rs.getString("department_name");
                long employees = rs.getLong("employees");
                data.getChartData().put(dept, employees);
                data.getTableRows().add(row("Department", dept,
                        "Employees", String.valueOf(employees),
                        "Salary pool", money(rs.getBigDecimal("salary_pool")),
                        "Avg training", Math.round(rs.getDouble("avg_training")) + "%"));
            }
        }
        data.setEmpty(data.getChartData().isEmpty());
        return data;
    }

    private HrmReportData attendance(Connection connection, HrmReportFilter filter) throws SQLException {
        HrmReportData data = base("ATTENDANCE", "Attendance Report", connection, filter);
        List<Object> params = new ArrayList<>();
        String where = " WHERE 1=1";
        where += rangeCondition("a.attendance_date", filter, params);
        where += departmentCondition("e", filter, params);

        try (PreparedStatement statement = prepare(connection, """
                SELECT a.status, COUNT(*) AS total
                FROM attendance a
                JOIN users u ON u.user_id = a.user_id
                LEFT JOIN employees e ON e.user_id = u.user_id
                """ + where + " GROUP BY a.status ORDER BY a.status", params);
             ResultSet rs = statement.executeQuery()) {
            long total = 0;
            while (rs.next()) {
                long value = rs.getLong("total");
                total += value;
                data.getChartData().put(rs.getString("status"), value);
            }
            data.getSummaryMetrics().put("Attendance records", String.valueOf(total));
            data.setEmpty(total == 0);
        }

        try (PreparedStatement statement = prepare(connection, """
                SELECT a.attendance_date, a.status, COUNT(*) AS total
                FROM attendance a
                JOIN users u ON u.user_id = a.user_id
                LEFT JOIN employees e ON e.user_id = u.user_id
                """ + where + " GROUP BY a.attendance_date, a.status ORDER BY a.attendance_date DESC, a.status", params);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                data.getTableRows().add(row("Date", String.valueOf(rs.getDate("attendance_date")),
                        "Status", rs.getString("status"),
                        "Records", String.valueOf(rs.getLong("total"))));
            }
        }
        return data;
    }

    private HrmReportData leave(Connection connection, HrmReportFilter filter) throws SQLException {
        HrmReportData data = base("LEAVE", "Leave Report", connection, filter);
        List<Object> params = new ArrayList<>();
        String where = " WHERE 1=1";
        if (filter.getDateFrom() != null) {
            where += " AND lr.end_date >= ?";
            params.add(filter.getDateFrom());
        }
        if (filter.getDateTo() != null) {
            where += " AND lr.start_date <= ?";
            params.add(filter.getDateTo());
        }
        where += departmentCondition("e", filter, params);

        try (PreparedStatement statement = prepare(connection, """
                SELECT lr.status, COUNT(*) AS total
                FROM leave_requests lr
                JOIN employees e ON e.employee_id = lr.employee_id
                """ + where + " GROUP BY lr.status ORDER BY lr.status", params);
             ResultSet rs = statement.executeQuery()) {
            long total = 0;
            while (rs.next()) {
                long value = rs.getLong("total");
                total += value;
                data.getChartData().put(rs.getString("status"), value);
            }
            data.getSummaryMetrics().put("Leave requests", String.valueOf(total));
            data.setEmpty(total == 0);
        }

        try (PreparedStatement statement = prepare(connection, """
                SELECT lr.leave_id, u.full_name, d.department_name, lr.leave_type,
                       lr.start_date, lr.end_date, lr.days_count, lr.status
                FROM leave_requests lr
                JOIN employees e ON e.employee_id = lr.employee_id
                JOIN users u ON u.user_id = e.user_id
                LEFT JOIN departments d ON d.department_id = e.department_id
                """ + where + " ORDER BY lr.created_at DESC LIMIT 200", params);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                data.getTableRows().add(row("Employee", rs.getString("full_name"),
                        "Department", rs.getString("department_name"),
                        "Type", rs.getString("leave_type"),
                        "Period", rs.getDate("start_date") + " -> " + rs.getDate("end_date"),
                        "Days", String.valueOf(rs.getInt("days_count")),
                        "Status", rs.getString("status")));
            }
        }
        return data;
    }

    private HrmReportData payroll(Connection connection, HrmReportFilter filter) throws SQLException {
        HrmReportData data = base("PAYROLL", "Payroll Report", connection, filter);
        List<Object> params = new ArrayList<>();
        String where = " WHERE 1=1";
        if (filter.getDateFrom() != null) {
            where += " AND b.period_end >= ?";
            params.add(filter.getDateFrom());
        }
        if (filter.getDateTo() != null) {
            where += " AND b.period_start <= ?";
            params.add(filter.getDateTo());
        }
        where += departmentCondition("e", filter, params);

        try (PreparedStatement statement = prepare(connection, """
                SELECT COALESCE(d.department_name, 'Unassigned') AS dept_name,
                       COALESCE(SUM(p.net_pay), 0) AS total_net,
                       COUNT(p.payslip_id) AS payslips
                FROM payroll_batches b
                LEFT JOIN payslips p ON p.batch_id = b.batch_id
                LEFT JOIN employees e ON e.employee_id = p.employee_id
                LEFT JOIN departments d ON d.department_id = e.department_id
                """ + where + " GROUP BY COALESCE(d.department_name, 'Unassigned') ORDER BY total_net DESC", params);
             ResultSet rs = statement.executeQuery()) {
            BigDecimal totalNet = BigDecimal.ZERO;
            long payslips = 0;
            while (rs.next()) {
                BigDecimal amount = rs.getBigDecimal("total_net");
                totalNet = totalNet.add(amount == null ? BigDecimal.ZERO : amount);
                payslips += rs.getLong("payslips");
                data.getChartData().put(rs.getString("dept_name"), amount == null ? 0 : amount.longValue());
            }
            data.getSummaryMetrics().put("Payslips", String.valueOf(payslips));
            data.getSummaryMetrics().put("Total net", money(totalNet));
            data.setEmpty(payslips == 0);
        }

        try (PreparedStatement statement = prepare(connection, """
                SELECT b.period_label, u.full_name, d.department_name, p.net_pay, b.status
                FROM payroll_batches b
                JOIN payslips p ON p.batch_id = b.batch_id
                JOIN employees e ON e.employee_id = p.employee_id
                JOIN users u ON u.user_id = e.user_id
                LEFT JOIN departments d ON d.department_id = e.department_id
                """ + where + " ORDER BY b.period_start DESC, u.full_name LIMIT 200", params);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                data.getTableRows().add(row("Period", rs.getString("period_label"),
                        "Employee", rs.getString("full_name"),
                        "Department", rs.getString("department_name"),
                        "Net pay", money(rs.getBigDecimal("net_pay")),
                        "Status", rs.getString("status")));
            }
        }
        return data;
    }

    private HrmReportData performance(Connection connection, HrmReportFilter filter) throws SQLException {
        HrmReportData data = base("PERFORMANCE", "Performance Report", connection, filter);
        List<Object> params = new ArrayList<>();
        String where = " WHERE e.status = 'ACTIVE'";
        where += departmentCondition("e", filter, params);

        try (PreparedStatement statement = prepare(connection, """
                SELECT CASE
                         WHEN e.training_progress < 40 THEN '0-39%'
                         WHEN e.training_progress < 70 THEN '40-69%'
                         ELSE '70-100%'
                       END AS bucket,
                       COUNT(*) AS total
                FROM employees e
                """ + where + """
                GROUP BY CASE
                         WHEN e.training_progress < 40 THEN '0-39%'
                         WHEN e.training_progress < 70 THEN '40-69%'
                         ELSE '70-100%'
                       END
                ORDER BY bucket
                """, params);
             ResultSet rs = statement.executeQuery()) {
            long total = 0;
            while (rs.next()) {
                long value = rs.getLong("total");
                total += value;
                data.getChartData().put(rs.getString("bucket"), value);
            }
            data.getSummaryMetrics().put("Reviewed employees", String.valueOf(total));
            data.setEmpty(total == 0);
        }

        try (PreparedStatement statement = prepare(connection, """
                SELECT u.full_name, d.department_name, e.training_progress, e.base_salary
                FROM employees e
                JOIN users u ON u.user_id = e.user_id
                LEFT JOIN departments d ON d.department_id = e.department_id
                """ + where + " ORDER BY e.training_progress DESC, u.full_name LIMIT 200", params);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                data.getTableRows().add(row("Employee", rs.getString("full_name"),
                        "Department", rs.getString("department_name"),
                        "Training", rs.getInt("training_progress") + "%",
                        "Base salary", money(rs.getBigDecimal("base_salary"))));
            }
        }
        return data;
    }

    private HrmReportData base(String type, String title, Connection connection, HrmReportFilter filter)
            throws SQLException {
        HrmReportData data = new HrmReportData();
        data.setReportType(type);
        data.setReportTitle(title);
        if (filter.getDepartmentId() != null) {
            data.setDepartmentLabel(departmentName(connection, filter.getDepartmentId()));
        }
        return data;
    }

    private String departmentName(Connection connection, long departmentId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT department_name FROM departments WHERE department_id = ?")) {
            statement.setLong(1, departmentId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getString(1) : "Department #" + departmentId;
            }
        }
    }

    private long count(Connection connection, String sql, Long departmentId) throws SQLException {
        List<Object> params = new ArrayList<>();
        if (departmentId != null) params.add(departmentId);
        try (PreparedStatement statement = prepare(connection, sql, params);
             ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    private String departmentCondition(String employeeAlias, HrmReportFilter filter, List<Object> params) {
        if (filter.getDepartmentId() == null) return "";
        params.add(filter.getDepartmentId());
        return " AND " + employeeAlias + ".department_id = ?";
    }

    private String rangeCondition(String column, HrmReportFilter filter, List<Object> params) {
        String condition = "";
        if (filter.getDateFrom() != null) {
            condition += " AND " + column + " >= ?";
            params.add(filter.getDateFrom());
        }
        if (filter.getDateTo() != null) {
            condition += " AND " + column + " <= ?";
            params.add(filter.getDateTo());
        }
        return condition;
    }

    private PreparedStatement prepare(Connection connection, String sql, List<Object> params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof LocalDate date) statement.setDate(i + 1, Date.valueOf(date));
            else if (value instanceof Long number) statement.setLong(i + 1, number);
            else if (value instanceof Integer number) statement.setInt(i + 1, number);
            else statement.setString(i + 1, String.valueOf(value));
        }
        return statement;
    }

    private Map<String, String> row(String... values) {
        Map<String, String> row = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            row.put(values[i], values[i + 1] == null ? "" : values[i + 1]);
        }
        return row;
    }

    private String money(BigDecimal value) {
        if (value == null) return "0";
        return String.format("%,.0f", value);
    }

    private int bindNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index++, Types.INTEGER);
            statement.setNull(index++, Types.INTEGER);
        } else {
            statement.setInt(index++, value);
            statement.setInt(index++, value);
        }
        return index;
    }

    private int bindNullableDate(PreparedStatement statement, int index, LocalDate value) throws SQLException {
        if (value == null) {
            statement.setNull(index++, Types.DATE);
            statement.setNull(index++, Types.DATE);
        } else {
            statement.setDate(index++, Date.valueOf(value));
            statement.setDate(index++, Date.valueOf(value));
        }
        return index;
    }

    private int bindNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index++, Types.VARCHAR);
            statement.setNull(index++, Types.VARCHAR);
        } else {
            statement.setString(index++, value);
            statement.setString(index++, value);
        }
        return index;
    }

    private int bindNullableLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) {
            statement.setNull(index++, Types.BIGINT);
            statement.setNull(index++, Types.BIGINT);
        } else {
            statement.setLong(index++, value);
            statement.setLong(index++, value);
        }
        return index;
    }

    private String serialize(HrmReportData data) {
        StringBuilder payload = new StringBuilder();
        payload.append("title=").append(encode(data.getReportTitle())).append('\n');
        payload.append("department=").append(encode(data.getDepartmentLabel())).append('\n');
        payload.append("empty=").append(data.isEmpty()).append('\n');
        for (Map.Entry<String, String> entry : data.getSummaryMetrics().entrySet()) {
            payload.append("summary=").append(encode(entry.getKey())).append(':')
                    .append(encode(entry.getValue())).append('\n');
        }
        for (Map.Entry<String, Long> entry : data.getChartData().entrySet()) {
            payload.append("chart=").append(encode(entry.getKey())).append(':')
                    .append(entry.getValue()).append('\n');
        }
        for (Map<String, String> row : data.getTableRows()) {
            payload.append("row=");
            boolean first = true;
            for (Map.Entry<String, String> cell : row.entrySet()) {
                if (!first) payload.append(',');
                payload.append(encode(cell.getKey())).append(':').append(encode(cell.getValue()));
                first = false;
            }
            payload.append('\n');
        }
        return payload.toString();
    }

    private HrmReportData deserialize(String payload) {
        HrmReportData data = new HrmReportData();
        if (payload == null) return data;
        for (String line : payload.split("\\R")) {
            if (line.startsWith("title=")) {
                data.setReportTitle(decode(line.substring("title=".length())));
            } else if (line.startsWith("department=")) {
                data.setDepartmentLabel(decode(line.substring("department=".length())));
            } else if (line.startsWith("empty=")) {
                data.setEmpty(Boolean.parseBoolean(line.substring("empty=".length())));
            } else if (line.startsWith("summary=")) {
                String[] parts = line.substring("summary=".length()).split(":", 2);
                if (parts.length == 2) data.getSummaryMetrics().put(decode(parts[0]), decode(parts[1]));
            } else if (line.startsWith("chart=")) {
                String[] parts = line.substring("chart=".length()).split(":", 2);
                if (parts.length == 2) data.getChartData().put(decode(parts[0]), Long.parseLong(parts[1]));
            } else if (line.startsWith("row=")) {
                Map<String, String> row = new LinkedHashMap<>();
                String body = line.substring("row=".length());
                if (!body.isBlank()) {
                    for (String cell : body.split(",")) {
                        String[] parts = cell.split(":", 2);
                        if (parts.length == 2) row.put(decode(parts[0]), decode(parts[1]));
                    }
                }
                data.getTableRows().add(row);
            }
        }
        return data;
    }

    private String encode(String value) {
        String safe = value == null ? "" : value;
        return Base64.getUrlEncoder().encodeToString(safe.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        if (value == null || value.isBlank()) return "";
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
