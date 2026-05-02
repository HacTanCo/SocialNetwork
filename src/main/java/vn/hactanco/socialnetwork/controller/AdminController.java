package vn.hactanco.socialnetwork.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.model.Comment;
import vn.hactanco.socialnetwork.model.Post;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.LikeRepository;
import vn.hactanco.socialnetwork.repository.PostRepository;
import vn.hactanco.socialnetwork.repository.RoleRepository;
import vn.hactanco.socialnetwork.repository.UserRepository;
import vn.hactanco.socialnetwork.service.CommentService;
import vn.hactanco.socialnetwork.service.PostService;
import vn.hactanco.socialnetwork.service.RoleService;
import vn.hactanco.socialnetwork.service.UserService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

	private final UserService userService;
	private final PostService postService;
	private final CommentService commentService;
	private final RoleService roleService;
	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final LikeRepository likeRepository;
	private final RoleRepository roleRepository;

	// ==================== DASHBOARD ====================

	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		// Thống kê tổng quan
		long totalUsers = userService.countAll();
		long totalPosts = postService.countAll();
		long totalComments = commentService.countAll();
		long totalLikes = likeRepository.count();

		model.addAttribute("totalUsers", totalUsers);
		model.addAttribute("totalPosts", totalPosts);
		model.addAttribute("totalComments", totalComments);
		model.addAttribute("totalLikes", totalLikes);

		// Biểu đồ: user và post theo tháng
		model.addAttribute("usersByMonth", userRepository.countNewUsersPerMonth());
		model.addAttribute("postsByMonth", postRepository.countPostsPerMonth());

		// Danh sách mới nhất
		model.addAttribute("recentUsers", userRepository.findAllWithRole().stream().limit(5).toList());
		model.addAttribute("recentPosts", postService.getRecentPosts());

		return "admin/dashboard";
	}

	// API trả JSON cho Chart.js
	@GetMapping("/api/stats/users-by-month")
	@ResponseBody
	public List<Object[]> getUsersByMonth() {
		return userRepository.countNewUsersPerMonth();
	}

	@GetMapping("/api/stats/posts-by-month")
	@ResponseBody
	public List<Object[]> getPostsByMonth() {
		return postRepository.countPostsPerMonth();
	}

	// ==================== QUẢN LÝ USER ====================

	@GetMapping("/users")
	public String users(Model model,
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") int page,
			org.springframework.security.core.Authentication authentication) {

		List<User> users;
		if (keyword != null && !keyword.trim().isEmpty()) {
			users = userService.searchUsers(keyword.trim());
			model.addAttribute("keyword", keyword);
		} else {
			users = userService.getAllUsersWithRole();
		}

		// Phân trang thủ công
		int size = 10;
		int start = page * size;
		int end = Math.min(start + size, users.size());
		List<User> pagedUsers = start < users.size() ? users.subList(start, end) : List.of();
		int totalPages = (int) Math.ceil((double) users.size() / size);

		model.addAttribute("users", pagedUsers);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("roles", roleService.getAllRoles());

		// Truyền ID của admin hiện tại để disable role selector cho chính mình
		User currentUser = userService.findUserByEmail(authentication.getName());
		if (currentUser != null) {
			model.addAttribute("currentUserId", currentUser.getId());
		}

		return "admin/users";
	}

	@PostMapping("/users/{id}/toggle-active")
	public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		userService.toggleActive(id);
		redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
		return "redirect:/admin/users";
	}

	@PostMapping("/users/{id}/change-role")
	public String changeRole(@PathVariable Long id, @RequestParam Long roleId,
			RedirectAttributes redirectAttributes, org.springframework.security.core.Authentication authentication) {
		// Không cho phép admin tự đổi role của chính mình
		String currentEmail = authentication.getName();
		User currentUser = userService.findUserByEmail(currentEmail);
		if (currentUser != null && currentUser.getId().equals(id)) {
			redirectAttributes.addFlashAttribute("error", "Không thể thay đổi role của chính mình!");
			return "redirect:/admin/users";
		}
		userService.changeRole(id, roleId, roleRepository);
		redirectAttributes.addFlashAttribute("success", "Đổi role thành công!");
		return "redirect:/admin/users";
	}

	@PostMapping("/users/{id}/delete")
	public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		userService.deleteUser(id);
		redirectAttributes.addFlashAttribute("success", "Xóa user thành công!");
		return "redirect:/admin/users";
	}

	// ==================== QUẢN LÝ POST ====================

	@GetMapping("/posts")
	public String posts(Model model,
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") int page) {

		if (keyword != null && !keyword.trim().isEmpty()) {
			List<Post> posts = postService.searchPostsAdmin(keyword.trim());
			// Phân trang thủ công
			int size = 10;
			int start = page * size;
			int end = Math.min(start + size, posts.size());
			List<Post> pagedPosts = start < posts.size() ? posts.subList(start, end) : List.of();
			int totalPages = (int) Math.ceil((double) posts.size() / size);

			model.addAttribute("posts", pagedPosts);
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", totalPages);
			model.addAttribute("keyword", keyword);
		} else {
			Page<Post> postPage = postService.findAllPaged(page, 10);
			model.addAttribute("posts", postPage.getContent());
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", postPage.getTotalPages());
		}

		return "admin/posts";
	}

	@PostMapping("/posts/{id}/delete")
	public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			postService.adminDeletePost(id);
			redirectAttributes.addFlashAttribute("success", "Xóa bài viết thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Xóa bài viết thất bại: " + e.getMessage());
		}
		return "redirect:/admin/posts";
	}

	@GetMapping("/posts/{id}/detail")
	@ResponseBody
	public Map<String, Object> postDetail(@PathVariable Long id) {
		Post post = postService.findById(id);
		Map<String, Object> result = new HashMap<>();
		result.put("id", post.getId());
		result.put("content", post.getContent());
		result.put("timeAgo",
				post.getCreatedAt() != null
						? vn.hactanco.socialnetwork.helper.TinhThoiGian.timeAgo(post.getCreatedAt())
						: "N/A");
		if (post.getUser() != null) {
			result.put("userName", post.getUser().getName());
			result.put("userAvatar", post.getUser().getAvatar());
		}
		if (post.getMedias() != null) {
			result.put("medias", post.getMedias().stream().map(m -> {
				Map<String, String> media = new HashMap<>();
				media.put("url", m.getMediaUrl());
				media.put("type", m.getMediaType().name());
				return media;
			}).collect(Collectors.toList()));
		}
		return result;
	}

	// ==================== QUẢN LÝ COMMENT ====================

	@GetMapping("/comments")
	public String comments(Model model, @RequestParam(defaultValue = "0") int page) {

		List<Comment> allComments = commentService.findAllWithUserAndPost();

		// Phân trang thủ công
		int size = 10;
		int start = page * size;
		int end = Math.min(start + size, allComments.size());
		List<Comment> pagedComments = start < allComments.size() ? allComments.subList(start, end) : List.of();
		int totalPages = (int) Math.ceil((double) allComments.size() / size);

		model.addAttribute("comments", pagedComments);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);

		return "admin/comments";
	}

	@PostMapping("/comments/{id}/delete")
	public String deleteComment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		commentService.adminDeleteComment(id);
		redirectAttributes.addFlashAttribute("success", "Xóa comment thành công!");
		return "redirect:/admin/comments";
	}
}
