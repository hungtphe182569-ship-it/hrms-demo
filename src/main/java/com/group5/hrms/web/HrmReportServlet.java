package com.group5.hrms.web;

import com.group5.hrms.model.Account;
import com.group5.hrms.model.HrmReportData;
import com.group5.hrms.model.HrmReportFilter;
import com.group5.hrms.service.DepartmentService;
import com.group5.hrms.service.HrmReportExportService;
import com.group5.hrms.service.HrmReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/hrm/reports")
public class HrmReportServlet extends HttpServlet {
    private static final List<String> TABS = List.of("OVERVIEW", "ATTENDANCE", "LEAVE", "PAYROLL", "PERFORMANCE");
    private final HrmReportService reportService = new HrmReportService();
    private final HrmReportExportService exportService = new HrmReportExportService();
    private final DepartmentService departmentService = new DepartmentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Account hrm = (Account) request.getSession().getAttribute("currentUser");
        String tab = value(request.getParameter("tab"), "OVERVIEW");
        HrmReportFilter filter = parseFilter(request);
        try {
            HrmReportData report;
            if ("pdf".equalsIgnoreCase(request.getParameter("format"))) {
                report = reportService.prepareReport(tab, filter);
                reportService.recordExport(hrm.getId(), report, filter);
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition",
                        "attachment; filename=hrm-" + report.getReportType().toLowerCase() + "-report.pdf");
                exportService.writePdf(report, response.getOutputStream());
                return;
            }

            report = reportService.viewReport(hrm.getId(), tab, filter);
            request.setAttribute("tabs", TABS);
            request.setAttribute("selectedTab", report.getReportType());
            request.setAttribute("filter", filter);
            request.setAttribute("report", report);
            request.setAttribute("departments", departmentService.findAll(false));
            request.setAttribute("activePage", "hrm-reports");
            request.setAttribute("pageTitle", "Reports Dashboard");
            request.getRequestDispatcher("/WEB-INF/views/hrm/reports.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ServletException("Cannot load HRM reports", e);
        }
    }

    private HrmReportFilter parseFilter(HttpServletRequest request) {
        HrmReportFilter filter = new HrmReportFilter();
        filter.setYear(parseInt(request.getParameter("year"), LocalDate.now().getYear()));
        filter.setDateFrom(parseDate(request.getParameter("dateFrom")));
        filter.setDateTo(parseDate(request.getParameter("dateTo")));
        filter.setReviewPeriod(request.getParameter("reviewPeriod"));
        filter.setDepartmentId(parseLong(request.getParameter("departmentId")));
        filter.setPage(parseInt(request.getParameter("page"), 1));
        return filter;
    }

    private LocalDate parseDate(String value) {
        try {
            return value == null || value.isBlank() ? null : LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(String value) {
        try {
            return value == null || value.isBlank() ? null : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null || value.isBlank() ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
