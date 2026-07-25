package com.dealxanh.app.security;

import jakarta.servlet.http.Cookie;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import java.io.IOException;

/**
 * Handles sessions that are expired due to concurrent login (maxSessions=1).
 * When a user logs in from a second device, their first session is invalidated.
 * On the next request from the old session, this strategy fires and shows
 * "Tài khoản đang đăng nhập ở nơi khác".
 */
public class CustomSessionExpiredStrategy implements SessionInformationExpiredStrategy {

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event)
            throws IOException {

        String requestUrl = event.getRequest().getRequestURI();

        // Don't redirect if already on a login page
        if (requestUrl.contains("/login")) {
            return;
        }

        String loginUrl;
        if (requestUrl.startsWith("/admin")) {
            loginUrl = "/admin/login?kicked=true";
        } else if (requestUrl.startsWith("/staff") || requestUrl.startsWith("/seller")) {
            loginUrl = "/seller/login?kicked=true";
        } else {
            loginUrl = "/login?kicked=true";
        }

        // AJAX/API → 401 JSON
        String requestedWith = event.getRequest().getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)
                || requestUrl.startsWith("/admin/api")
                || requestUrl.startsWith("/api")) {
            event.getResponse().setStatus(401);
            event.getResponse().setContentType("application/json;charset=UTF-8");
            event.getResponse().getWriter().write(
                "{\"error\":\"kicked\",\"message\":\"Tai khoan dang dang nhap o noi khac\"}");
            return;
        }

        // Clear old session cookie
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        event.getResponse().addCookie(cookie);

        event.getResponse().sendRedirect(loginUrl);
    }
}
