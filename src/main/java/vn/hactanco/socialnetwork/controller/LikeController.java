package vn.hactanco.socialnetwork.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.model.Post;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.service.LikeService;
import vn.hactanco.socialnetwork.service.PostService;
import vn.hactanco.socialnetwork.service.UserService;

@RestController
@RequiredArgsConstructor
public class LikeController {

	private final LikeService likeService;
	private final UserService userService;
	private final PostService postService;

	@PostMapping("/post/like/{postId}")
	public Map<String, Object> likePost(@PathVariable Long postId, Authentication authentication) {

		User user = userService.findUserByEmail(authentication.getName());
		Post post = postService.findById(postId);

		boolean liked = likeService.toggleLike(user, post);
		long likeCount = likeService.countLike(post);

		Map<String, Object> res = new HashMap<>();
		res.put("liked", liked);
		res.put("likeCount", likeCount);

		return res;
	}
}
