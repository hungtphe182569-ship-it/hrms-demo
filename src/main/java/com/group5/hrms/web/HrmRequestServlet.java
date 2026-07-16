package com.group5.hrms.web;

import com.group5.hrms.model.Account;
import com.group5.hrms.service.HrmRequestService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/hrm/requests")
public class HrmRequestServlet extends HttpServlet {
    private final HrmRequestService requestService = new HrmRequestService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            boolean processed = "processed".equals(request.getParameter("view"));
            request.setAttribute("view", processed ? "processed" : "pending");
            request.setAttribute("requests", processed
                    ? requestService.findProcessed()
                    : requestService.findPending());
            request.setAttribute("activePage", "hrm-requests");
            request.setAttribute("pageTitle", "Request Management");
            request.getRequestDispatcher("/WEB-INF/views/hrm/requests.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load employee requests", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Account hrm = (Account) request.getSession().getAttribute("currentUser");
        String action = request.getParameter("action");
        try {
            if ("approve".equals(action)) {
                String status = requestService.approve(id(request), hrm.getId());
                if ("PENDING_ADMIN".equals(status)) {
                    Flash.success(request, "Forwarded to Admin");
                } else {
                    Flash.success(request, "Request approved");
                }
            } else if ("reject".equals(action)) {
                requestService.reject(id(request), hrm.getId(), request.getParameter("reason"));
                Flash.success(request, "Request rejected");
            } else {
                throw new IllegalArgumentException("Invalid action");
            }
        } catch (Exception e) {
            Flash.error(request, e.getMessage() == null ? "Failed, please try again" : e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/hrm/requests");
    }

    private long id(HttpServletRequest request) {
        return Long.parseLong(request.getParameter("requestId"));
    }
}
