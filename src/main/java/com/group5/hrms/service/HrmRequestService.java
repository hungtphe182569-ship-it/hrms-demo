package com.group5.hrms.service;

import com.group5.hrms.dao.ActivityLogDao;
import com.group5.hrms.dao.EmployeeRequestDao;
import com.group5.hrms.dao.NotificationDao;
import com.group5.hrms.model.EmployeeRequest;

import java.sql.SQLException;
import java.util.List;

public class HrmRequestService {
    private final EmployeeRequestDao requestDao = new EmployeeRequestDao();
    private final NotificationDao notificationDao = new NotificationDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public List<EmployeeRequest> findPending() throws SQLException {
        return requestDao.findPending();
    }

    public List<EmployeeRequest> findProcessed() throws SQLException {
        return requestDao.findProcessed();
    }

    public String approve(long requestId, long hrmId) throws SQLException {
        EmployeeRequest request = require(requestId);
        String status = requestDao.approve(requestId, hrmId);
        if ("PENDING_ADMIN".equals(status)) {
            notificationDao.notifyUser(request.getRequesterUserId(), "Request forwarded to Admin",
                    "Request #" + requestId + " has been forwarded for Admin approval.");
            notificationDao.notifyAdmins("Request waiting for Admin",
                    "Promotion request #" + requestId + " is waiting for Admin approval.");
            activityLogDao.log(hrmId, "FORWARD_REQUEST_ADMIN", "REQUEST", requestId,
                    request.getCategory() + " forwarded to Admin");
        } else {
            notificationDao.notifyUser(request.getRequesterUserId(), "Request approved",
                    "Request #" + requestId + " has been approved.");
            activityLogDao.log(hrmId, "APPROVE_REQUEST", "REQUEST", requestId,
                    "Approved " + request.getCategory());
        }
        return status;
    }

    public void reject(long requestId, long hrmId, String reason) throws SQLException {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        EmployeeRequest request = require(requestId);
        requestDao.reject(requestId, hrmId, reason);
        notificationDao.notifyUser(request.getRequesterUserId(), "Request rejected",
                "Request #" + requestId + " was rejected: " + reason.trim());
        activityLogDao.log(hrmId, "REJECT_REQUEST", "REQUEST", requestId,
                "Rejected " + request.getCategory());
    }

    private EmployeeRequest require(long requestId) throws SQLException {
        return requestDao.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
    }
}
