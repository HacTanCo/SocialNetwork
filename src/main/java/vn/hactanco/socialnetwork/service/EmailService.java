package vn.hactanco.socialnetwork.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender javaMailSender;

	public void sendOtp(String email, String otp) {

		SimpleMailMessage message = new SimpleMailMessage();

		message.setTo(email);
		message.setSubject("Reset Password OTP");
		message.setText("Your OTP code is: " + otp);

		this.javaMailSender.send(message);
	}

	public void sendUnlockRequest(String adminEmail, String userEmail, String userName, String unlockUrl) {

		SimpleMailMessage message = new SimpleMailMessage();

		message.setTo(adminEmail);
		message.setSubject("[Social Network] Yêu cầu mở khóa tài khoản - " + userName);
		message.setText(
				"Xin chào Admin,\n\n"
				+ "Người dùng sau đang yêu cầu mở khóa tài khoản:\n\n"
				+ "- Tên: " + userName + "\n"
				+ "- Email: " + userEmail + "\n\n"
				+ "Nhấn vào link sau để mở khóa tài khoản:\n"
				+ unlockUrl + "\n\n"
				+ "Nếu bạn không muốn mở khóa, hãy bỏ qua email này.\n\n"
				+ "---\n"
				+ "Social Network Admin System"
		);

		this.javaMailSender.send(message);
	}
}