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
}