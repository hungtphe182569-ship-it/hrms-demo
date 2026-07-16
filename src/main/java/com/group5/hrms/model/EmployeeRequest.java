package com.group5.hrms.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EmployeeRequest {
    private long id;
    private long employeeId;
    private long requesterUserId;
    private String requesterName;
    private String departmentName;
    private String category;
    private String title;
    private String description;
    private String status;
    private Long targetDepartmentId;
    private String targetDepartmentName;
    private BigDecimal proposedSalary;
    private String proposedPosition;
    private String rejectionReason;
    private Long decidedBy;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getEmployeeId() { return employeeId; }
    public void setEmployeeId(long employeeId) { this.employeeId = employeeId; }
    public long getRequesterUserId() { return requesterUserId; }
    public void setRequesterUserId(long requesterUserId) { this.requesterUserId = requesterUserId; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getTargetDepartmentId() { return targetDepartmentId; }
    public void setTargetDepartmentId(Long targetDepartmentId) { this.targetDepartmentId = targetDepartmentId; }
    public String getTargetDepartmentName() { return targetDepartmentName; }
    public void setTargetDepartmentName(String targetDepartmentName) { this.targetDepartmentName = targetDepartmentName; }
    public BigDecimal getProposedSalary() { return proposedSalary; }
    public void setProposedSalary(BigDecimal proposedSalary) { this.proposedSalary = proposedSalary; }
    public String getProposedPosition() { return proposedPosition; }
    public void setProposedPosition(String proposedPosition) { this.proposedPosition = proposedPosition; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Long getDecidedBy() { return decidedBy; }
    public void setDecidedBy(Long decidedBy) { this.decidedBy = decidedBy; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean requiresAdminApproval() {
        return "PROMOTION".equals(category);
    }
}
