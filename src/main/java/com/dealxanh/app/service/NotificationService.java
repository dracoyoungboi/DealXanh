package com.dealxanh.app.service;

import com.dealxanh.app.entity.Notification;
import com.dealxanh.app.entity.User;
import com.dealxanh.app.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getRecentNotifications(User user, int limit) {
        return notificationRepository
            .findByUserUserIdOrderByCreatedAtDesc(user.getUserId(), PageRequest.of(0, limit))
            .getContent();
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserUserIdAndIsReadFalse(user.getUserId());
    }

    public Notification createNotification(User user, String title, String message, String type, String linkUrl) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setLinkUrl(linkUrl);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        return notificationRepository.save(n);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository
            .findByUserUserIdOrderByCreatedAtDesc(user.getUserId(), PageRequest.of(0, 50))
            .getContent();
        for (Notification n : unread) {
            if (!n.getIsRead()) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        }
    }

    public void notifyOrderStatusChange(User user, Long orderId, String newStatus) {
        String title;
        String message;
        String type;
        switch (newStatus) {
            case "CONFIRMED":
                title = "Đơn hàng đã được xác nhận";
                message = "Đơn hàng #" + orderId + " của bạn đã được cửa hàng xác nhận.";
                type = "ORDER_CONFIRMED";
                break;
            case "READY_FOR_PICKUP":
                title = "Đơn hàng sẵn sàng nhận";
                message = "Đơn hàng #" + orderId + " đã sẵn sàng để bạn đến nhận.";
                type = "ORDER_READY";
                break;
            case "COMPLETED":
                title = "Đơn hàng đã hoàn thành";
                message = "Đơn hàng #" + orderId + " đã hoàn thành. Cảm ơn bạn!";
                type = "ORDER_COMPLETED";
                break;
            case "CANCELLED":
                title = "Đơn hàng đã bị hủy";
                message = "Đơn hàng #" + orderId + " đã bị hủy.";
                type = "ORDER_CANCELLED";
                break;
            default:
                return;
        }
        createNotification(user, title, message, type, "/buyer/orders");
    }

    public void notifyNewDealNearby(User user, String storeName, String dealName, Long dealId) {
        createNotification(user,
            "Deal mới gần bạn",
            storeName + " vừa đăng deal \"" + dealName + "\" gần vị trí của bạn.",
            "DEAL_NEARBY",
            "/products/" + dealId);
    }
}
