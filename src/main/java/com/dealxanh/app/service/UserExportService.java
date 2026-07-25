package com.dealxanh.app.service;

import com.dealxanh.app.entity.Role;
import com.dealxanh.app.entity.Store;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.RoleRepository;
import com.dealxanh.app.repository.StoreRepository;
import com.dealxanh.app.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý export danh sách người dùng ra file Excel.
 * Tái sử dụng UserRepository, StoreRepository, RoleRepository từ module hiện có.
 * Không thay đổi database, không ảnh hưởng CRUD hiện tại.
 */
@Service
public class UserExportService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private RoleRepository roleRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Export danh sách người dùng ra file Excel (.xlsx) dạng byte array.
     *
     * @param role   filter theo vai trò (buyer/seller/admin), có thể null
     * @param status filter theo trạng thái (active/inactive), có thể null
     * @param search từ khóa tìm kiếm (username/email/fullName/phone), có thể null
     * @return byte[] chứa nội dung file Excel
     * @throws IOException nếu có lỗi khi ghi workbook
     */
    public byte[] exportUsersToExcel(String role, String status, String search) throws IOException {
        // 1. Lấy danh sách users đã filter (giống logic trong AdminController.users())
        List<User> users = getFilteredUsers(role, status, search);

        // 2. Tạo workbook Excel
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Người dùng");

            // 3. Tạo style cho header
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle statusActiveStyle = createStatusActiveStyle(workbook);
            CellStyle statusInactiveStyle = createStatusInactiveStyle(workbook);

            // 4. Tạo header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Username", "Full Name", "Email", "Phone", "Role", "Status", "Created Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 5. Đổ dữ liệu users vào các row
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);

                // ID
                Cell idCell = row.createCell(0);
                idCell.setCellValue(user.getUserId() != null ? user.getUserId() : 0);
                idCell.setCellStyle(dataStyle);

                // Username
                Cell usernameCell = row.createCell(1);
                usernameCell.setCellValue(user.getUsername() != null ? user.getUsername() : "");
                usernameCell.setCellStyle(dataStyle);

                // Full Name
                Cell nameCell = row.createCell(2);
                nameCell.setCellValue(user.getFullName() != null ? user.getFullName() : "");
                nameCell.setCellStyle(dataStyle);

                // Email
                Cell emailCell = row.createCell(3);
                emailCell.setCellValue(user.getEmail() != null ? user.getEmail() : "");
                emailCell.setCellStyle(dataStyle);

                // Phone
                Cell phoneCell = row.createCell(4);
                // Lưu phone dạng text để tránh Excel tự format số
                phoneCell.setCellValue(user.getPhone() != null ? user.getPhone() : "");
                phoneCell.setCellStyle(dataStyle);

                // Role - lấy tên role từ DB relationship
                Cell roleCell = row.createCell(5);
                String roleName = "";
                if (user.getRole() != null && user.getRole().getName() != null) {
                    roleName = user.getRole().getName();
                }
                // Fallback: xác định role từ store relationship
                if (roleName.isEmpty()) {
                    roleName = determineRoleName(user);
                }
                roleCell.setCellValue(mapRoleToDisplayName(roleName));
                roleCell.setCellStyle(dataStyle);

                // Status
                Cell statusCell = row.createCell(6);
                boolean isActive = user.getActive();
                statusCell.setCellValue(isActive ? "Active" : "Inactive");
                statusCell.setCellStyle(isActive ? statusActiveStyle : statusInactiveStyle);

                // Created Date
                Cell dateCell = row.createCell(7);
                if (user.getCreatedAt() != null) {
                    dateCell.setCellValue(user.getCreatedAt().format(DATE_FORMATTER));
                } else {
                    dateCell.setCellValue("");
                }
                dateCell.setCellStyle(dateStyle);
            }

            // 6. Auto-size columns (giới hạn max width để tránh cột quá rộng)
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                // Max width = 50 ký tự (~256*50 units)
                int maxWidth = 256 * 50;
                if (currentWidth > maxWidth) {
                    sheet.setColumnWidth(i, maxWidth);
                }
                // Min width = 12 ký tự
                int minWidth = 256 * 12;
                if (currentWidth < minWidth) {
                    sheet.setColumnWidth(i, minWidth);
                }
            }

            // 7. Freeze header row
            sheet.createFreezePane(0, 1);

            // 8. Ghi workbook ra byte array
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                workbook.write(bos);
                return bos.toByteArray();
            }
        }
    }

    /**
     * Lấy danh sách người dùng đã filter, sao chép logic từ AdminController.users().
     * Không phân trang — lấy toàn bộ để export.
     */
    private List<User> getFilteredUsers(String role, String status, String search) {
        List<User> users;
        boolean hasSearchFilter = search != null && !search.isEmpty();

        // Filter theo role
        if (role != null && !role.isEmpty()) {
            if ("buyer".equals(role)) {
                Role userRole = findRoleByName("ROLE_USER", "USER");
                users = userRole != null ? userRepository.findByRole(userRole) : new ArrayList<>();
            } else if ("seller".equals(role)) {
                List<Store> allStores = storeRepository.findAll();
                users = new ArrayList<>();
                for (Store store : allStores) {
                    if (store.getOwner() != null) {
                        users.add(store.getOwner());
                    }
                    if (store.getStaffList() != null && !store.getStaffList().isEmpty()) {
                        users.addAll(store.getStaffList());
                    }
                }
                users = users.stream().distinct().collect(Collectors.toList());
            } else if ("admin".equals(role)) {
                Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);
                Role moderatorRole = roleRepository.findByName("ROLE_MODERATOR").orElse(null);
                users = new ArrayList<>();
                if (adminRole != null) {
                    users.addAll(userRepository.findByRole(adminRole));
                }
                if (moderatorRole != null) {
                    users.addAll(userRepository.findByRole(moderatorRole));
                }
            } else {
                users = userRepository.findAll();
            }
        } else {
            users = userRepository.findAll();
        }

        // Filter theo trạng thái active
        if ("active".equals(status)) {
            users = users.stream()
                    .filter(User::getActive)
                    .collect(Collectors.toList());
        } else if ("inactive".equals(status)) {
            users = users.stream()
                    .filter(u -> !u.getActive())
                    .collect(Collectors.toList());
        }

        // Filter theo từ khóa tìm kiếm
        if (hasSearchFilter) {
            final String searchLower = search.toLowerCase();
            users = users.stream()
                    .filter(u -> (u.getUsername() != null && u.getUsername().toLowerCase().contains(searchLower))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchLower))
                            || (u.getFullName() != null && u.getFullName().toLowerCase().contains(searchLower))
                            || (u.getPhone() != null && u.getPhone().contains(searchLower)))
                    .collect(Collectors.toList());
        }

        return users;
    }

    /**
     * Xác định tên role từ DB role hoặc store relationship (fallback).
     */
    private String determineRoleName(User user) {
        // Thử từ DB role
        if (user.getRole() != null && user.getRole().getName() != null) {
            return user.getRole().getName();
        }
        // Fallback: kiểm tra store relationship
        List<Store> allStores = storeRepository.findAll();
        for (Store store : allStores) {
            if (store.getOwner() != null && store.getOwner().getUserId().equals(user.getUserId())) {
                return "ROLE_STORE_OWNER";
            }
            if (store.getStaffList() != null) {
                for (User staff : store.getStaffList()) {
                    if (staff.getUserId().equals(user.getUserId())) {
                        return "ROLE_STORE_STAFF";
                    }
                }
            }
        }
        return "ROLE_USER";
    }

    /**
     * Map tên role trong DB sang tên hiển thị tiếng Việt.
     */
    private String mapRoleToDisplayName(String roleName) {
        if (roleName == null) return "Unknown";
        return switch (roleName.toUpperCase()) {
            case "ROLE_ADMIN", "ADMIN" -> "Admin";
            case "ROLE_MODERATOR", "MODERATOR" -> "Moderator";
            case "ROLE_STORE_OWNER", "STORE_OWNER" -> "Store Owner";
            case "ROLE_STORE_STAFF", "STORE_STAFF" -> "Store Staff";
            case "ROLE_USER", "USER" -> "Buyer";
            default -> roleName;
        };
    }

    /**
     * Tìm Role theo tên, thử cả 2 format "ROLE_XXX" và "XXX".
     */
    private Role findRoleByName(String withPrefix, String withoutPrefix) {
        return roleRepository.findByName(withPrefix)
                .orElse(roleRepository.findByName(withoutPrefix).orElse(null));
    }

    // ==================== Excel Cell Styles ====================

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(false);
        return style;
    }

    private CellStyle createDateStyle(XSSFWorkbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createStatusActiveStyle(XSSFWorkbook workbook) {
        CellStyle style = createDataStyle(workbook);
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createStatusInactiveStyle(XSSFWorkbook workbook) {
        CellStyle style = createDataStyle(workbook);
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.RED.getIndex());
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
