package com.dealxanh.app.controller.admin;

import com.dealxanh.app.service.UserExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller riêng cho chức năng Export Excel User Management.
 * Tách biệt khỏi AdminController để không ảnh hưởng CRUD hiện có.
 */
@Controller
@RequestMapping("/admin")
public class UserExportController {

    @Autowired
    private UserExportService userExportService;

    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Export danh sách người dùng ra file Excel.
     * GET /admin/users/export?role=&status=&search=
     *
     * Hỗ trợ đầy đủ bộ lọc giống trang Quản lý Người dùng:
     * - role: buyer | seller | admin
     * - status: active | inactive
     * - search: từ khóa tìm kiếm (username, email, fullName, phone)
     */
    @GetMapping("/users/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public void exportUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            HttpServletResponse response) throws IOException {

        try {
            // 1. Gọi service export ra byte array
            byte[] excelBytes = userExportService.exportUsersToExcel(role, status, search);

            // 2. Tạo tên file với timestamp để tránh trùng lặp
            String timestamp = LocalDateTime.now().format(FILE_DATE_FORMATTER);
            String filename = "users" + (search != null && !search.isEmpty() ? "_search" : "")
                    + (role != null && !role.isEmpty() ? "_" + role : "")
                    + (status != null && !status.isEmpty() ? "_" + status : "")
                    + "_" + timestamp + ".xlsx";

            // 3. Set response headers cho Excel download
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentLength(excelBytes.length);
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            // 4. Ghi dữ liệu ra response output stream
            response.getOutputStream().write(excelBytes);
            response.getOutputStream().flush();

        } catch (IOException e) {
            // Log lỗi chi tiết
            System.err.println("[UserExportController] Lỗi khi export Excel: " + e.getMessage());
            e.printStackTrace();

            // Trả về lỗi cho client
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Lỗi khi tạo file Excel: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            // Bắt tất cả exception khác (NullPointerException, etc.)
            System.err.println("[UserExportController] Lỗi không xác định khi export Excel: " + e.getMessage());
            e.printStackTrace();

            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Lỗi hệ thống khi tạo file Excel. Vui lòng thử lại sau.\"}");
        }
    }
}
