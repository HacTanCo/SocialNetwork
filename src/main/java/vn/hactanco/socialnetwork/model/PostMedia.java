package vn.hactanco.socialnetwork.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.hactanco.socialnetwork.enums.MediaType;

@Entity
@Table(name = "post_medias")
@Getter
@Setter
public class PostMedia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String mediaUrl;
	@Enumerated(EnumType.STRING)
	private MediaType mediaType;

	private Instant createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id")
	private Post post;

	@PrePersist
	public void prePersist() {
		this.createdAt = Instant.now();
	}
}
