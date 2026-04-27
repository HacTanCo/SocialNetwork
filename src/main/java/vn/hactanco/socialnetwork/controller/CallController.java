package vn.hactanco.socialnetwork.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.websocket.CallMessage;

@Controller
@RequiredArgsConstructor
public class CallController {

	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/call")
	public void handle(CallMessage msg) {
		messagingTemplate.convertAndSend("/topic/call/" + msg.getTo(), msg);
	}
}