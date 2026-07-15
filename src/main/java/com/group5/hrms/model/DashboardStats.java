package com.group5.hrms.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardStats {
    private int totalAccounts;
    private int activeAccounts;
    private int lockedAccounts;
    private int deletedAccounts;
    private int totalRoles;
    private int totalPermissions;
    private int attendanceRecords;
    private Map<String, Integer> accountsByRole = new LinkedHashMap<>();
    private Map<String, Integer> accountsByStatus = new LinkedHashMap<>();
    private Map<String, Integer> permissionsByRole = new LinkedHashMap<>();
    private Map<String, Integer> attendanceByStatus = new LinkedHashMap<>();

    public int getTotalAccounts() { return totalAccounts; }
    public void setTotalAccounts(int totalAccounts) { this.totalAccounts = totalAccounts; }
    public int getActiveAccounts() { return activeAccounts; }
    public void setActiveAccounts(int activeAccounts) { this.activeAccounts = activeAccounts; }
    public int getLockedAccounts() { return lockedAccounts; }
    public void setLockedAccounts(int lockedAccounts) { this.lockedAccounts = lockedAccounts; }
    public int getDeletedAccounts() { return deletedAccounts; }
    public void setDeletedAccounts(int deletedAccounts) { this.deletedAccounts = deletedAccounts; }
    public int getTotalRoles() { return totalRoles; }
    public void setTotalRoles(int totalRoles) { this.totalRoles = totalRoles; }
    public int getTotalPermissions() { return totalPermissions; }
    public void setTotalPermissions(int totalPermissions) { this.totalPermissions = totalPermissions; }
    public int getAttendanceRecords() { return attendanceRecords; }
    public void setAttendanceRecords(int attendanceRecords) { this.attendanceRecords = attendanceRecords; }
    public Map<String, Integer> getAccountsByRole() { return accountsByRole; }
    public void setAccountsByRole(Map<String, Integer> accountsByRole) { this.accountsByRole = accountsByRole; }
    public Map<String, Integer> getAccountsByStatus() { return accountsByStatus; }
    public void setAccountsByStatus(Map<String, Integer> accountsByStatus) { this.accountsByStatus = accountsByStatus; }
    public Map<String, Integer> getPermissionsByRole() { return permissionsByRole; }
    public void setPermissionsByRole(Map<String, Integer> permissionsByRole) { this.permissionsByRole = permissionsByRole; }
    public Map<String, Integer> getAttendanceByStatus() { return attendanceByStatus; }
    public void setAttendanceByStatus(Map<String, Integer> attendanceByStatus) { this.attendanceByStatus = attendanceByStatus; }
}
