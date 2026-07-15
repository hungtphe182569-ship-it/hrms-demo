package com.group5.hrms.web;

import com.group5.hrms.service.RoleService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/roles")
public class RoleServlet extends HttpServlet {
    private final RoleService roleService = new RoleService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("roles", roleService.findAll());
            request.setAttribute("activePage", "roles");
            request.getRequestDispatcher("/WEB-INF/views/roles.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load roles", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                roleService.create(request.getParameter("name"), request.getParameter("description"));
                Flash.success(request, "Tạo role thành công");
            } else if ("update".equals(action)) {
                roleService.update(Long.parseLong(request.getParameter("roleId")),
                        request.getParameter("name"), request.getParameter("description"));
                Flash.success(request, "Cập nhật role thành công");
            } else if ("delete".equals(action)) {
                roleService.delete(Long.parseLong(request.getParameter("roleId")));
                Flash.success(request, "Xóa role thành công");
            } else {
                throw new IllegalArgumentException("Action không hợp lệ");
            }
        } catch (Exception e) {
            Flash.error(request, e.getMessage() == null ? "Không thể xử lý role" : e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/roles");
    }
}
