package vn.hactanco.socialnetwork.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotBlank(message = "name không được để trống")
	@Column(columnDefinition = "NVARCHAR(255)")
	private String name;

	@NotBlank(message = "email không được để trống")
	@Column(nullable = false, unique = true)
	private String email;

	@NotBlank(message = "password không được để trống")
	private String password;

	private String avatar;

	@Column(columnDefinition = "NVARCHAR(255)")
	private String bio;

	private Boolean isActive;

	private Instant createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id")
	private Role role;

}