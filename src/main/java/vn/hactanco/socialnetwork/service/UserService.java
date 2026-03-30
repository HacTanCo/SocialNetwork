package vn.hactanco.socialnetwork.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
	@Value("${file.upload-dir}")
	private String uploadDir;

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

	public void updateProfile(User user, String name, String bio, MultipartFile avatarFile) throws IOException {

		// update name + bio
		if (name != null && !name.trim().isEmpty()) {
			user.setName(name.trim());
		}

		if (bio != null) {
			user.setBio(bio.trim());
		}

		// xử lý avatar
		if (avatarFile != null && !avatarFile.isEmpty()) {

			String originalName = avatarFile.getOriginalFilename();

			if (originalName == null || !originalName.contains(".")) {
				throw new RuntimeException("File không hợp lệ");
			}

			String extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();

			List<String> imageExt = List.of("jpg", "jpeg", "png", "gif", "webp");

			if (!imageExt.contains(extension)) {
				throw new RuntimeException("Chỉ cho phép ảnh");
			}

			// tạo folder avatars
			File avatarDir = new File(uploadDir + "avatars/");
			if (!avatarDir.exists())
				avatarDir.mkdirs();

			// 🔥 xóa avatar cũ (nếu có)
			if (user.getAvatar() != null && !user.getAvatar().contains("default-avatar")) {
				String oldPath = uploadDir + user.getAvatar().replace("/uploads/", "");
				File oldFile = new File(oldPath);
				if (oldFile.exists())
					oldFile.delete();
			}

			// lưu file mới
			String fileName = UUID.randomUUID() + "_" + originalName;
			File dest = new File(avatarDir, fileName);
			avatarFile.transferTo(dest);

			String avatarUrl = "/uploads/avatars/" + fileName;

			user.setAvatar(avatarUrl);
		}

		userRepository.save(user);
	}

}
