package vn.hactanco.socialnetwork.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.PostResponseDTO;
import vn.hactanco.socialnetwork.exception.ResourceNotFoundException;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.service.PostService;
import vn.hactanco.socialnetwork.service.UserService;

@Controller
@RequiredArgsConstructor
public class PostController {
	private final PostService postService;
	private final UserService userService;

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
	public String homePage(Model model, HttpSession session, Authentication authentication,
			@RequestParam(defaultValue = "0") int page) {
		if (session.getAttribute("USER") == null) {
			User user = userService.findUserByEmail(authentication.getName());
			session.setAttribute("USER", user);
		}

		List<PostResponseDTO> posts = postService.getFeedDTO(page, 10);

		model.addAttribute("posts", posts);

		return "home";
	}

	@PostMapping("/post/create")
	public String createPost(@RequestParam String content, @RequestParam MultipartFile[] files,
			Authentication authentication, RedirectAttributes redirectAttributes) {

		try {

			String email = authentication.getName();
			User user = userService.findUserByEmail(email);

			postService.createPost(content, files, user);

			redirectAttributes.addFlashAttribute("success", "Đăng bài thành công");

		} catch (ResourceNotFoundException ex) {

			redirectAttributes.addFlashAttribute("error", ex.getMessage());

		} catch (IOException ex) {

			redirectAttributes.addFlashAttribute("error", "Upload file thất bại");

		}
		return "redirect:/home";
	}

	@PostMapping("/post/delete/{id}")
	public String deletePost(@PathVariable Long id, Authentication authentication) {
		User user = this.userService.findUserByEmail(authentication.getName());

		postService.deletePost(id, user);

		return "redirect:/home";
	}

	@PostMapping("/post/update/{id}")
	public String editPost(@PathVariable Long id, @RequestParam String content,
			@RequestParam(required = false) MultipartFile[] files, Authentication authentication,
			RedirectAttributes redirectAttributes) {

		try {

			User user = userService.findUserByEmail(authentication.getName());

			postService.updatePost(id, content, files, user);

			redirectAttributes.addFlashAttribute("success", "Cập nhật bài viết thành công");

		} catch (ResourceNotFoundException ex) {

			redirectAttributes.addFlashAttribute("error", ex.getMessage());

		} catch (Exception ex) {

			redirectAttributes.addFlashAttribute("error", "Không thể cập nhật bài viết");

		}

		return "redirect:/home";
	}
}
