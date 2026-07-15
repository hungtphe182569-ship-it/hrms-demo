package com.group5.hrms.model;

import java.time.LocalDateTime;

public class Role {
    private long id;
    private String name;
    private String description;
    private int assignedUsers;
    private LocalDateTime createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getAssignedUsers() { return assignedUsers; }
    public void setAssignedUsers(int assignedUsers) { this.assignedUsers = assignedUsers; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
