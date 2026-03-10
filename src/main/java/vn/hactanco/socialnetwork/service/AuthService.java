package vn.hactanco.socialnetwork.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.RegisterRequest;
import vn.hactanco.socialnetwork.exception.ResourceAlreadyExistsException;
import vn.hactanco.socialnetwork.exception.ResourceNotFoundException;
import vn.hactanco.socialnetwork.model.OtpResetPassword;
import vn.hactanco.socialnetwork.model.Role;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.OtpResetPasswordRepository;
import vn.hactanco.socialnetwork.repository.RoleRepository;
import vn.hactanco.socialnetwork.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;
	private final OtpResetPasswordRepository otpResetPasswordRepository;

	public void register(RegisterRequest request) {
		if (!request.getPassword().equals(request.getConfirmPassword())) {
			throw new ResourceNotFoundException("Mật khẩu xác nhận không khớp");
		}
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new ResourceAlreadyExistsException("Email " + request.getEmail() + " đã tồn tại");
		}

		Role role = roleRepository.findByName("USER")
				.orElseThrow(() -> new ResourceNotFoundException("Role USER không tồn tại"));

		User user = new User();

		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(this.passwordEncoder.encode(request.getPassword()));
		user.setAvatar("/assets/images/default-avatar.jpg");
		user.setBio("");
		user.setIsActive(true);
		user.setCreatedAt(Instant.now());
		user.setRole(role);

		userRepository.save(user);
	}

//	public User login(LoginRequest request) {
//
//		User user = userRepository.findByEmail(request.getEmail())
//				.orElseThrow(() -> new ResourceNotFoundException("Email hoặc mật khẩu không đúng"));
//
//		if (!user.getPassword().equals(request.getPassword())) {
//			throw new ResourceNotFoundException("Email hoặc mật khẩu không đúng");
//		}
//
//		return user;
//	}

	public void sendOtp(String email, HttpSession session) {

		User user = this.userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Email không tồn tại"));

		String otp = String.valueOf(100000 + new Random().nextInt(900000));

		OtpResetPassword otpEntity = new OtpResetPassword();
		otpEntity.setOtpCode(otp);
		otpEntity.setExpiredAt(Instant.now().plus(1, ChronoUnit.MINUTES));
		otpEntity.setUsed(false);
		otpEntity.setUser(user);

		this.otpResetPasswordRepository.save(otpEntity);

		this.emailService.sendOtp(email, otp);

		session.setAttribute("resetUserId", user.getId());
		session.setAttribute("emailFromResetPassword", email);
	}

	public boolean verifyOtp(Long userId, String otp) {

		User user = userRepository.findById(userId).orElseThrow();

		Optional<OtpResetPassword> otpOptional = this.otpResetPasswordRepository
				.findTopByUserAndOtpCodeAndIsUsedFalseAndExpiredAtAfter(user, otp, Instant.now());

		if (otpOptional.isPresent()) {

			OtpResetPassword otpEntity = otpOptional.get();
			otpEntity.setUsed(true);
			this.otpResetPasswordRepository.save(otpEntity);

			return true;
		}

		return false;
	}

	public void resendOtp(Long userId, String email) {

		User user = userRepository.findById(userId).orElseThrow();

		String otp = String.valueOf(100000 + new Random().nextInt(900000));

		OtpResetPassword otpEntity = new OtpResetPassword();
		otpEntity.setOtpCode(otp);
		otpEntity.setExpiredAt(Instant.now().plus(1, ChronoUnit.MINUTES));
		otpEntity.setUsed(false);
		otpEntity.setUser(user);

		this.otpResetPasswordRepository.save(otpEntity);

		this.emailService.sendOtp(email, otp);
	}

	public void resetPassword(Long userId, String password) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

		user.setPassword(passwordEncoder.encode(password));

		this.userRepository.save(user);
	}

}