package com.group5.hrms.service;

import com.group5.hrms.dao.AuditDao;
import com.group5.hrms.model.AuditEntry;

import java.sql.SQLException;
import java.util.List;

public class AuditService {
    private final AuditDao auditDao = new AuditDao();

    public List<AuditEntry> recentAdminActivities(int limit) throws SQLException {
        return auditDao.findRecent(limit);
    }
}
