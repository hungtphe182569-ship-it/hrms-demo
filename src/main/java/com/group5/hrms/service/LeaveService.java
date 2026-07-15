package com.group5.hrms.service;

import com.group5.hrms.dao.ActivityLogDao;
import com.group5.hrms.dao.LeaveRequestDao;
import com.group5.hrms.dao.NotificationDao;
import com.group5.hrms.model.LeaveRequest;

import java.sql.SQLException;
import java.util.List;

public class LeaveService {
    private final LeaveRequestDao leaveRequestDao = new LeaveRequestDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final NotificationDao notificationDao = new NotificationDao();

    public List<LeaveRequest> findPending(String leaveType) throws SQLException {
        return leaveRequestDao.findByStatus("PENDING_HRM", leaveType);
    }

    public List<LeaveRequest> findProcessed(String leaveType) throws SQLException {
        List<LeaveRequest> approved = leaveRequestDao.findByStatus("APPROVED", leaveType);
        List<LeaveRequest> rejected = leaveRequestDao.findByStatus("REJECTED", leaveType);
        approved.addAll(rejected);
        approved.sort((a, b) -> {
            if (a.getDecidedAt() == null || b.getDecidedAt() == null) return 0;
            return b.getDecidedAt().compareTo(a.getDecidedAt());
        });
        return approved;
    }

    public void approve(long leaveId, long hrmId) throws SQLException {
        leaveRequestDao.decide(leaveId, "APPROVED", null, hrmId);
        long employeeUserId = leaveRequestDao.findUserIdByLeave(leaveId);
        notificationDao.notifyUser(employeeUserId, "Leave approved",
                "Đơn nghỉ #" + leaveId + " đã được HR Manager phê duyệt.");
        activityLogDao.log(hrmId, "APPROVE_LEAVE", "LEAVE", leaveId, "Approved leave #" + leaveId);
    }

    public void reject(long leaveId, long hrmId, String reason) throws SQLException {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        leaveRequestDao.decide(leaveId, "REJECTED", reason, hrmId);
        long employeeUserId = leaveRequestDao.findUserIdByLeave(leaveId);
        notificationDao.notifyUser(employeeUserId, "Leave rejected",
                "Đơn nghỉ #" + leaveId + " bị từ chối: " + reason.trim());
        activityLogDao.log(hrmId, "REJECT_LEAVE", "LEAVE", leaveId, "Rejected leave #" + leaveId);
    }

    public void revert(long leaveId, long hrmId) throws SQLException {
        LeaveRequest leave = leaveRequestDao.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        if (!leave.canRevert()) {
            throw new IllegalArgumentException("Decisions cannot be reverted after 24 hours");
        }
        leaveRequestDao.revert(leaveId);
        activityLogDao.log(hrmId, "REVERT_LEAVE", "LEAVE", leaveId, "Reverted leave #" + leaveId);
    }

    public void approveMany(long[] leaveIds, long hrmId) throws SQLException {
        for (long leaveId : leaveIds) approve(leaveId, hrmId);
    }
}
