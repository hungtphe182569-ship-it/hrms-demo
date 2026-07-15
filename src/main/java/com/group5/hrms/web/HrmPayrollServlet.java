package com.group5.hrms.web;

import com.group5.hrms.model.Account;
import com.group5.hrms.service.PayrollService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/hrm/payroll")
public class HrmPayrollServlet extends HttpServlet {
    private final PayrollService payrollService = new PayrollService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String exportId = request.getParameter("export");
            if (exportId != null) {
                long batchId = Long.parseLong(exportId);
                byte[] csv = payrollService.exportBankCsv(batchId);
                response.setContentType("text/csv; charset=UTF-8");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"payroll-batch-" + batchId + ".csv\"");
                response.getOutputStream().write(csv);
                return;
            }
            Long selectedId = parseLong(request.getParameter("batchId"));
            var batches = payrollService.findAll();
            if (selectedId == null && !batches.isEmpty()) selectedId = batches.get(0).getId();
            request.setAttribute("batches", batches);
            request.setAttribute("selectedBatchId", selectedId);
            if (selectedId != null) {
                request.setAttribute("selectedBatch", payrollService.require(selectedId));
                request.setAttribute("payslips", payrollService.findPayslips(selectedId));
            }
            request.setAttribute("activePage", "hrm-payroll");
            request.setAttribute("pageTitle", "Payroll for Approval");
            request.getRequestDispatcher("/WEB-INF/views/hrm/payroll.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load payroll", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Account hrm = (Account) request.getSession().getAttribute("currentUser");
        String action = request.getParameter("action");
        long batchId = Long.parseLong(request.getParameter("batchId"));
        try {
            if ("approve".equals(action)) {
                payrollService.finalApprove(batchId, hrm.getId());
                Flash.success(request, "Final approve payroll thành công");
            } else if ("reject".equals(action)) {
                payrollService.reject(batchId, hrm.getId(), request.getParameter("reason"));
                Flash.success(request, "Đã từ chối payroll batch");
            } else {
                throw new IllegalArgumentException("Action không hợp lệ");
            }
        } catch (Exception e) {
            Flash.error(request, e.getMessage() == null ? "Không thể xử lý payroll" : e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/hrm/payroll?batchId=" + batchId);
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        return Long.parseLong(value);
    }
}
