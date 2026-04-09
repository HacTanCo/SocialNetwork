package vn.hactanco.socialnetwork.controller;

import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.MessageDTO;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.service.MessageService;

@Controller
@RequiredArgsConstructor
public class MessageController {

	private final SimpMessagingTemplate messagingTemplate;
	private final MessageService messageService;

	@MessageMapping("/chat.send")
	public void send(MessageDTO dto) {

		// lưu DB
		MessageDTO saved = messageService.save(dto);

		// 🔥 đánh dấu isDelivered ngay sau khi lưu, vì đã gửi đến receiver rồi
		messageService.markDelivered(saved.getId());

		// 🔥 lấy lại message mới (đã updated)
		MessageDTO updated = messageService.getById(saved.getId());
		// gửi cho receiver
		messagingTemplate.convertAndSend("/topic/chat/" + dto.getReceiverId(), updated);
		// gửi lại cho sender (để hiển thị luôn)
		messagingTemplate.convertAndSend("/topic/chat/" + dto.getSenderId(), updated);
	}

	@MessageMapping("/chat.seen")
	public void seen(MessageDTO dto) {

		// B đã đọc tin của A
		messageService.markAsRead(dto.getReceiverId(), dto.getSenderId());

		// 🔥 gửi event về A với NGỮ NGHĨA ĐÚNG
		MessageDTO seenEvent = MessageDTO.builder().senderId(dto.getReceiverId()) // 👉 B (người đã xem)
				.receiverId(dto.getSenderId()) // 👉 A
				.build();

		messagingTemplate.convertAndSend("/topic/chat/" + dto.getSenderId(), seenEvent);

	}

	@GetMapping("/chat/{friendId}")
	@ResponseBody
	public List<MessageDTO> getChat(@PathVariable Long friendId, HttpSession session) {
		User currentUser = (User) session.getAttribute("USER");
		// 🔥 mark seen
		messageService.markAsRead(currentUser.getId(), friendId);
		return messageService.getChat(currentUser.getId(), friendId);
	}
}