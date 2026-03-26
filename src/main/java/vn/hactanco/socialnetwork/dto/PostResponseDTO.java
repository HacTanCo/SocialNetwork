package vn.hactanco.socialnetwork.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostResponseDTO {
	private Long postId;
	private String content;
	private Instant createdAt;

	private Long userId;
	private String userName;
	private String userAvatar;
	private List<PostMediaResponseDTO> medias;

	private boolean liked;
	private long likeCount;
	private long commentCount;
}
