package com.group5.hrms.web;

import com.group5.hrms.model.Account;
import com.group5.hrms.service.AuditService;
import com.group5.hrms.service.ReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet({"", "/home"})
public class HomeServlet extends HttpServlet {
    private final ReportService reportService = new ReportService();
    private final AuditService auditService = new AuditService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (request.getServletPath().isEmpty() || "/".equals(request.getServletPath())) {
            request.getRequestDispatcher("/WEB-INF/views/landing.jsp").forward(request, response);
            return;
        }

        Account account = (Account) request.getSession().getAttribute("currentUser");
        request.setAttribute("activePage", "home");
        try {
            if (account.hasRole("Admin")) {
                request.setAttribute("stats", reportService.loadDashboardStats());
                request.setAttribute("recentActivities", auditService.recentAdminActivities(6));
                request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
            } else if (account.hasRole("Manager")) {
                request.setAttribute("dashboardRole", "Manager");
                request.getRequestDispatcher("/WEB-INF/views/role-home.jsp").forward(request, response);
            } else if (account.hasRole("Employee")) {
                request.setAttribute("dashboardRole", "Employee");
                request.getRequestDispatcher("/WEB-INF/views/role-home.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tài khoản chưa được gán role hợp lệ");
            }
        } catch (Exception e) {
            throw new ServletException("Không thể tải trang chủ theo role", e);
        }
    }
}
