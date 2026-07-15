package com.group5.hrms.dao;

import com.group5.hrms.config.Database;
import com.group5.hrms.model.HrmDashboardStats;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

public class HrmStatsDao {
    public int countActiveEmployees(Long departmentId) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM dbo.employees e
                WHERE e.status = 'ACTIVE'
                  AND (? IS NULL OR e.department_id = ?)
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindDept(statement, departmentId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Map<String, Integer> employeesByDepartment(Long departmentId) throws SQLException {
        String sql = """
                SELECT COALESCE(d.department_name, N'Unassigned') AS dept_name, COUNT(*) AS cnt
                FROM dbo.employees e
                LEFT JOIN dbo.departments d ON d.department_id = e.department_id
                WHERE e.status = 'ACTIVE'
                  AND (? IS NULL OR e.department_id = ?)
                GROUP BY COALESCE(d.department_name, N'Unassigned')
                ORDER BY cnt DESC
                """;
        return intMap(sql, departmentId);
    }

    public Map<String, Integer> genderBreakdown(Long departmentId) throws SQLException {
        String sql = """
                SELECT COALESCE(e.gender, 'OTHER') AS gender_name, COUNT(*) AS cnt
                FROM dbo.employees e
                WHERE e.status = 'ACTIVE'
                  AND (? IS NULL OR e.department_id = ?)
                GROUP BY COALESCE(e.gender, 'OTHER')
                ORDER BY cnt DESC
                """;
        return intMap(sql, departmentId);
    }

    public Map<String, BigDecimal> salaryByDepartment(Long departmentId) throws SQLException {
        String sql = """
                SELECT COALESCE(d.department_name, N'Unassigned') AS dept_name, SUM(e.base_salary) AS total_salary
                FROM dbo.employees e
                LEFT JOIN dbo.departments d ON d.department_id = e.department_id
                WHERE e.status = 'ACTIVE'
                  AND (? IS NULL OR e.department_id = ?)
                GROUP BY COALESCE(d.department_name, N'Unassigned')
                ORDER BY total_salary DESC
                """;
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindDept(statement, departmentId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) map.put(rs.getString(1), rs.getBigDecimal(2));
            }
        }
        return map;
    }

    public Map<String, Integer> trainingBuckets(Long departmentId) throws SQLException {
        String sql = """
                SELECT CASE
                         WHEN e.training_progress < 40 THEN '0-39%'
                         WHEN e.training_progress < 70 THEN '40-69%'
                         ELSE '70-100%'
                       END AS bucket,
                       COUNT(*) AS cnt
                FROM dbo.employees e
                WHERE e.status = 'ACTIVE'
                  AND (? IS NULL OR e.department_id = ?)
                GROUP BY CASE
                         WHEN e.training_progress < 40 THEN '0-39%'
                         WHEN e.training_progress < 70 THEN '40-69%'
                         ELSE '70-100%'
                       END
                ORDER BY bucket
                """;
        return intMap(sql, departmentId);
    }

    public HrmDashboardStats overview() throws SQLException {
        HrmDashboardStats stats = new HrmDashboardStats();
        stats.setTotalEmployees(countActiveEmployees(null));
        stats.setEmployeesByDepartment(employeesByDepartment(null));
        stats.setGenderBreakdown(genderBreakdown(null));
        stats.setSalaryByDepartment(salaryByDepartment(null));
        stats.setTrainingBuckets(trainingBuckets(null));
        return stats;
    }

    private Map<String, Integer> intMap(String sql, Long departmentId) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindDept(statement, departmentId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) map.put(rs.getString(1), rs.getInt(2));
            }
        }
        return map;
    }

    private void bindDept(PreparedStatement statement, Long departmentId) throws SQLException {
        if (departmentId == null) {
            statement.setNull(1, Types.BIGINT);
            statement.setNull(2, Types.BIGINT);
        } else {
            statement.setLong(1, departmentId);
            statement.setLong(2, departmentId);
        }
    }
}
