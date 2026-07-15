package com.group5.hrms.model;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HrmDashboardStats {
    private int totalEmployees;
    private int pendingLeaves;
    private int pendingPayrollBatches;
    private BigDecimal pendingPayrollAmount = BigDecimal.ZERO;
    private Map<String, Integer> employeesByDepartment = new LinkedHashMap<>();
    private Map<String, Integer> genderBreakdown = new LinkedHashMap<>();
    private Map<String, BigDecimal> salaryByDepartment = new LinkedHashMap<>();
    private Map<String, Integer> trainingBuckets = new LinkedHashMap<>();
    private List<CompanyEvent> upcomingEvents;
    private List<ActivityLog> recentActivities;

    public int getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(int totalEmployees) { this.totalEmployees = totalEmployees; }
    public int getPendingLeaves() { return pendingLeaves; }
    public void setPendingLeaves(int pendingLeaves) { this.pendingLeaves = pendingLeaves; }
    public int getPendingPayrollBatches() { return pendingPayrollBatches; }
    public void setPendingPayrollBatches(int pendingPayrollBatches) { this.pendingPayrollBatches = pendingPayrollBatches; }
    public BigDecimal getPendingPayrollAmount() { return pendingPayrollAmount; }
    public void setPendingPayrollAmount(BigDecimal pendingPayrollAmount) { this.pendingPayrollAmount = pendingPayrollAmount; }
    public Map<String, Integer> getEmployeesByDepartment() { return employeesByDepartment; }
    public void setEmployeesByDepartment(Map<String, Integer> employeesByDepartment) { this.employeesByDepartment = employeesByDepartment; }
    public Map<String, Integer> getGenderBreakdown() { return genderBreakdown; }
    public void setGenderBreakdown(Map<String, Integer> genderBreakdown) { this.genderBreakdown = genderBreakdown; }
    public Map<String, BigDecimal> getSalaryByDepartment() { return salaryByDepartment; }
    public void setSalaryByDepartment(Map<String, BigDecimal> salaryByDepartment) { this.salaryByDepartment = salaryByDepartment; }
    public Map<String, Integer> getTrainingBuckets() { return trainingBuckets; }
    public void setTrainingBuckets(Map<String, Integer> trainingBuckets) { this.trainingBuckets = trainingBuckets; }
    public List<CompanyEvent> getUpcomingEvents() { return upcomingEvents; }
    public void setUpcomingEvents(List<CompanyEvent> upcomingEvents) { this.upcomingEvents = upcomingEvents; }
    public List<ActivityLog> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<ActivityLog> recentActivities) { this.recentActivities = recentActivities; }
}
