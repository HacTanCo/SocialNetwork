package vn.hactanco.socialnetwork.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.NotificationDTO;
import vn.hactanco.socialnetwork.model.Notification;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;

	public List<NotificationDTO> getByUser(Long userId) {
		return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId).stream()
				.map(n -> NotificationDTO.builder().id(n.getId()).content(n.getContent()).isRead(n.isRead())
						.createdAt(n.getCreatedAt()).senderName(n.getSender().getName())
						.senderAvatar(n.getSender().getAvatar())
						.postId(n.getPost() != null ? n.getPost().getId() : null).type(n.getType()).build())
				.toList();
	}

	public void createNotification(Long senderId, Long receiverId, String content, String type, Long postId) {
		Notification n = Notification.builder().content(content).type(type).sender(new User(senderId))
				.receiver(new User(receiverId)).post(null).build();

		notificationRepository.save(n);
	}

	public long countUnread(Long userId) {
		return notificationRepository.countByReceiverIdAndIsReadFalse(userId);
	}

	public void markAsRead(Long id) {
		Notification n = notificationRepository.findById(id).orElseThrow();
		n.setRead(true);
		notificationRepository.save(n);
	}
}
