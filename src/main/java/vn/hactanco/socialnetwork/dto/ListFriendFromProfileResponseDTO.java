package vn.hactanco.socialnetwork.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class ListFriendFromProfileResponseDTO {
	private Long userId;
	private String avatar;
	private String name;
	@JsonProperty("isFriend")
	private boolean isFriend;
}
