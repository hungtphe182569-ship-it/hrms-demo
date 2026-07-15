package com.group5.hrms.service;

import com.group5.hrms.dao.ActivityLogDao;
import com.group5.hrms.dao.CompanyEventDao;
import com.group5.hrms.dao.HrmStatsDao;
import com.group5.hrms.dao.LeaveRequestDao;
import com.group5.hrms.dao.PayrollDao;
import com.group5.hrms.model.ActivityLog;
import com.group5.hrms.model.HrmDashboardStats;

import java.sql.SQLException;
import java.util.List;

public class HrmDashboardService {
    private final HrmStatsDao statsDao = new HrmStatsDao();
    private final LeaveRequestDao leaveRequestDao = new LeaveRequestDao();
    private final PayrollDao payrollDao = new PayrollDao();
    private final CompanyEventDao companyEventDao = new CompanyEventDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public HrmDashboardStats loadDashboard() throws SQLException {
        HrmDashboardStats stats = statsDao.overview();
        stats.setPendingLeaves(leaveRequestDao.countByStatus("PENDING_HRM"));
        stats.setPendingPayrollBatches(payrollDao.countSubmitted());
        stats.setPendingPayrollAmount(payrollDao.sumSubmittedAmount());
        stats.setUpcomingEvents(companyEventDao.upcoming(5));
        stats.setRecentActivities(activityLogDao.recent(8));
        return stats;
    }

    public HrmDashboardStats loadAnalytics(Long departmentId) throws SQLException {
        HrmDashboardStats stats = new HrmDashboardStats();
        stats.setTotalEmployees(statsDao.countActiveEmployees(departmentId));
        stats.setEmployeesByDepartment(statsDao.employeesByDepartment(departmentId));
        stats.setGenderBreakdown(statsDao.genderBreakdown(departmentId));
        stats.setSalaryByDepartment(statsDao.salaryByDepartment(departmentId));
        stats.setTrainingBuckets(statsDao.trainingBuckets(departmentId));
        return stats;
    }

    public List<ActivityLog> searchActivities(String keyword, String action, int page, int pageSize) throws SQLException {
        return activityLogDao.search(keyword, action, page, Math.min(Math.max(pageSize, 1), 50));
    }

    public int countActivities(String keyword, String action) throws SQLException {
        return activityLogDao.count(keyword, action);
    }
}
