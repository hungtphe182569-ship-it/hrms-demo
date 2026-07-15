package com.group5.hrms.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.group5.hrms.service.ReportService;
import jakarta.servlet.ServletException;
import java.io.IOException;

@WebServlet({"", "/home"})
public class HomeServlet extends HttpServlet {
    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            request.setAttribute("stats", reportService.loadDashboardStats());
            request.setAttribute("activePage", "home");
            request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Không thể tải trang chủ", e);
        }
    }
}
