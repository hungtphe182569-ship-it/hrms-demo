package com.group5.hrms.service;

import com.group5.hrms.dao.RoleDao;
import com.group5.hrms.model.Role;

import java.sql.SQLException;
import java.util.List;

public class RoleService {
    private final RoleDao roleDao = new RoleDao();

    public List<Role> findAll() throws SQLException {
        return roleDao.findAll();
    }

    public long create(String name, String description) throws SQLException {
        name = cleanName(name);
        if (roleDao.existsName(name, null)) throw new IllegalArgumentException("Role name already exists");
        return roleDao.create(name, cleanDescription(description));
    }

    public void update(long roleId, String name, String description) throws SQLException {
        name = cleanName(name);
        if (roleDao.existsName(name, roleId)) throw new IllegalArgumentException("Role name already exists");
        roleDao.update(roleId, name, cleanDescription(description));
    }

    public void delete(long roleId) throws SQLException {
        roleDao.delete(roleId);
    }

    private String cleanName(String value) {
        String name = value == null ? "" : value.trim();
        if (name.length() < 2 || name.length() > 50) {
            throw new IllegalArgumentException("Role name phải dài 2–50 ký tự");
        }
        return name;
    }

    private String cleanDescription(String value) {
        String description = value == null ? "" : value.trim();
        if (description.length() > 255) throw new IllegalArgumentException("Description tối đa 255 ký tự");
        return description;
    }
}
