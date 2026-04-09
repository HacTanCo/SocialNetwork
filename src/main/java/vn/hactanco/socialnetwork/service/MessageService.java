package vn.hactanco.socialnetwork.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.MessageDTO;
import vn.hactanco.socialnetwork.enums.MessageType;
import vn.hactanco.socialnetwork.model.Message;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.MessageRepository;
import vn.hactanco.socialnetwork.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class MessageService {

	private final MessageRepository messageRepository;
	private final UserRepository userRepository;

	public List<MessageDTO> getChat(Long userId, Long friendId) {
		return messageRepository.getChat(userId, friendId).stream().map(this::toDTO).collect(Collectors.toList());
	}

	public void markDelivered(Long messageId) {
		Message m = messageRepository.findById(messageId).orElseThrow();
		m.setDelivered(true);
		messageRepository.save(m);
	}

	public void markAsRead(Long userId, Long friendId) {
		List<Message> messages = messageRepository.getChat(userId, friendId);

		for (Message m : messages) {
			if (m.getReceiver().getId().equals(userId) && !m.isRead()) {
				m.setRead(true);
			}
		}

		messageRepository.saveAll(messages);
	}

	public MessageDTO getById(Long id) {
		Message m = messageRepository.findById(id).orElseThrow();
		return toDTO(m);
	}

	public MessageDTO save(MessageDTO dto) {

		User sender = userRepository.findById(dto.getSenderId()).orElseThrow();
		User receiver = userRepository.findById(dto.getReceiverId()).orElseThrow();
		// @@formatter:off
		Message message = Message.builder()
				.content(dto.getContent() != null ? dto.getContent() : "")
				.mediaUrl(dto.getMediaUrl())
				.type(dto.getType() != null ? dto.getType() : MessageType.TEXT)
				.sender(sender)
				.receiver(receiver)
				.isDelivered(false)
			    .isRead(false)
				.build();

		messageRepository.save(message);

		return toDTO(message);
	}

	private MessageDTO toDTO(Message m) {
	    return MessageDTO.builder()
	            .id(m.getId())
	            .content(m.getContent())
	            .mediaUrl(m.getMediaUrl())   // 🔥 thêm
	            .type(m.getType())           // 🔥 thêm (enum)
	            .senderId(m.getSender().getId())
	            .receiverId(m.getReceiver().getId())
	            .senderName(m.getSender().getName())
	            .senderAvatar(m.getSender().getAvatar())
	            .isRead(m.isRead())
	            .isDelivered(m.isDelivered())
	            .createdAt(m.getCreatedAt())
	            .build();
	}
}