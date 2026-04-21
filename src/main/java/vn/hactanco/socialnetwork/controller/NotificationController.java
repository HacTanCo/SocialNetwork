package vn.hactanco.socialnetwork.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.NotificationDTO;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.service.NotificationService;

@RestController
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/notifications")
	public List<NotificationDTO> getAll(HttpSession session) {
		User user = (User) session.getAttribute("USER");
		return notificationService.getByUser(user.getId());
	}

	@GetMapping("/notifications/unread-count")
	public long countUnread(HttpSession session) {
		User user = (User) session.getAttribute("USER");
		return notificationService.countUnread(user.getId());
	}

	@PostMapping("/notifications/read/{id}")
	public void markRead(@PathVariable Long id) {
		notificationService.markAsRead(id);
	}
}
