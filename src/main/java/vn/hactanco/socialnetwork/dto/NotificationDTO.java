package vn.hactanco.socialnetwork.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationDTO {
	private Long id;
	private String content;
	private boolean isRead;
	private Instant createdAt;

	private String senderName;
	private String senderAvatar;

	private Long postId;
	private String type;
}
