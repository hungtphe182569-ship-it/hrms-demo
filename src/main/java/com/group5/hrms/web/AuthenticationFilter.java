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
        if (!publicPath && isHrmArea(path)) {
            Account account = (Account) request.getSession(false).getAttribute("currentUser");
            if (!account.hasRole("HR Manager")) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Chỉ HR Manager mới có quyền truy cập HRM portal");
                return;
            }
            String permission = requiredHrmPermission(path);
            if (permission != null) {
                try {
                    if (!permissionDao.hasPermission(account.getId(), permission)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Thiếu permission " + permission + ". Hãy chạy sql/05-hrm-manager.sql.");
                        return;
                    }
                } catch (Exception e) {
                    throw new ServletException("Cannot verify HRM permission", e);
                }
            }
        }
        chain.doFilter(req, res);
    }

    private boolean isAdminArea(String path) {
        return path.startsWith("/accounts") || path.startsWith("/roles") || path.startsWith("/reports");
    }

    private boolean isHrmArea(String path) {
        return path.equals("/hrm") || path.startsWith("/hrm/");
    }

    private String requiredPermission(HttpServletRequest request, String path) {
        if (path.startsWith("/accounts")) return "MANAGE_ACCOUNT";
        if (path.startsWith("/roles")) return "MANAGE_ROLE";
        if (path.startsWith("/reports")) {
            return request.getParameter("format") == null ? "VIEW_REPORT" : "EXPORT_REPORT";
        }
        return null;
    }

    private String requiredHrmPermission(String path) {
        if (path.startsWith("/hrm/analytics")) return "VIEW_HR_ANALYTICS";
        if (path.startsWith("/hrm/reports")) return "VIEW_HR_REPORTS";
        if (path.startsWith("/hrm/activities")) return "VIEW_ACTIVITY_CENTER";
        if (path.startsWith("/hrm/requests")) return "MANAGE_REQUEST";
        if (path.startsWith("/hrm/leaves")) return "APPROVE_LEAVE";
        if (path.startsWith("/hrm/payroll")) return "MANAGE_PAYROLL";
        if (path.startsWith("/hrm/departments")) return "MANAGE_DEPARTMENT";
        if (path.startsWith("/hrm/calendar")) return "MANAGE_CALENDAR";
        if (path.equals("/hrm") || path.startsWith("/hrm/dashboard")) return "VIEW_HR_DASHBOARD";
        return "VIEW_HR_DASHBOARD";
    }
}
