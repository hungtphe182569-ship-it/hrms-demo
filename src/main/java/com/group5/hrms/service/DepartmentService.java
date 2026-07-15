package com.group5.hrms.service;

import com.group5.hrms.dao.ActivityLogDao;
import com.group5.hrms.dao.DepartmentDao;
import com.group5.hrms.model.Department;

import java.sql.SQLException;
import java.util.List;

public class DepartmentService {
    private final DepartmentDao departmentDao = new DepartmentDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public List<Department> findAll(boolean includeInactive) throws SQLException {
        return departmentDao.findAll(includeInactive);
    }

    public void create(String code, String name, long actorId) throws SQLException {
        validate(code, name);
        if (departmentDao.existsCodeOrName(code, name, null)) {
            throw new IllegalArgumentException("Department code hoặc name đã tồn tại");
        }
        long id = departmentDao.create(code, name);
        activityLogDao.log(actorId, "CREATE_DEPARTMENT", "DEPARTMENT", id, "Created " + code);
    }

    public void update(long id, String code, String name, long actorId) throws SQLException {
        validate(code, name);
        if (departmentDao.existsCodeOrName(code, name, id)) {
            throw new IllegalArgumentException("Department code hoặc name đã tồn tại");
        }
        departmentDao.update(id, code, name);
        activityLogDao.log(actorId, "UPDATE_DEPARTMENT", "DEPARTMENT", id, "Updated " + code);
    }

    public void softDelete(long id, long actorId) throws SQLException {
        Department department = departmentDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng ban"));
        if (department.getEmployeeCount() > 0) {
            throw new IllegalArgumentException("Cannot delete department because active employees are still assigned to it");
        }
        departmentDao.softDelete(id);
        activityLogDao.log(actorId, "SOFT_DELETE_DEPARTMENT", "DEPARTMENT", id,
                "Soft-deleted " + department.getCode());
    }

    private void validate(String code, String name) {
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Department code và name là bắt buộc");
        }
    }
}
