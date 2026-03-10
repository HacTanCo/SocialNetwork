package vn.hactanco.socialnetwork.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class PostController {
	@GetMapping("/")
	public String root() {
		return "redirect:/login";
	}

	@GetMapping("/home")
	public String homePage() {
		return "home";
	}
}
