package com.group5.hrms.web;

import com.group5.hrms.model.Account;
import com.group5.hrms.dao.PermissionDao;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {
    private final PermissionDao permissionDao = new PermissionDao();
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean publicPath = path.isEmpty() || path.equals("/") || path.equals("/login")
                || path.startsWith("/assets/") || path.equals("/favicon.ico");
        boolean authenticated = request.getSession(false) != null
                && request.getSession(false).getAttribute("currentUser") != null;
        if (!publicPath && !authenticated) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!publicPath && isAdminArea(path)) {
            Account account = (Account) request.getSession(false).getAttribute("currentUser");
            if (!account.hasRole("Admin")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Chỉ Admin mới có quyền truy cập Admin Center");
                return;
            }
            String permission = requiredPermission(request, path);
            if (permission != null) {
                try {
                    if (!permissionDao.hasPermission(account.getId(), permission)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Admin không có permission " + permission + ". Hãy chạy sql/04-admin-upgrade.sql.");
                        return;
                    }
                } catch (Exception e) {
                    throw new ServletException("Cannot verify Admin permission", e);
                }
            }
        }
        chain.doFilter(req, res);
    }

    private boolean isAdminArea(String path) {
        return path.startsWith("/accounts") || path.startsWith("/roles") || path.startsWith("/reports");
    }

    private String requiredPermission(HttpServletRequest request, String path) {
        if (path.startsWith("/accounts")) return "MANAGE_ACCOUNT";
        if (path.startsWith("/roles")) return "MANAGE_ROLE";
        if (path.startsWith("/reports")) {
            return request.getParameter("format") == null ? "VIEW_REPORT" : "EXPORT_REPORT";
        }
        return null;
    }
}
