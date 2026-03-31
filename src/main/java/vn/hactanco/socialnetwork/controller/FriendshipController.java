package vn.hactanco.socialnetwork.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.UserSuggestionResponseDTO;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.service.FriendshipService;

@Controller
@RequiredArgsConstructor
public class FriendshipController {
	private final FriendshipService friendshipService;

	@GetMapping("/friend")
	// @fortmat:off
	public String friendPage(Model model, HttpSession session, @RequestParam(defaultValue = "0") int friendPage,
			@RequestParam(defaultValue = "0") int suggestPage, @RequestParam(defaultValue = "0") int sentPage,
			@RequestParam(defaultValue = "0") int pendingPage,

			@RequestParam(defaultValue = "6") int size, @RequestParam(required = false) String keyword) {

		User user = (User) session.getAttribute("USER");
		Long userId = user.getId();

		// trim keyword cho sạch
		if (keyword != null) {
			keyword = keyword.trim();
		}

		if (keyword != null && !keyword.isEmpty()) {
			Page<User> friends = friendshipService.searchFriendsPage(userId, keyword, friendPage, size);
			model.addAttribute("friends", friends.getContent());
			model.addAttribute("friendPage", friends);

			Page<UserSuggestionResponseDTO> suggestions = friendshipService.searchSuggestionsPage(userId, keyword,
					suggestPage, size);
			model.addAttribute("suggestions", suggestions.getContent());
			model.addAttribute("suggestPage", suggestions);

			Page<User> sent = friendshipService.searchSentPage(userId, keyword, sentPage, size);
			model.addAttribute("sent", sent.getContent());
			model.addAttribute("sentPage", sent);

			Page<User> pending = friendshipService.searchPendingPage(userId, keyword, pendingPage, size);
			model.addAttribute("pending", pending.getContent());
			model.addAttribute("pendingPage", pending);
		} else {
			// ✅ FRIEND
			Page<User> friends = friendshipService.getFriendPhanTrang(userId, friendPage, size);
			model.addAttribute("friends", friends.getContent());
			model.addAttribute("friendPage", friends);

			// ✅ SUGGESTION (phải tạo thêm repo)
			Page<UserSuggestionResponseDTO> suggestions = friendshipService.getSuggestionPhanTrang(userId, suggestPage,
					size);
			model.addAttribute("suggestions", suggestions.getContent());
			model.addAttribute("suggestPage", suggestions);

			// ✅ SENT
			Page<User> sent = friendshipService.getSentPhanTrang(userId, sentPage, size);
			model.addAttribute("sent", sent.getContent());
			model.addAttribute("sentPage", sent);

			// ✅ PENDING
			Page<User> pending = friendshipService.getPendingPhanTrang(userId, pendingPage, size);
			model.addAttribute("pending", pending.getContent());
			model.addAttribute("pendingPage", pending);
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

	@PostMapping("/friend/remove-profile")
	public String removeFriendFromProfile(Long friend_id, HttpSession session) {
		User user = (User) session.getAttribute("USER");

		friendshipService.removeFriend(user.getId(), friend_id);

		return "redirect:/profile/" + friend_id;
	}
}
