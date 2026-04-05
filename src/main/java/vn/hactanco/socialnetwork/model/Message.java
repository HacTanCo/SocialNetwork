package vn.hactanco.socialnetwork.model;

import java.time.Instant;

import jakarta.persistence.Entity;
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

	private String content;

	private boolean isRead = false;

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