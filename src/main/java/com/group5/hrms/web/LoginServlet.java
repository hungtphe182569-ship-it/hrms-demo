package com.group5.hrms.web;

import com.group5.hrms.model.Account;
import com.group5.hrms.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getSession(false) != null && request.getSession(false).getAttribute("currentUser") != null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Account account = authService.login(request.getParameter("username"), request.getParameter("password")).orElse(null);
            if (account == null) {
                request.setAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng, hoặc tài khoản đã bị khóa.");
                request.setAttribute("username", request.getParameter("username"));
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                return;
            }
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) oldSession.invalidate();
            HttpSession session = request.getSession(true);
            session.setAttribute("currentUser", account);
            response.sendRedirect(request.getContextPath() + "/home");
        } catch (Exception e) {
            throw new ServletException("Không thể đăng nhập", e);
        }
    }
}
