package vn.hactanco.socialnetwork.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.ListFriendFromProfileResponseDTO;
import vn.hactanco.socialnetwork.dto.PostResponseDTO;
import vn.hactanco.socialnetwork.dto.UserProfileDTO;
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
		int friendCount = friendshipService.countFriends(userId);
		// 🔥 lấy post (có pagination)
		int page = 0;
		int size = 10;

		List<PostResponseDTO> posts = postService.getPostsByUserDTO(userId, page, size, currentUser);
		boolean isFriend = false;
		boolean isPending = false;

		if (currentUser != null && !currentUser.getId().equals(userId)) {
			isFriend = friendshipService.isFriend(currentUser.getId(), userId);
			isPending = friendshipService.isPending(currentUser.getId(), userId);
		}

		model.addAttribute("isFriend", isFriend);
		model.addAttribute("isPending", isPending);
		model.addAttribute("profileUser", profileUser);
		model.addAttribute("posts", posts);
		model.addAttribute("currentUser", currentUser);
		model.addAttribute("friendCount", friendCount);
		model.addAttribute("suggestions", this.friendshipService.getSuggestions(currentUser.getId()));

		return "profile/profile";
	}

	@GetMapping("/api/user/{id}")
	@ResponseBody
	public UserProfileDTO getUserProfile(@PathVariable Long id) {
		return userService.getProfileDTO(id);
	}

	@GetMapping("/api/friends/{userId}")
	@ResponseBody
	public List<ListFriendFromProfileResponseDTO> getFriends(@PathVariable Long userId, HttpSession session) {
		User currentUser = (User) session.getAttribute("USER");
		return friendshipService.getFriendsDTO(userId, currentUser.getId());
	}
}