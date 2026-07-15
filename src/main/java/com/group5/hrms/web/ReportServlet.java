package com.group5.hrms.web;

import com.group5.hrms.service.ReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/reports")
public class ReportServlet extends HttpServlet {
    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("stats", reportService.loadDashboardStats());
            request.setAttribute("activePage", "reports");
            request.getRequestDispatcher("/WEB-INF/views/reports.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load reports", e);
        }
    }
}
