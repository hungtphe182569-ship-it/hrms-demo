package com.group5.hrms.web;

import com.group5.hrms.model.DashboardStats;
import com.group5.hrms.service.ReportExportService;
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
    private final ReportExportService exportService = new ReportExportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            DashboardStats stats = reportService.loadDashboardStats();
            String format = request.getParameter("format");
            if ("xlsx".equals(format)) {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", "attachment; filename=hrms-admin-report.xlsx");
                exportService.writeExcel(stats, response.getOutputStream());
                return;
            }
            if ("pdf".equals(format)) {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=hrms-admin-report.pdf");
                exportService.writePdf(stats, response.getOutputStream());
                return;
            }
            if (format != null && !format.isBlank()) throw new IllegalArgumentException("Invalid report format");
            request.setAttribute("stats", stats);
            request.setAttribute("activePage", "reports");
            request.getRequestDispatcher("/WEB-INF/views/reports.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ServletException("Cannot load or export reports", e);
        }
    }
}
