package vn.hactanco.socialnetwork.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hactanco.socialnetwork.enums.MediaType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostMediaResponseDTO {
	private String mediaUrl;
	private MediaType mediaType;
}
