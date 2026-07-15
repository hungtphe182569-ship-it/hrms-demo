package com.group5.hrms.model;

import java.math.BigDecimal;

public class Payslip {
    private long id;
    private long batchId;
    private long employeeId;
    private String employeeName;
    private String departmentName;
    private BigDecimal grossPay;
    private BigDecimal netPay;
    private String bankAccount;
    private boolean published;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getBatchId() { return batchId; }
    public void setBatchId(long batchId) { this.batchId = batchId; }
    public long getEmployeeId() { return employeeId; }
    public void setEmployeeId(long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public BigDecimal getGrossPay() { return grossPay; }
    public void setGrossPay(BigDecimal grossPay) { this.grossPay = grossPay; }
    public BigDecimal getNetPay() { return netPay; }
    public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
}
