package com.group5.hrms.model;

import java.time.LocalDateTime;

public class AuditEntry {
    private long id;
    private String accountName;
    private String action;
    private String reason;
    private String performedBy;
    private LocalDateTime performedAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }
}
