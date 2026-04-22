package vn.hactanco.socialnetwork.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.ai.AIService;
import vn.hactanco.socialnetwork.model.AIMessage;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.AIMessageRepository;

@Service
@RequiredArgsConstructor
public class AIMesageService {

	private final AIMessageRepository aiMessageRepository;
	private final AIService aiService;

	public String chat(Long userId, String message) throws Exception {

		// 1. Lưu câu hỏi user
		aiMessageRepository.save(AIMessage.builder().user(new User(userId)).role("USER").content(message).build());

		// 2. Gọi AI (KHÔNG history)
		String reply = aiService.callGroq(message);

		// 3. Lưu câu trả lời AI
		aiMessageRepository.save(AIMessage.builder().user(new User(userId)).role("ASSISTANT").content(reply).build());

		return reply;
	}

	public List<AIMessage> getHistory(Long userId) {
		return aiMessageRepository.findByUser_IdOrderByCreatedAtAsc(userId);
	}
}