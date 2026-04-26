package com.dealxanh.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorCode", 404);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    // Handle our own custom AccessDeniedException (not Spring Security's)
    @ExceptionHandler(com.dealxanh.app.exception.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(com.dealxanh.app.exception.AccessDeniedException ex, Model model) {
        model.addAttribute("errorCode", 403);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/403";
    }

    // Handle Spring Security AccessDeniedException
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleSpringForbidden(org.springframework.security.access.AccessDeniedException ex, Model model) {
        model.addAttribute("errorCode", 403);
        model.addAttribute("errorMessage", "Bạn không có quyền truy cập trang này.");
        return "error/403";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model, HttpServletRequest request) {
        model.addAttribute("errorCode", 500);
        model.addAttribute("errorMessage", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        return "error/500";
    }
}
