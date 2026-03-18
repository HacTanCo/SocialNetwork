package vn.hactanco.socialnetwork.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.model.Like;
import vn.hactanco.socialnetwork.model.Post;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.LikeRepository;

@Service
@RequiredArgsConstructor
public class LikeService {

	private final LikeRepository likeRepository;

	public boolean toggleLike(User user, Post post) {
		return likeRepository.findByUserAndPost(user, post).map(like -> {
			likeRepository.delete(like);
			return false;
		}).orElseGet(() -> {
			Like like = new Like();
			like.setUser(user);
			like.setPost(post);
			likeRepository.save(like);
			return true;
		});
	}

	public long countLike(Post post) {
		return likeRepository.countByPost(post);
	}
}
