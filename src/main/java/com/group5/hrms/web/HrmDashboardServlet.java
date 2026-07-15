package com.group5.hrms.web;

import com.group5.hrms.service.DepartmentService;
import com.group5.hrms.service.HrmDashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet({"/hrm", "/hrm/dashboard", "/hrm/analytics", "/hrm/activities"})
public class HrmDashboardServlet extends HttpServlet {
    private final HrmDashboardService dashboardService = new HrmDashboardService();
    private final DepartmentService departmentService = new DepartmentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            if ("/hrm/analytics".equals(path)) {
                Long departmentId = parseLong(request.getParameter("departmentId"));
                request.setAttribute("stats", dashboardService.loadAnalytics(departmentId));
                request.setAttribute("departments", departmentService.findAll(false));
                request.setAttribute("selectedDepartmentId", departmentId);
                request.setAttribute("activePage", "hrm-analytics");
                request.setAttribute("pageTitle", "HR Analytics");
                request.getRequestDispatcher("/WEB-INF/views/hrm/analytics.jsp").forward(request, response);
                return;
            }
            if ("/hrm/activities".equals(path)) {
                String keyword = request.getParameter("q");
                String action = request.getParameter("actionName");
                int page = Math.max(parseInt(request.getParameter("page"), 1), 1);
                int pageSize = 20;
                int total = dashboardService.countActivities(keyword, action);
                int totalPages = Math.max((int) Math.ceil(total / (double) pageSize), 1);
                request.setAttribute("activities", dashboardService.searchActivities(keyword, action, page, pageSize));
                request.setAttribute("keyword", keyword);
                request.setAttribute("actionName", action);
                request.setAttribute("page", page);
                request.setAttribute("totalPages", totalPages);
                request.setAttribute("total", total);
                request.setAttribute("activePage", "hrm-activities");
                request.setAttribute("pageTitle", "Activity Center");
                request.getRequestDispatcher("/WEB-INF/views/hrm/activities.jsp").forward(request, response);
                return;
            }
            request.setAttribute("stats", dashboardService.loadDashboard());
            request.setAttribute("activePage", "hrm-dashboard");
            request.setAttribute("pageTitle", "HR Dashboard");
            request.getRequestDispatcher("/WEB-INF/views/hrm/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Không thể tải HR Manager workspace", e);
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        return Long.parseLong(value);
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null || value.isBlank() ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
