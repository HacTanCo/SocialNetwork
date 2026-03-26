package vn.hactanco.socialnetwork.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserProfileDTO {
	private Long id;
	private String name;
	private String avatar;
	private String bio;
	private long postCount;
}
