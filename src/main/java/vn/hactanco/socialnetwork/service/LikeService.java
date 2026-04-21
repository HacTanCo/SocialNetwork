package vn.hactanco.socialnetwork.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.model.Like;
import vn.hactanco.socialnetwork.model.Notification;
import vn.hactanco.socialnetwork.model.Post;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.LikeRepository;
import vn.hactanco.socialnetwork.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class LikeService {

	private final LikeRepository likeRepository;
	private final NotificationRepository notificationRepository;

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
				Notification n = Notification.builder().sender(user).receiver(post.getUser()).post(post).type("LIKE")
						.content(user.getName() + " đã thích bài viết của bạn").build();

				notificationRepository.save(n);
			}
			return true;
		});
	}

	public long countLike(Post post) {
		return likeRepository.countByPost(post);
	}
}
