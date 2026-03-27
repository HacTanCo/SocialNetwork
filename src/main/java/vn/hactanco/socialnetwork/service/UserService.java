package vn.hactanco.socialnetwork.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.UserProfileDTO;
import vn.hactanco.socialnetwork.exception.ResourceNotFoundException;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.PostRepository;
import vn.hactanco.socialnetwork.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final PostRepository postRepository;

	public User findUserByEmail(String email) {
		Optional<User> userOpt = this.userRepository.findByEmail(email);
		if (!userOpt.isPresent())
			return null;
		return userOpt.get();
	}

	public User getById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
	}

	public UserProfileDTO getProfileDTO(Long userId) {

		// 1. lấy user
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

		// 2. count post (🔥 dùng query riêng cho nhanh)
		long postCount = postRepository.countByUser_Id(userId);

		// 3. build DTO
		return UserProfileDTO.builder().id(user.getId()).name(user.getName()).avatar(user.getAvatar())
				.bio(user.getBio()).postCount(postCount).build();
	}

}
