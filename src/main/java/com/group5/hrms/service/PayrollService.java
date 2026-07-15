package com.group5.hrms.service;

import com.group5.hrms.dao.ActivityLogDao;
import com.group5.hrms.dao.NotificationDao;
import com.group5.hrms.dao.PayrollDao;
import com.group5.hrms.model.PayrollBatch;
import com.group5.hrms.model.Payslip;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class PayrollService {
    private final PayrollDao payrollDao = new PayrollDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final NotificationDao notificationDao = new NotificationDao();

    public List<PayrollBatch> findAll() throws SQLException {
        return payrollDao.findAll();
    }

    public PayrollBatch require(long batchId) throws SQLException {
        return payrollDao.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Payroll batch not found"));
    }

    public List<Payslip> findPayslips(long batchId) throws SQLException {
        return payrollDao.findPayslips(batchId);
    }

    public void finalApprove(long batchId, long hrmId) throws SQLException {
        PayrollBatch batch = require(batchId);
        if (!"SUBMITTED".equals(batch.getStatus())) {
            throw new IllegalArgumentException("Only SUBMITTED payroll batches may be finally approved");
        }
        payrollDao.finalApprove(batchId, hrmId);
        for (Long userId : payrollDao.findEmployeeUserIds(batchId)) {
            notificationDao.notifyUser(userId, "Payslip published",
                    "Payslip kỳ " + batch.getPeriodLabel() + " đã được công bố.");
        }
        activityLogDao.log(hrmId, "APPROVE_PAYROLL", "PAYROLL", batchId,
                "Final approved batch " + batch.getPeriodLabel());
    }

    public void reject(long batchId, long hrmId, String reason) throws SQLException {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        PayrollBatch batch = require(batchId);
        if (!"SUBMITTED".equals(batch.getStatus())) {
            throw new IllegalArgumentException("Only SUBMITTED payroll batches may be rejected");
        }
        payrollDao.reject(batchId, hrmId, reason);
        activityLogDao.log(hrmId, "REJECT_PAYROLL", "PAYROLL", batchId,
                "Rejected batch " + batch.getPeriodLabel());
    }

    public byte[] exportBankCsv(long batchId) throws SQLException {
        PayrollBatch batch = require(batchId);
        if (!"APPROVED_FINAL".equals(batch.getStatus())) {
            throw new IllegalArgumentException("CSV export is allowed only when PayrollBatch status is APPROVED_FINAL");
        }
        StringBuilder csv = new StringBuilder("employee_name,department,bank_account,net_pay\n");
        for (Payslip payslip : payrollDao.findPayslips(batchId)) {
            csv.append(escape(payslip.getEmployeeName())).append(',')
                    .append(escape(payslip.getDepartmentName())).append(',')
                    .append(escape(payslip.getBankAccount())).append(',')
                    .append(payslip.getNetPay()).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"")) return "\"" + escaped + "\"";
        return escaped;
    }
}
