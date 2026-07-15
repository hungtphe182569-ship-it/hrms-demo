package com.group5.hrms.web;

import jakarta.servlet.http.HttpServletRequest;

public final class Flash {
    private Flash() {
    }

    public static void success(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashType", "success");
        request.getSession().setAttribute("flashMessage", message);
    }

    public static void error(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashType", "error");
        request.getSession().setAttribute("flashMessage", message);
    }
}
