package com.group5.hrms.web;

import com.group5.hrms.model.Account;
import com.group5.hrms.service.DepartmentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/hrm/departments")
public class HrmDepartmentServlet extends HttpServlet {
    private final DepartmentService departmentService = new DepartmentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            boolean includeInactive = "true".equals(request.getParameter("includeInactive"));
            request.setAttribute("departments", departmentService.findAll(includeInactive));
            request.setAttribute("includeInactive", includeInactive);
            request.setAttribute("activePage", "hrm-departments");
            request.setAttribute("pageTitle", "Department Management");
            request.getRequestDispatcher("/WEB-INF/views/hrm/departments.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Cannot load departments", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Account hrm = (Account) request.getSession().getAttribute("currentUser");
        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                departmentService.create(request.getParameter("code"), request.getParameter("name"), hrm.getId());
                Flash.success(request, "Tạo phòng ban thành công");
            } else if ("update".equals(action)) {
                departmentService.update(Long.parseLong(request.getParameter("departmentId")),
                        request.getParameter("code"), request.getParameter("name"), hrm.getId());
                Flash.success(request, "Cập nhật phòng ban thành công");
            } else if ("delete".equals(action)) {
                departmentService.softDelete(Long.parseLong(request.getParameter("departmentId")), hrm.getId());
                Flash.success(request, "Soft delete phòng ban thành công");
            } else {
                throw new IllegalArgumentException("Action không hợp lệ");
            }
        } catch (Exception e) {
            Flash.error(request, e.getMessage() == null ? "Không thể xử lý phòng ban" : e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/hrm/departments");
    }
}
