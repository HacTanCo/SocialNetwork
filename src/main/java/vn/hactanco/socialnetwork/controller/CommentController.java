package vn.hactanco.socialnetwork.controller;

import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.CommentResponseDTO;
import vn.hactanco.socialnetwork.dto.ReplyResponseDTO;
import vn.hactanco.socialnetwork.helper.TinhThoiGian;
import vn.hactanco.socialnetwork.model.Comment;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.service.CommentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

	private final CommentService commentService;
	private final SimpMessagingTemplate messagingTemplate;

	// @GetMapping("/post/{postId}")
	// public List<Map<String, Object>> getComments(@PathVariable Long postId,
	// HttpSession session) {
	// User user = (User) session.getAttribute("USER");
	// List<Map<String, Object>> comments =
	// commentService.getCommentsByPostId(postId);
	// if (user != null) {
	// comments.forEach(c -> c.put("isOwner",
	// c.get("userId").equals(user.getId())));
	// }
	// return comments;
	// }

	// @PostMapping("/create")
	// public Map<String, Object> createComment(@RequestBody Map<String, Object>
	// body, HttpSession session) {
	// User user = (User) session.getAttribute("USER");
	// if (user == null)
	// return Map.of("success", false, "message", "Bạn chưa đăng nhập");
	//
	// Long postId = Long.valueOf(body.get("postId").toString());
	// String content = body.get("content").toString();
	//
	// Map<String, Object> commentMap = commentService.createComment(postId,
	// content, user);
	// return Map.of("success", true, "comment", commentMap);
	// }
	@PostMapping("/create")
	public Map<String, Object> createComment(@RequestBody Map<String, Object> body, HttpSession session) {

		User user = (User) session.getAttribute("USER");
		if (user == null) {
			return Map.of("success", false);
		}

		Long postId = Long.valueOf(body.get("postId").toString());
		String content = body.get("content").toString();

		Comment comment = commentService.createComment(postId, content, user);

		CommentResponseDTO dto = CommentResponseDTO.builder().id(comment.getId()).content(comment.getContent())
				.userName(user.getName()).userAvatar(user.getAvatar()).userId(user.getId())
				.timeAgo(TinhThoiGian.timeAgo(comment.getCreatedAt())).isOwner(true).replies(List.of()).build();

		// 🔥 gửi realtime cho tất cả client đang xem post này
		messagingTemplate.convertAndSend("/topic/comments/" + postId, Map.of("type", "COMMENT", "data", dto));

		long newCount = commentService.countByPostId(postId);

		return Map.of("success", true, "commentCount", newCount);
	}

	@GetMapping("/post/{postId}")
	public List<CommentResponseDTO> getComments(@PathVariable Long postId, HttpSession session) {

		User user = (User) session.getAttribute("USER");
		Long userId = user != null ? user.getId() : null;

		return commentService.getComments(postId, userId);
	}

	@PostMapping("/reply")
	public Map<String, Object> replyComment(@RequestBody Map<String, Object> body, HttpSession session) {

		User user = (User) session.getAttribute("USER");
		if (user == null) {
			return Map.of("success", false, "message", "Chưa đăng nhập");
		}

		Long postId = Long.valueOf(body.get("postId").toString());
		Long parentId = Long.valueOf(body.get("commentId").toString());
		String content = body.get("content").toString();

		// 🔥 tạo reply
		Comment reply = commentService.replyComment(postId, parentId, content, user);

		// 🔥 build DTO để gửi realtime
		ReplyResponseDTO dto = ReplyResponseDTO.builder().id(reply.getId()).content(reply.getContent())
				.userName(user.getName()).userAvatar(user.getAvatar()).userId(user.getId())
				.timeAgo(TinhThoiGian.timeAgo(reply.getCreatedAt())).isOwner(true).build();

		// 🔥 gửi realtime (kèm parentId để FE biết chèn vào đâu)
		messagingTemplate.convertAndSend("/topic/comments/" + postId,
				Map.of("type", "REPLY", "parentId", parentId, "data", dto));

		long newCount = commentService.countByPostId(postId);

		return Map.of("success", true, "commentCount", newCount);
	}

	@PostMapping("/update")
	public Map<String, Object> updateComment(@RequestBody Map<String, Object> body, HttpSession session) {

		User user = (User) session.getAttribute("USER");
		if (user == null) {
			return Map.of("success", false);
		}

		Long commentId = Long.valueOf(body.get("commentId").toString());
		String content = body.get("content").toString();

		boolean updated = commentService.updateComment(commentId, content, user);

		return Map.of("success", updated);
	}

	@PostMapping("/delete")
	public Map<String, Object> deleteComment(@RequestBody Map<String, Object> body, HttpSession session) {

		User user = (User) session.getAttribute("USER");
		if (user == null) {
			return Map.of("success", false);
		}

		Long commentId = Long.valueOf(body.get("commentId").toString());

		// ✅ LẤY TRƯỚC
		Long postId = commentService.getPostIdByCommentId(commentId);

		boolean deleted = commentService.deleteComment(commentId, user);

		long newCount = commentService.countByPostId(postId);
		return Map.of("success", deleted, "commentCount", newCount);
	}
	// @PostMapping("/reply")
	// public Map<String, Object> replyComment(@RequestBody Map<String, Object>
	// body, HttpSession session) {
	// User user = (User) session.getAttribute("USER");
	// if (user == null)
	// return Map.of("success", false, "message", "Bạn chưa đăng nhập");
	//
	// Long postId = Long.valueOf(body.get("postId").toString());
	// Long parentId = Long.valueOf(body.get("commentId").toString());
	// String content = body.get("content").toString();
	//
	// Map<String, Object> replyMap = commentService.replyComment(postId, parentId,
	// content, user);
	// return Map.of("success", true, "comment", replyMap);
	// }

	// Sửa comment
	// @PostMapping("/update")
	// public Map<String, Object> updateComment(@RequestBody Map<String, Object>
	// body, HttpSession session) {
	// User user = (User) session.getAttribute("USER");
	// if (user == null)
	// return Map.of("success", false, "message", "Bạn chưa đăng nhập");
	//
	// Long commentId = Long.valueOf(body.get("commentId").toString());
	// String newContent = body.get("content").toString();
	//
	// return commentService.updateComment(commentId, newContent, user)
	// .map(commentMap -> Map.of("success", true, "comment", commentMap))
	// .orElse(Map.of("success", false, "message", "Không thể sửa comment"));
	// }

	// Xóa comment
	// @PostMapping("/delete")
	// public Map<String, Object> deleteComment(@RequestBody Map<String, Object>
	// body, HttpSession session) {
	// User user = (User) session.getAttribute("USER");
	// if (user == null)
	// return Map.of("success", false, "message", "Bạn chưa đăng nhập");
	//
	// Long commentId = Long.valueOf(body.get("commentId").toString());
	// boolean deleted = commentService.deleteComment(commentId, user);
	//
	// if (deleted)
	// return Map.of("success", true);
	// else
	// return Map.of("success", false, "message", "Không thể xóa comment");
	// }
}