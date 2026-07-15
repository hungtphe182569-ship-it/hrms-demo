package com.group5.hrms.web;

import com.group5.hrms.service.AccountService;
import com.group5.hrms.service.RoleService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/accounts")
public class AccountServlet extends HttpServlet {
    private static final long DEMO_ADMIN_ID = 1L;
    private final AccountService accountService = new AccountService();
    private final RoleService roleService = new RoleService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            boolean includeDeleted = "true".equals(request.getParameter("includeDeleted"));
            request.setAttribute("accounts", accountService.findAll(includeDeleted));
            request.setAttribute("roles", roleService.findAll());
            request.setAttribute("includeDeleted", includeDeleted);
            request.setAttribute("activePage", "accounts");
            request.getRequestDispatcher("/WEB-INF/views/accounts.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load accounts", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                long roleId = Long.parseLong(request.getParameter("roleId"));
                accountService.create(
                        request.getParameter("username"), request.getParameter("email"),
                        request.getParameter("fullName"), request.getParameter("phone"),
                        request.getParameter("password"), roleId);
                Flash.success(request, "Tạo tài khoản thành công");
            } else if ("delete".equals(action)) {
                long accountId = Long.parseLong(request.getParameter("accountId"));
                accountService.softDelete(accountId, DEMO_ADMIN_ID, request.getParameter("reason"));
                Flash.success(request, "Soft delete tài khoản thành công");
            } else if ("resetPassword".equals(action)) {
                long accountId = Long.parseLong(request.getParameter("accountId"));
                String password = accountService.resetPassword(accountId, DEMO_ADMIN_ID);
                Flash.success(request, "Reset password thành công. Password tạm: " + password);
            } else {
                throw new IllegalArgumentException("Action không hợp lệ");
            }
        } catch (Exception e) {
            Flash.error(request, userMessage(e));
        }
        response.sendRedirect(request.getContextPath() + "/accounts");
    }

    private String userMessage(Exception e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? "Không thể xử lý yêu cầu" : message;
    }
}
