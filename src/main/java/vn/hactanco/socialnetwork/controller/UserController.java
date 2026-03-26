package vn.hactanco.socialnetwork.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // ✅ ĐÚNG
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.PostResponseDTO;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.service.FriendshipService;
import vn.hactanco.socialnetwork.service.PostService;
import vn.hactanco.socialnetwork.service.UserService;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final PostService postService;
	private final FriendshipService friendshipService;

	@GetMapping("/profile/{userId}")
	public String profile(@PathVariable Long userId, Model model, HttpSession session) {

		User currentUser = (User) session.getAttribute("USER");

		// 🔥 thông tin user
		User profileUser = userService.getById(userId);

		// 🔥 lấy post (có pagination)
		int page = 0;
		int size = 10;

		List<PostResponseDTO> posts = postService.getPostsByUserDTO(userId, page, size, currentUser);

		model.addAttribute("profileUser", profileUser);
		model.addAttribute("posts", posts);
		model.addAttribute("currentUser", currentUser);
		model.addAttribute("suggestions", this.friendshipService.getSuggestions(currentUser.getId()));
		System.out.println(profileUser.toString());
		return "profile/profile";
	}
}