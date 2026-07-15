package com.group5.hrms.web;

import com.group5.hrms.model.Account;
import com.group5.hrms.service.LeaveService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;

@WebServlet("/hrm/leaves")
public class HrmLeaveServlet extends HttpServlet {
    private final LeaveService leaveService = new LeaveService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String leaveType = blankToNull(request.getParameter("leaveType"));
            String view = request.getParameter("view");
            boolean processed = "processed".equals(view);
            request.setAttribute("leaveType", leaveType);
            request.setAttribute("view", processed ? "processed" : "pending");
            request.setAttribute("leaves", processed
                    ? leaveService.findProcessed(leaveType)
                    : leaveService.findPending(leaveType));
            request.setAttribute("activePage", "hrm-leaves");
            request.setAttribute("pageTitle", "Pending Leave (HRM)");
            request.getRequestDispatcher("/WEB-INF/views/hrm/leaves.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load leave requests", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Account hrm = (Account) request.getSession().getAttribute("currentUser");
        String action = request.getParameter("action");
        String redirect = request.getContextPath() + "/hrm/leaves";
        try {
            if ("approve".equals(action)) {
                leaveService.approve(id(request), hrm.getId());
                Flash.success(request, "Đã phê duyệt đơn nghỉ phép");
            } else if ("approveBulk".equals(action)) {
                leaveService.approveMany(ids(request), hrm.getId());
                Flash.success(request, "Đã phê duyệt các đơn đã chọn");
            } else if ("reject".equals(action)) {
                leaveService.reject(id(request), hrm.getId(), request.getParameter("reason"));
                Flash.success(request, "Đã từ chối đơn nghỉ phép");
            } else if ("revert".equals(action)) {
                leaveService.revert(id(request), hrm.getId());
                Flash.success(request, "Đã hoàn tác quyết định trong vòng 24 giờ");
                redirect += "?view=processed";
            } else {
                throw new IllegalArgumentException("Action không hợp lệ");
            }
        } catch (Exception e) {
            Flash.error(request, e.getMessage() == null ? "Không thể xử lý đơn nghỉ" : e.getMessage());
            if ("revert".equals(action)) redirect += "?view=processed";
        }
        response.sendRedirect(redirect);
    }

    private long id(HttpServletRequest request) {
        return Long.parseLong(request.getParameter("leaveId"));
    }

    private long[] ids(HttpServletRequest request) {
        String[] values = request.getParameterValues("leaveIds");
        if (values == null || values.length == 0) throw new IllegalArgumentException("Chưa chọn đơn nào");
        return Arrays.stream(values).mapToLong(Long::parseLong).toArray();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
