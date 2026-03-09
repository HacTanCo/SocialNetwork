package vn.hactanco.socialnetwork.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.LoginRequest;
import vn.hactanco.socialnetwork.dto.RegisterRequest;
import vn.hactanco.socialnetwork.exception.ResourceAlreadyExistsException;
import vn.hactanco.socialnetwork.exception.ResourceNotFoundException;
import vn.hactanco.socialnetwork.model.Role;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.RoleRepository;
import vn.hactanco.socialnetwork.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	public void register(RegisterRequest request) {
		if (!request.getPassword().equals(request.getConfirmPassword())) {
			throw new ResourceNotFoundException("Mật khẩu xác nhận không khớp");
		}
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new ResourceAlreadyExistsException("Email " + request.getEmail() + " đã tồn tại");
		}

		Role role = roleRepository.findByName("USER")
				.orElseThrow(() -> new ResourceNotFoundException("Role USER không tồn tại"));

		User user = new User();

		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(request.getPassword());
		user.setAvatar("/assets/images/default-avatar.jpg");
		user.setBio("");
		user.setIsActive(true);
		user.setCreatedAt(Instant.now());
		user.setRole(role);

		userRepository.save(user);
	}

	public User login(LoginRequest request) {

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("Email hoặc mật khẩu không đúng"));

		if (!user.getPassword().equals(request.getPassword())) {
			throw new ResourceNotFoundException("Email hoặc mật khẩu không đúng");
		}

		return user;
	}
}