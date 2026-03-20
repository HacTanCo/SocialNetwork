package vn.hactanco.socialnetwork.dto;

import java.util.List;

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
public class CommentResponseDTO {
	private Long id;
	private String content;
	private String userName;
	private String userAvatar;
	private String timeAgo;
	private boolean isOwner;

	private List<ReplyResponseDTO> replies;
}
