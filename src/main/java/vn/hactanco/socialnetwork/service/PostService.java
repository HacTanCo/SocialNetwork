package vn.hactanco.socialnetwork.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.enums.MediaType;
import vn.hactanco.socialnetwork.exception.ResourceNotFoundException;
import vn.hactanco.socialnetwork.model.Post;
import vn.hactanco.socialnetwork.model.PostMedia;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostRepository postRepository;

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

	public List<Post> getAllPosts() {
		return postRepository.findFeedPosts();
	}
}