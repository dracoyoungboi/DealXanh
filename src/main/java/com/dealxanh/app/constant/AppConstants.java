package com.dealxanh.app.constant;

public class AppConstants {

    // Order statuses
    public static final String ORDER_PENDING = "PENDING";
    public static final String ORDER_CONFIRMED = "CONFIRMED";
    public static final String ORDER_READY_FOR_PICKUP = "READY_FOR_PICKUP";
    public static final String ORDER_COMPLETED = "COMPLETED";
    public static final String ORDER_CANCELLED = "CANCELLED";

    // Payment statuses
    public static final String PAYMENT_UNPAID = "UNPAID";
    public static final String PAYMENT_PAID = "PAID";

    // Payment methods
    public static final String PAYMENT_BANK_TRANSFER = "BANK_TRANSFER";
    public static final String PAYMENT_MOMO = "MOMO";
    public static final String PAYMENT_ZALOPAY = "ZALOPAY";
    public static final String PAYMENT_CASH = "CASH";

    // Product types
    public static final String PRODUCT_SPECIFIC_DEAL = "SPECIFIC_DEAL";
    public static final String PRODUCT_BLIND_BOX = "BLIND_BOX";

    // Store statuses
    public static final String STORE_PENDING = "PENDING";
    public static final String STORE_ACTIVE = "ACTIVE";
    public static final String STORE_SUSPENDED = "SUSPENDED";

    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MODERATOR = "MODERATOR";
    public static final String ROLE_STORE_OWNER = "STORE_OWNER";
    public static final String ROLE_STORE_STAFF = "STORE_STAFF";
    public static final String ROLE_USER = "USER"; // khách hàng

    // Discount types
    public static final String DISCOUNT_PERCENT = "PERCENT";
    public static final String DISCOUNT_FIXED = "FIXED";

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 12;
    public static final int DEFAULT_PAGE = 0;

    // File upload
    public static final String UPLOAD_DIR = "uploads/dealxanh/";
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    // Notification types
    public static final String NOTIF_ORDER_NEW = "ORDER_NEW";
    public static final String NOTIF_ORDER_CONFIRMED = "ORDER_CONFIRMED";
    public static final String NOTIF_ORDER_READY = "ORDER_READY";
    public static final String NOTIF_ORDER_COMPLETED = "ORDER_COMPLETED";
    public static final String NOTIF_ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String NOTIF_STORE_APPROVED = "STORE_APPROVED";

    private AppConstants() {}
}
