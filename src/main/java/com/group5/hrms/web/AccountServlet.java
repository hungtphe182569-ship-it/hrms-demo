package com.group5.hrms.web;

import com.group5.hrms.model.Account;
import com.group5.hrms.service.AccountService;
import com.group5.hrms.service.RoleService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;

@WebServlet("/accounts")
public class AccountServlet extends HttpServlet {
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
        Account admin = (Account) request.getSession().getAttribute("currentUser");
        try {
            if ("create".equals(action)) {
                accountService.create(request.getParameter("username"), request.getParameter("email"),
                        request.getParameter("fullName"), request.getParameter("phone"),
                        request.getParameter("password"), roleIds(request));
                Flash.success(request, "Tạo tài khoản thành công");
            } else if ("update".equals(action)) {
                accountService.update(id(request), request.getParameter("email"),
                        request.getParameter("fullName"), request.getParameter("phone"),
                        roleIds(request), admin.getId());
                Flash.success(request, "Cập nhật tài khoản thành công");
            } else if ("status".equals(action)) {
                long accountId = id(request);
                String target = request.getParameter("targetStatus");
                accountService.changeStatus(accountId, request.getParameter("currentStatus"), target, admin.getId());
                if (!"ACTIVE".equals(target)) SessionRegistry.revokeAll(accountId);
                Flash.success(request, "Cập nhật trạng thái thành công");
            } else if ("delete".equals(action)) {
                long accountId = id(request);
                accountService.softDelete(accountId, admin.getId(), request.getParameter("reason"));
                SessionRegistry.revokeAll(accountId);
                Flash.success(request, "Soft delete tài khoản thành công");
            } else if ("resetPassword".equals(action)) {
                String password = accountService.resetPassword(id(request), admin.getId());
                Flash.success(request, "Reset password thành công. Password tạm: " + password);
            } else {
                throw new IllegalArgumentException("Action không hợp lệ");
            }
        } catch (Exception e) {
            Flash.error(request, e.getMessage() == null ? "Không thể xử lý yêu cầu" : e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/accounts");
    }

    private long id(HttpServletRequest request) {
        return Long.parseLong(request.getParameter("accountId"));
    }

    private long[] roleIds(HttpServletRequest request) {
        String[] values = request.getParameterValues("roleIds");
        if (values == null) return new long[0];
        return Arrays.stream(values).mapToLong(Long::parseLong).distinct().toArray();
    }
}
