package vn.hactanco.socialnetwork.websocket;

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
public class CallMessage {
	private String type; // offer | answer | ice | reject
	private Long from;
	private Long to;
	private String data; // SDP hoặc candidate JSON
	private String fromName;
}