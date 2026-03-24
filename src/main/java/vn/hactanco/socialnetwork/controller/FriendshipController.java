package vn.hactanco.socialnetwork.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.service.FriendshipService;

@Controller
@RequiredArgsConstructor
public class FriendshipController {
	private final FriendshipService friendshipService;

	@GetMapping("/friend")
	public String friendPage(Model model, HttpSession session, @RequestParam(required = false) String keyword) {

		User user = (User) session.getAttribute("USER");

		Long userId = user.getId();

		// trim keyword cho sạch
		if (keyword != null) {
			keyword = keyword.trim();
		}

		if (keyword != null && !keyword.isEmpty()) {
			// 👉 SEARCH
			model.addAttribute("friends", friendshipService.searchFriends(userId, keyword));

			model.addAttribute("pending", friendshipService.searchPending(userId, keyword));

			model.addAttribute("suggestions", friendshipService.searchSuggestions(userId, keyword));

		} else {
			// 👉 NORMAL
			model.addAttribute("friends", friendshipService.getFriends(userId));

			model.addAttribute("pending", friendshipService.getPendingRequests(userId));

			model.addAttribute("suggestions", friendshipService.getSuggestions(userId)); // full User
		}

		return "friendships/friendship";
	}

	@PostMapping("/friend/toggle")
	@ResponseBody
	public String toggleFriend(Long friend_id, HttpSession session) {
		User user = (User) session.getAttribute("USER");

		// check có tồn tại quan hệ chưa (không cần status)
		boolean exists = friendshipService.exists(user.getId(), friend_id);

		if (exists) {
			friendshipService.removeFriend(user.getId(), friend_id);
			return "unfollowed";
		} else {
			friendshipService.sendFriendRequest(user.getId(), friend_id);
			return "followed";
		}
	}

	// ================= SEND =================
	@PostMapping("/friend/send")
	public String sendFriend(Long friend_id, HttpSession session) {
		User user = (User) session.getAttribute("USER");

		friendshipService.sendFriendRequest(user.getId(), friend_id);

		return "redirect:/friend";
	}

	// ================= ACCEPT =================
	@PostMapping("/friend/accept")
	public String acceptFriend(Long friend_id, HttpSession session) {
		User user = (User) session.getAttribute("USER");

		friendshipService.acceptFriend(user.getId(), friend_id);

		return "redirect:/friend";
	}

	// ================= REJECT =================
	@PostMapping("/friend/reject")
	public String rejectFriend(Long friend_id, HttpSession session) {
		User user = (User) session.getAttribute("USER");

		friendshipService.rejectFriend(user.getId(), friend_id);

		return "redirect:/friend";
	}

	// ================= REMOVE =================
	@PostMapping("/friend/remove")
	public String removeFriend(Long friend_id, HttpSession session) {
		User user = (User) session.getAttribute("USER");

		friendshipService.removeFriend(user.getId(), friend_id);

		return "redirect:/friend";
	}
}
