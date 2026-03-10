package vn.hactanco.socialnetwork.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.ForgotPasswordRequest;
import vn.hactanco.socialnetwork.dto.RegisterRequest;
import vn.hactanco.socialnetwork.dto.ResetPasswordRequest;
import vn.hactanco.socialnetwork.dto.VerifyOtpRequest;
import vn.hactanco.socialnetwork.exception.ResourceAlreadyExistsException;
import vn.hactanco.socialnetwork.exception.ResourceNotFoundException;
import vn.hactanco.socialnetwork.service.AuthService;

@Controller
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@GetMapping("/register")
	public String registerPage(Model model) {

		model.addAttribute("user", new RegisterRequest());

		return "auth/register";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("user") RegisterRequest registerRequest, BindingResult result,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			return "auth/register";
		}

		try {

			this.authService.register(registerRequest);

			redirectAttributes.addFlashAttribute("success", "Đăng ký tài khoản thành công!");

		} catch (ResourceAlreadyExistsException | ResourceNotFoundException ex) {

			redirectAttributes.addFlashAttribute("error", ex.getMessage());

			return "redirect:/register";
		}

		return "redirect:/login";
	}

	@GetMapping("/login")
	public String loginPage() {
		return "auth/login";
	}

//	@PostMapping("/login")
//	public String login(@Valid @ModelAttribute("user") LoginRequest loginRequest, BindingResult result,
//			RedirectAttributes redirectAttributes, HttpSession session) {
//
//		if (result.hasErrors()) {
//			return "auth/login";
//		}
//
//		try {
//
//			User user = this.authService.login(loginRequest);
//
//			session.setAttribute("USER", user);
//
//			redirectAttributes.addFlashAttribute("success", "Đăng nhập thành công!");
//
//		} catch (ResourceNotFoundException ex) {
//
//			redirectAttributes.addFlashAttribute("error", ex.getMessage());
//
//			return "redirect:/login";
//		}
//
//		return "redirect:/home";
//	}

	// forgot password page
	@GetMapping("/forgot-password")
	public String forgotPasswordPage(Model model) {
		model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
		return "auth/forgot-password";
	}

	// send otp
	@PostMapping("/forgot-password")
	public String sendOtp(@Valid @ModelAttribute ForgotPasswordRequest forgotPasswordRequest, BindingResult result,
			HttpSession session, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {

			return "auth/forgot-password";
		}

		try {

			this.authService.sendOtp(forgotPasswordRequest.getEmail(), session);

			redirectAttributes.addFlashAttribute("success", "OTP đã được gửi đến email của bạn");

			return "redirect:/verify-otp";

		} catch (ResourceNotFoundException ex) {

			redirectAttributes.addFlashAttribute("error", ex.getMessage());

			return "redirect:/forgot-password";
		}
	}

	// verify page
	@GetMapping("/verify-otp")
	public String verifyOtpPage(HttpSession session, Model model) {

		if (session.getAttribute("resetUserId") == null) {
			return "redirect:/forgot-password";
		}
		model.addAttribute("verifyOtpRequest", new VerifyOtpRequest());
		return "auth/verify-otp";
	}

	// verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@Valid @ModelAttribute VerifyOtpRequest verifyOtpRequest, BindingResult result,
			@RequestParam(required = false) String action, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) {
		Long userId = (Long) session.getAttribute("resetUserId");
		String email = (String) session.getAttribute("emailFromResetPassword");
		// resend otp
		if ("resend".equals(action)) {

			this.authService.resendOtp(userId, email);

			redirectAttributes.addFlashAttribute("success", "OTP mới đã được gửi đến email của bạn");

			return "redirect:/verify-otp";
		}
		if (result.hasErrors()) {
			return "auth/verify-otp";
		}

		if (userId == null) {
			return "redirect:/forgot-password";
		}

		boolean valid = this.authService.verifyOtp(userId, verifyOtpRequest.getOtp());

		if (valid) {

			session.setAttribute("otpVerified", true);
			redirectAttributes.addFlashAttribute("success", "OTP hợp lệ! Bạn có thể đặt lại mật khẩu ngay bây giờ.");
			return "redirect:/reset-password";
		}

		redirectAttributes.addFlashAttribute("error", "OTP không hợp lệ hoặc đã hết hạn");

		return "redirect:/verify-otp";
	}

	// reset password page
	@GetMapping("/reset-password")
	public String resetPasswordPage(HttpSession session, Model model) {

		if (session.getAttribute("resetUserId") == null) {
			return "redirect:/forgot-password";
		}
		Boolean verified = (Boolean) session.getAttribute("otpVerified");
		if (verified == null || !verified) {
			return "redirect:/forgot-password";
		}
		model.addAttribute("resetPasswordRequest", new ResetPasswordRequest());
		return "auth/reset-password";
	}

	// reset password
	@PostMapping("/reset-password")
	public String resetPassword(@Valid @ModelAttribute ResetPasswordRequest resetPasswordRequest, BindingResult result,
			HttpSession session, Model model, RedirectAttributes redirectAttributes) {

		Boolean verified = (Boolean) session.getAttribute("otpVerified");
		Long userId = (Long) session.getAttribute("resetUserId");

		if (result.hasErrors()) {
			return "auth/reset-password";
		}

		if (verified == null || userId == null) {
			return "redirect:/forgot-password";
		}
		if (result.hasErrors()) {
			return "auth/reset-password";
		}
		if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())) {

			redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp");

			return "redirect:/reset-password";
		}

		this.authService.resetPassword(userId, resetPasswordRequest.getNewPassword());

		session.removeAttribute("otpVerified");
		session.removeAttribute("resetUserId");
		session.removeAttribute("emailFromResetPassword");
		redirectAttributes.addFlashAttribute("success",
				"Mật khẩu đã được đặt lại thành công! Bạn có thể đăng nhập ngay bây giờ.");
		return "redirect:/login";
	}

}