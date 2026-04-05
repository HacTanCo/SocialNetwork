package vn.hactanco.socialnetwork.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.MessageDTO;
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

	public MessageDTO save(MessageDTO dto) {

		User sender = userRepository.findById(dto.getSenderId()).orElseThrow();
		User receiver = userRepository.findById(dto.getReceiverId()).orElseThrow();

		Message message = Message.builder().content(dto.getContent()).sender(sender).receiver(receiver).build();

		messageRepository.save(message);

		return toDTO(message);
	}

	private MessageDTO toDTO(Message m) {
		return MessageDTO.builder().id(m.getId()).content(m.getContent()).senderId(m.getSender().getId())
				.receiverId(m.getReceiver().getId()).senderName(m.getSender().getName())
				.senderAvatar(m.getSender().getAvatar()).createdAt(m.getCreatedAt()).build();
	}
}