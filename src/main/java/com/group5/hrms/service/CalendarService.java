package com.group5.hrms.service;

import com.group5.hrms.dao.ActivityLogDao;
import com.group5.hrms.dao.CompanyEventDao;
import com.group5.hrms.dao.NotificationDao;
import com.group5.hrms.model.CompanyEvent;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class CalendarService {
    private static final Set<String> TYPES = Set.of("HOLIDAY", "MEETING", "DEADLINE", "OTHER");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final CompanyEventDao companyEventDao = new CompanyEventDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final NotificationDao notificationDao = new NotificationDao();

    public List<CompanyEvent> findAll() throws SQLException {
        return companyEventDao.findAll();
    }

    public void create(String title, String type, String start, String end, String description, long actorId)
            throws SQLException {
        LocalDateTime startAt = parse(start);
        LocalDateTime endAt = parse(end);
        validate(title, type, startAt, endAt);
        long id = companyEventDao.create(title, type, startAt, endAt, description, actorId);
        notificationDao.notifyAllEmployees("Company calendar updated",
                "Sự kiện mới: " + title.trim());
        activityLogDao.log(actorId, "CREATE_EVENT", "CALENDAR", id, "Created event " + title);
    }

    public void update(long id, String title, String type, String start, String end, String description, long actorId)
            throws SQLException {
        LocalDateTime startAt = parse(start);
        LocalDateTime endAt = parse(end);
        validate(title, type, startAt, endAt);
        companyEventDao.update(id, title, type, startAt, endAt, description);
        notificationDao.notifyAllEmployees("Company calendar updated",
                "Sự kiện đã cập nhật: " + title.trim());
        activityLogDao.log(actorId, "UPDATE_EVENT", "CALENDAR", id, "Updated event " + title);
    }

    public void delete(long id, long actorId) throws SQLException {
        CompanyEvent event = companyEventDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        companyEventDao.delete(id);
        notificationDao.notifyAllEmployees("Company calendar updated",
                "Sự kiện đã xóa: " + event.getTitle());
        activityLogDao.log(actorId, "DELETE_EVENT", "CALENDAR", id, "Deleted event " + event.getTitle());
    }

    private void validate(String title, String type, LocalDateTime startAt, LocalDateTime endAt) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title is required");
        if (type == null || !TYPES.contains(type)) throw new IllegalArgumentException("Invalid event type");
        if (endAt.isBefore(startAt)) {
            throw new IllegalArgumentException("End Date/Time must be greater than or equal to Start Date/Time");
        }
    }

    private LocalDateTime parse(String value) {
        try {
            return LocalDateTime.parse(value, FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date/time format");
        }
    }
}
