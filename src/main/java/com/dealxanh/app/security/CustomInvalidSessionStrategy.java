package com.dealxanh.app.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.session.InvalidSessionStrategy;

import java.io.IOException;

public class CustomInvalidSessionStrategy implements InvalidSessionStrategy {

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String requestUrl = request.getRequestURI();

        // Don't redirect if already on a login page (prevents redirect loop)
        if (requestUrl.contains("/login")) {
            return;
        }

        String loginUrl;
        String originalUrl = requestUrl;
        // Preserve query string for GET requests
        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            originalUrl = requestUrl + "?" + request.getQueryString();
        }
        if (requestUrl.startsWith("/admin")) {
            loginUrl = "/admin/login?expired=true&redirect=" + java.net.URLEncoder.encode(originalUrl, "UTF-8");
        } else if (requestUrl.startsWith("/staff") || requestUrl.startsWith("/seller") || requestUrl.startsWith("/store")) {
            loginUrl = "/seller/login?expired=true&redirect=" + java.net.URLEncoder.encode(originalUrl, "UTF-8");
        } else {
            loginUrl = "/login?expired=true&redirect=" + java.net.URLEncoder.encode(originalUrl, "UTF-8");
        }

        // For AJAX requests, return 401
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith) || requestUrl.startsWith("/admin/api") || requestUrl.startsWith("/api")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"session_expired\",\"redirect\":\"" + loginUrl + "\"}");
            return;
        }

        // Xóa session cookie cũ để tránh redirect loop
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        response.sendRedirect(loginUrl);
    }
}
