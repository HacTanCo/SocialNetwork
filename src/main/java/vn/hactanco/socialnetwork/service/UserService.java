package vn.hactanco.socialnetwork.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.exception.ResourceNotFoundException;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	public User findUserByEmail(String email) {
		Optional<User> userOpt = this.userRepository.findByEmail(email);
		if (!userOpt.isPresent())
			return null;
		return userOpt.get();
	}

	public User getById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
	}
}
