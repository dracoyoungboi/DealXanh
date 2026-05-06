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
}
