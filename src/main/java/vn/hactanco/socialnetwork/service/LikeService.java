package vn.hactanco.socialnetwork.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.model.Like;
import vn.hactanco.socialnetwork.model.Notification;
import vn.hactanco.socialnetwork.model.Post;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.LikeRepository;

@Service
@RequiredArgsConstructor
public class LikeService {

	private final LikeRepository likeRepository;
	private final NotificationService notificationService;

	public boolean toggleLike(User user, Post post) {
		return likeRepository.findByUserAndPost(user, post).map(like -> {
			likeRepository.delete(like);
			return false;
		}).orElseGet(() -> {
			Like like = new Like();
			like.setUser(user);
			like.setPost(post);
			likeRepository.save(like);

			// TẠO NOTIFICATION
			if (!post.getUser().getId().equals(user.getId())) {
				notificationService.createNotification(user.getId(), post.getUser().getId(),
						user.getName() + " đã thích bài viết của bạn", "LIKE", post.getId());
			}
			return true;
		});
	}

	public long countLike(Post post) {
		return likeRepository.countByPost(post);
	}
}
