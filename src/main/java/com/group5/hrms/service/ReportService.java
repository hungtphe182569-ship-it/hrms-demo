package com.group5.hrms.service;

import com.group5.hrms.dao.ReportDao;
import com.group5.hrms.model.DashboardStats;

import java.sql.SQLException;

public class ReportService {
    private final ReportDao reportDao = new ReportDao();

    public DashboardStats loadDashboardStats() throws SQLException {
        return reportDao.loadDashboardStats();
    }
}
