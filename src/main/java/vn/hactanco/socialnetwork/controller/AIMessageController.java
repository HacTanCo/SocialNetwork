package vn.hactanco.socialnetwork.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.model.AIMessage;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.service.AIMesageService;

@RestController
@RequiredArgsConstructor
public class AIMessageController {

	private final AIMesageService aiMesageService;

	@PostMapping("/ai/chat")
	public Map<String, String> chat(@RequestParam String message, HttpSession session) throws Exception {

		User user = (User) session.getAttribute("USER");

		Map<String, String> res = new HashMap<>();

		if (user == null) {
			res.put("reply", "❌ Chưa đăng nhập");
			return res;
		}

		if (message == null || message.trim().isEmpty()) {
			res.put("reply", "❌ Tin nhắn rỗng");
			return res;
		}

		String reply = aiMesageService.chat(user.getId(), message);

		res.put("reply", reply);
		return res;
	}

	@GetMapping("/ai/history")
	public List<AIMessage> history(HttpSession session) {
		User user = (User) session.getAttribute("USER");
		return aiMesageService.getHistory(user.getId());
	}
}
