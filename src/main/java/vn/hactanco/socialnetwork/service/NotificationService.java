package vn.hactanco.socialnetwork.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.NotificationDTO;
import vn.hactanco.socialnetwork.model.Notification;
import vn.hactanco.socialnetwork.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;

	public List<NotificationDTO> getByUser(Long userId) {
		return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId).stream()
				.map(n -> NotificationDTO.builder().id(n.getId()).content(n.getContent()).isRead(n.isRead())
						.createdAt(n.getCreatedAt()).senderName(n.getSender().getName())
						.senderAvatar(n.getSender().getAvatar()).postId(n.getPost().getId()).type(n.getType()).build())
				.toList();
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
