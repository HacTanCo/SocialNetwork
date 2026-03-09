package vn.hactanco.socialnetwork.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.LoginRequest;
import vn.hactanco.socialnetwork.dto.RegisterRequest;
import vn.hactanco.socialnetwork.exception.ResourceAlreadyExistsException;
import vn.hactanco.socialnetwork.exception.ResourceNotFoundException;
import vn.hactanco.socialnetwork.model.User;
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
	public String loginPage(Model model) {

		model.addAttribute("user", new LoginRequest());

		return "auth/login";
	}

	@PostMapping("/login")
	public String login(@Valid @ModelAttribute("user") LoginRequest loginRequest, BindingResult result,
			RedirectAttributes redirectAttributes, HttpSession session) {

		if (result.hasErrors()) {
			return "auth/login";
		}

		try {

			User user = this.authService.login(loginRequest);

			session.setAttribute("USER", user);

			redirectAttributes.addFlashAttribute("success", "Đăng nhập thành công!");

		} catch (ResourceNotFoundException ex) {

			redirectAttributes.addFlashAttribute("error", ex.getMessage());

			return "redirect:/login";
		}

		return "redirect:/home";
	}
}