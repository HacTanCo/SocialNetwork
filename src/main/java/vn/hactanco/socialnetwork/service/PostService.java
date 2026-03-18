package vn.hactanco.socialnetwork.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.PostMediaResponseDTO;
import vn.hactanco.socialnetwork.dto.PostResponseDTO;
import vn.hactanco.socialnetwork.enums.MediaType;
import vn.hactanco.socialnetwork.exception.ResourceNotFoundException;
import vn.hactanco.socialnetwork.model.Post;
import vn.hactanco.socialnetwork.model.PostMedia;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.LikeRepository;
import vn.hactanco.socialnetwork.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;
	private final LikeRepository likeRepository;
	@Value("${file.upload-dir}")
	private String uploadDir;

	public void createPost(String content, MultipartFile[] files, User user) throws IOException {

		boolean hasContent = content != null && !content.trim().isEmpty();
		boolean hasMedia = false;

		if (files != null) {
			for (MultipartFile file : files) {
				if (!file.isEmpty()) {
					hasMedia = true;
					break;
				}
			}
		}

		// BẮT BUỘC có cả content và media
		if (!hasContent || !hasMedia) {
			throw new ResourceNotFoundException("Post phải có cả nội dung và ảnh/video");
		}

		Post post = new Post();
		post.setContent(content);
		post.setUser(user);

		List<PostMedia> medias = new ArrayList<>();

		// tạo folder nếu chưa có
		File imageDir = new File(uploadDir + "images/");
		File videoDir = new File(uploadDir + "videos/");

		if (!imageDir.exists())
			imageDir.mkdirs();
		if (!videoDir.exists())
			videoDir.mkdirs();

		if (files != null) {

			for (MultipartFile file : files) {

				if (file.isEmpty())
					continue;

				String originalName = file.getOriginalFilename();

				if (originalName == null || !originalName.contains(".")) {
					throw new ResourceNotFoundException("File không hợp lệ");
				}

				String extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();

				// danh sách extension hợp lệ
				List<String> imageExt = List.of("jpg", "jpeg", "png", "gif", "webp");
				List<String> videoExt = List.of("mp4", "mov", "avi", "webm");

				boolean isImage = imageExt.contains(extension);
				boolean isVideo = videoExt.contains(extension);

				if (!isImage && !isVideo) {
					throw new ResourceNotFoundException("Chỉ được upload ảnh hoặc video");
				}

				String fileName = UUID.randomUUID() + "_" + originalName;

				File dest;
				String mediaUrl;

				if (isImage) {

					dest = new File(imageDir, fileName);
					mediaUrl = "/uploads/images/" + fileName;

				} else {

					dest = new File(videoDir, fileName);
					mediaUrl = "/uploads/videos/" + fileName;

				}

				file.transferTo(dest);

				PostMedia media = new PostMedia();

				media.setMediaUrl(mediaUrl);

				if (isImage) {
					media.setMediaType(MediaType.IMAGE);
				} else {
					media.setMediaType(MediaType.VIDEO);
				}

				media.setPost(post);

				medias.add(media);
			}
		}

		post.setMedias(medias);

		postRepository.save(post);
	}

	private PostResponseDTO convertToDTO(Post post) {
//		private Long postId;
//		private String content;
//		private Instant createdAt;
//	
//		private Long userId;
//		private String userName;
//		private String userAvatar;
//		private List<PostMediaResponseDTO> medias;

		List<PostMediaResponseDTO> mediaDTOS = post.getMedias().stream().map(media -> PostMediaResponseDTO.builder()
				.mediaUrl(media.getMediaUrl()).mediaType(media.getMediaType()).build()).toList();

		return PostResponseDTO.builder().postId(post.getId()).content(post.getContent()).createdAt(post.getCreatedAt())
				.userId(post.getUser().getId()).userName(post.getUser().getName())
				.userAvatar(post.getUser().getAvatar()).medias(mediaDTOS).build();
	}

	public List<PostResponseDTO> getFeedDTO(int page, int size, User currentUser) {

		Pageable pageable = PageRequest.of(page, size);

		// 1. lấy id trước (tránh N+1)
		Page<Long> postIdsPage = postRepository.findPostIds(pageable);
		List<Long> ids = postIdsPage.getContent();

		// 2. fetch post + user + media
		List<Post> posts = postRepository.findByIdInWithUserAndMedia(ids);

		Map<Long, Post> map = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));

		List<Post> sortedPosts = ids.stream().map(map::get).toList();

		// 🔥 3. COUNT LIKE (1 query)
		Map<Long, Long> likeCountMap = likeRepository.countLikesByPostIds(ids).stream()
				.collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

		// 🔥 4. USER ĐÃ LIKE (1 query)
		List<Long> likedPostIds = likeRepository.findLikedPostIds(currentUser.getId(), ids);

		// 5. map DTO
		return sortedPosts.stream().map(post -> {

			PostResponseDTO dto = convertToDTO(post);
			// getOrDefault: Lấy value theo key, nếu key không tồn tại thì trả về giá trị
			// mặc định
			dto.setLikeCount(likeCountMap.getOrDefault(post.getId(), 0L));
			// contains: Kiểm tra xem phần tử có tồn tại trong List (hoặc Set) không
			dto.setLiked(likedPostIds.contains(post.getId()));

			return dto;

		}).toList();
	}

//	public Page<Post> getFeed(int page, int size) {
//
//		Pageable pageable = PageRequest.of(page, size);
//
//		return postRepository.findAllByOrderByCreatedAtDesc(pageable);
//	}

	public void deletePost(Long postId, User user) {

		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post không tồn tại"));
		if (!post.getUser().getId().equals(user.getId())) {
			throw new RuntimeException("Không có quyền xóa bài viết");
		}
		if (post.getMedias() != null) {
			for (PostMedia media : post.getMedias()) {

				String mediaPath = media.getMediaUrl();
				// ví dụ: /uploads/images/abc.jpg

				String filePath = uploadDir + mediaPath.replace("/uploads/", "");

				File file = new File(filePath);

				if (file.exists()) {
					file.delete();
				}
			}
		}
		postRepository.delete(post);
	}

	public void updatePost(Long postId, String content, User user) {

		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Post không tồn tại"));

		// kiểm tra quyền
		if (!post.getUser().getId().equals(user.getId())) {
			throw new RuntimeException("Không có quyền sửa bài viết");
		}

		// validate content
		if (content == null || content.trim().isEmpty()) {
			throw new ResourceNotFoundException("Nội dung bài viết không được để trống");
		}

		post.setContent(content.trim());

		postRepository.save(post);
	}

	public Post findById(Long id) {
		return postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post không tồn tại"));
	}
}