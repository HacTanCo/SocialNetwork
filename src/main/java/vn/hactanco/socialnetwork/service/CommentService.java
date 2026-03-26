package vn.hactanco.socialnetwork.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.CommentResponseDTO;
import vn.hactanco.socialnetwork.dto.ReplyResponseDTO;
import vn.hactanco.socialnetwork.helper.TinhThoiGian;
import vn.hactanco.socialnetwork.model.Comment;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.CommentRepository;
import vn.hactanco.socialnetwork.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	private final PostRepository postRepository;

	public Comment createComment(Long postId, String content, User user) {

		Comment comment = Comment.builder().content(content).post(postRepository.getReferenceById(postId)).user(user)
				.build();

		return commentRepository.save(comment);
	}

	public List<CommentResponseDTO> getComments(Long postId, Long currentUserId) {

		List<Comment> comments = commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtDesc(postId);

		return comments.stream().map(c -> {

			List<ReplyResponseDTO> replies = c.getReplies().stream()
					.map(r -> ReplyResponseDTO.builder().id(r.getId()).content(r.getContent())
							.userName(r.getUser().getName()).userAvatar(r.getUser().getAvatar())
							.timeAgo(TinhThoiGian.timeAgo(r.getCreatedAt()))
							.isOwner(r.getUser().getId().equals(currentUserId)).build())
					.toList();

			return CommentResponseDTO.builder().id(c.getId()).content(c.getContent()).userName(c.getUser().getName())
					.userAvatar(c.getUser().getAvatar()).timeAgo(TinhThoiGian.timeAgo(c.getCreatedAt()))
					.isOwner(c.getUser().getId().equals(currentUserId)).replies(replies).build();

		}).toList();
	}

	public void replyComment(Long postId, Long parentId, String content, User user) {

		Comment parent = commentRepository.findById(parentId)
				.orElseThrow(() -> new RuntimeException("Comment không tồn tại"));

		Comment reply = Comment.builder().content(content).post(postRepository.getReferenceById(postId)).user(user)
				.parent(parent) // 🔥 QUAN TRỌNG
				.build();

		commentRepository.save(reply);
	}

	public boolean updateComment(Long commentId, String content, User user) {
		Optional<Comment> optional = commentRepository.findById(commentId);

		if (optional.isPresent()) {
			Comment c = optional.get();

			// chỉ cho sửa của mình
			if (c.getUser().getId().equals(user.getId())) {
				c.setContent(content);
				commentRepository.save(c);
				return true;
			}
		}

		return false;
	}

	public boolean deleteComment(Long commentId, User user) {
		Optional<Comment> optional = commentRepository.findById(commentId);

		if (optional.isPresent()) {
			Comment c = optional.get();

			// chỉ cho xóa của mình
			if (c.getUser().getId().equals(user.getId())) {
				commentRepository.delete(c);
				return true;
			}
		}

		return false;
	}

	public long countByPostId(Long postId) {
		return commentRepository.countByPost_Id(postId);
	}

	public Long getPostIdByCommentId(Long commentId) {
		return commentRepository.findById(commentId).map(c -> c.getPost().getId()).orElse(null);
	}
//	private final CommentRepository commentRepository;
//	private final PostRepository postRepository;
//
//	/**
//	 * Lấy danh sách comment theo postId (parent = null)
//	 */
//
//	public List<Map<String, Object>> getCommentsByPostId(Long postId) {
//		List<Comment> comments = commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtDesc(postId);
//		return comments.stream().map(this::commentToMap).collect(Collectors.toList());
//	}
//
//	/**
//	 * Tạo comment mới
//	 */
//	public Map<String, Object> createComment(Long postId, String content, User user) {
//		Comment comment = Comment.builder().content(content).post(postRepository.getReferenceById(postId)).user(user)
//				.build();
//
//		commentRepository.save(comment);
//
//		return commentToMap(comment);
//	}
//
//	/**
//	 * Reply comment
//	 */
//	public Map<String, Object> replyComment(Long postId, Long parentId, String content, User user) {
//		Comment parent = commentRepository.getReferenceById(parentId);
//		Comment reply = Comment.builder().content(content).post(postRepository.getReferenceById(postId)).user(user)
//				.parent(parent).build();
//
//		commentRepository.save(reply);
//
//		return commentToMap(reply);
//	}
//
//	public Optional<Map<String, Object>> updateComment(Long commentId, String newContent, User user) {
//		Optional<Comment> optional = commentRepository.findById(commentId);
//		if (optional.isPresent()) {
//			Comment comment = optional.get();
//			// Chỉ cho phép sửa comment của chính mình
//			if (comment.getUser().getId().equals(user.getId())) {
//				comment.setContent(newContent);
//				commentRepository.save(comment);
//				return Optional.of(commentToMap(comment));
//			}
//		}
//		return Optional.empty();
//	}
//
//	/**
//	 * Xóa comment
//	 */
//	public boolean deleteComment(Long commentId, User user) {
//		Optional<Comment> optional = commentRepository.findById(commentId);
//		if (optional.isPresent()) {
//			Comment comment = optional.get();
//			// Chỉ cho phép xóa comment của chính mình
//			if (comment.getUser().getId().equals(user.getId())) {
//				commentRepository.delete(comment);
//				return true;
//			}
//		}
//		return false;
//	}
//
//	/**
//	 * Chuyển Comment -> Map JSON
//	 */
//	private Map<String, Object> commentToMap(Comment comment) {
//		Map<String, Object> map = new HashMap<>();
//		map.put("userId", comment.getUser().getId());
//		map.put("commentId", comment.getId());
//		map.put("content", comment.getContent());
//		map.put("userName", comment.getUser().getName());
//		map.put("userAvatar", comment.getUser().getAvatar());
//		map.put("timeAgo", TinhThoiGian.timeAgo(comment.getCreatedAt()));
//
//		// reply 1 cấp
//		List<Comment> replies = comment.getReplies() != null ? comment.getReplies() : new ArrayList<>();
//		List<Map<String, Object>> replyList = replies.stream().map(r -> {
//			Map<String, Object> rm = new HashMap<>();
//			rm.put("commentId", r.getId());
//			rm.put("content", r.getContent());
//			rm.put("userName", r.getUser().getName());
//			rm.put("userAvatar", r.getUser().getAvatar());
//			rm.put("timeAgo", TinhThoiGian.timeAgo(r.getCreatedAt()));
//			return rm;
//		}).collect(Collectors.toList());
//
//		map.put("replies", replyList);
//
//		return map;
//	}
}