package com.dealxanh.app.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Redirects to the correct login page based on the requested URL path.
 * - /admin/** → /admin/login
 * - /seller/** → /seller/login
 * - else → /login
 */
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        String requestUrl = request.getRequestURI();

        // AJAX requests → 401 JSON
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)
                || requestUrl.startsWith("/admin/api")
                || requestUrl.startsWith("/api")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Vui lòng đăng nhập lại\"}");
            return;
        }

        String loginUrl;
        if (requestUrl.startsWith("/admin")) {
            loginUrl = "/admin/login";
        } else if (requestUrl.startsWith("/seller") || requestUrl.startsWith("/store")) {
            loginUrl = "/seller/login";
        } else {
            loginUrl = "/login";
        }

        response.sendRedirect(loginUrl);
    }
}
