package vn.hactanco.socialnetwork.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class PostController {
	@GetMapping("/")
	public String root(Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken)) {

			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

			if (isAdmin) {
				return "redirect:/admin/dashboard";
			}

			return "redirect:/home";
		}
		return "redirect:/login";
	}

	@GetMapping("/home")
	public String homePage() {
		return "home";
	}
}
