package vn.hactanco.socialnetwork.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {

	private Long id;
	private String content;

	private Long senderId;
	private Long receiverId;

	private String senderName;
	private String senderAvatar;

	private boolean isRead;
	private boolean isDelivered;
	private Instant createdAt;
}