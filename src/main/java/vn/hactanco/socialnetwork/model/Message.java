package vn.hactanco.socialnetwork.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hactanco.socialnetwork.enums.MessageType;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "NVARCHAR(MAX)")
	private String content;

	@Column(columnDefinition = "NVARCHAR(MAX)")
	private String mediaUrl;

	@Enumerated(EnumType.STRING)
	private MessageType type;

	private boolean isRead = false;

	private boolean isDelivered = false;

	private Instant createdAt;

	// người gửi
	@ManyToOne
	@JoinColumn(name = "sender_id")
	private User sender;

	// người nhận
	@ManyToOne
	@JoinColumn(name = "receiver_id")
	private User receiver;

	@PrePersist
	public void prePersist() {
		this.createdAt = Instant.now();
	}
}