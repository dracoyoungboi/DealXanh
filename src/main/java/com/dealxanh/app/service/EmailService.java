package com.dealxanh.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("DealXanh - Mã xác thực (OTP)");
            message.setText("Xin chào,\n\nMã xác thực OTP của bạn là: " + otp + "\n\n"
                    + "Tuyệt đối không chia sẻ mã này với bất kỳ ai để đảm bảo an toàn tài khoản.\n"
                    + "Mã này sẽ hết hạn trong 5 phút.\n\n"
                    + "Cảm ơn,\nĐội ngũ DealXanh.");

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
            // Có thể thả Exception để Controller biết nếu cần
        }
    }

    public void sendPasswordResetOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("DealXanh - Đặt lại mật khẩu");
            message.setText("Xin chào,\n\nBạn đã yêu cầu đặt lại mật khẩu cho tài khoản DealXanh.\n\n"
                    + "Mã OTP để đặt lại mật khẩu là: " + otp + "\n\n"
                    + "⚠️ Tuyệt đối không chia sẻ mã này với bất kỳ ai để đảm bảo an toàn tài khoản.\n"
                    + "⏰ Mã này sẽ hết hạn sau 10 phút.\n\n"
                    + "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n"
                    + "Trân trọng,\nĐội ngũ DealXanh.");

            mailSender.send(message);
            System.out.println("Password reset OTP email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email đặt lại mật khẩu: " + e.getMessage());
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

    public void sendSellerReReviewEmail(String toEmail, String sellerName, String storeName, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("DealXanh - Hồ sơ của bạn đang được xét duyệt lại");

            StringBuilder text = new StringBuilder();
            text.append("Xin chào ").append(sellerName != null ? sellerName : "bạn").append(",\n\n");
            text.append("Cửa hàng \"").append(storeName != null ? storeName : "của bạn").append("\" của bạn trên DealXanh đã được chuyển về trạng thái chờ xét duyệt để được xem xét lại.\n\n");

            if (reason != null && !reason.trim().isEmpty()) {
                text.append("Lý do từ chối trước đó: ").append(reason).append("\n\n");
            }

            text.append("Đội ngũ DealXanh sẽ xem xét lại hồ sơ của bạn trong thời gian sớm nhất.\n");
            text.append("Vui lòng kiểm tra và cập nhật thông tin cửa hàng nếu cần thiết.\n\n");
            text.append("Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi qua email này.\n\n");
            text.append("Trân trọng,\nĐội ngũ DealXanh.");

            message.setText(text.toString());
            mailSender.send(message);
            System.out.println("Seller re-review email sent to: " + toEmail + " for store: " + storeName);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email xét duyệt lại: " + e.getMessage());
        }
    }
}
