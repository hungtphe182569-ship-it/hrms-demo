package com.group5.hrms.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardStats {
    private int totalAccounts;
    private int activeAccounts;
    private int lockedAccounts;
    private int deletedAccounts;
    private int totalRoles;
    private Map<String, Integer> accountsByRole = new LinkedHashMap<>();
    private Map<String, Integer> accountsByStatus = new LinkedHashMap<>();

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
    public Map<String, Integer> getAccountsByRole() { return accountsByRole; }
    public void setAccountsByRole(Map<String, Integer> accountsByRole) { this.accountsByRole = accountsByRole; }
    public Map<String, Integer> getAccountsByStatus() { return accountsByStatus; }
    public void setAccountsByStatus(Map<String, Integer> accountsByStatus) { this.accountsByStatus = accountsByStatus; }
}
