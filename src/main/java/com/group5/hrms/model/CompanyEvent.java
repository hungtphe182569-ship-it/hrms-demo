package com.group5.hrms.model;

import java.time.LocalDateTime;

public class CompanyEvent {
    private long id;
    private String title;
    private String eventType;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String description;
    private long createdBy;
    private LocalDateTime createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getCreatedBy() { return createdBy; }
    public void setCreatedBy(long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
