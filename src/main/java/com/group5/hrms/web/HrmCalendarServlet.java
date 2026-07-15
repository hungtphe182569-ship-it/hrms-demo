package com.group5.hrms.web;

import com.group5.hrms.model.Account;
import com.group5.hrms.service.CalendarService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/hrm/calendar")
public class HrmCalendarServlet extends HttpServlet {
    private final CalendarService calendarService = new CalendarService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("events", calendarService.findAll());
            request.setAttribute("activePage", "hrm-calendar");
            request.setAttribute("pageTitle", "Company Calendar");
            request.getRequestDispatcher("/WEB-INF/views/hrm/calendar.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load calendar", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Account hrm = (Account) request.getSession().getAttribute("currentUser");
        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                calendarService.create(request.getParameter("title"), request.getParameter("eventType"),
                        request.getParameter("startAt"), request.getParameter("endAt"),
                        request.getParameter("description"), hrm.getId());
                Flash.success(request, "Tạo sự kiện thành công");
            } else if ("update".equals(action)) {
                calendarService.update(Long.parseLong(request.getParameter("eventId")),
                        request.getParameter("title"), request.getParameter("eventType"),
                        request.getParameter("startAt"), request.getParameter("endAt"),
                        request.getParameter("description"), hrm.getId());
                Flash.success(request, "Cập nhật sự kiện thành công");
            } else if ("delete".equals(action)) {
                calendarService.delete(Long.parseLong(request.getParameter("eventId")), hrm.getId());
                Flash.success(request, "Xóa sự kiện thành công");
            } else {
                throw new IllegalArgumentException("Action không hợp lệ");
            }
        } catch (Exception e) {
            Flash.error(request, e.getMessage() == null ? "Không thể xử lý lịch" : e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/hrm/calendar");
    }
}
