package com.dealxanh.app.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        System.out.println("=== AUTH FAILURE START ===");
        System.out.println("DEBUG: Auth failed: " + exception.getMessage());

        // Check if this is a wrong_login_page error
        String wrongLoginPage = request.getParameter("error");
        if ("wrong_login_page".equals(wrongLoginPage)) {
            System.out.println("DEBUG: Wrong login page error - redirecting to correct login");

            // Determine correct login page based on referer
            String referer = request.getHeader("Referer");
            String correctLoginPage = "/login"; // Default to buyer

            if (referer != null) {
                if (referer.contains("/seller/login")) {
                    correctLoginPage = "/seller/login";
                } else if (referer.contains("/admin/login")) {
                    correctLoginPage = "/admin/login";
                }
            }

            response.sendRedirect(correctLoginPage + "?error=please_use_correct_login");
            System.out.println("=== AUTH FAILURE END ===");
            return;
        }

        String failureUrl = "/login?error=true"; // Default to buyer login

        // Check Referer header first
        String referer = request.getHeader("Referer");
        if (referer != null) {
            if (referer.contains("/seller/")) {
                failureUrl = "/seller/login?error=true";
            } else if (referer.contains("/admin/")) {
                failureUrl = "/admin/login?error=true";
            }
        }

        // Fallback: check request URI (if accessing seller/admin login page directly)
        String requestUri = request.getRequestURI();
        if (requestUri != null) {
            if (requestUri.contains("/seller/login")) {
                failureUrl = "/seller/login?error=true";
            } else if (requestUri.contains("/admin/login")) {
                failureUrl = "/admin/login?error=true";
            }
        }

        System.out.println("DEBUG: Redirecting to: " + failureUrl);
        response.sendRedirect(failureUrl);
        System.out.println("=== AUTH FAILURE END ===");
    }
}
