package vn.hactanco.socialnetwork.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.dto.UserSuggestionResponseDTO;
import vn.hactanco.socialnetwork.enums.FriendshipStatus;
import vn.hactanco.socialnetwork.model.Friendship;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.repository.FriendshipRepository;

@Service
@RequiredArgsConstructor
public class FriendshipService {
	private final FriendshipRepository friendshipRepository;

	public List<UserSuggestionResponseDTO> getSuggestions(Long userId) {
		List<UserSuggestionResponseDTO> results = friendshipRepository.getUserSuggestions(userId);
		return results;
	}

	public void sendFriendRequest(Long userId, Long targetId) {
		// 1. Nếu đã tồn tại cùng chiều → bỏ qua
		if (friendshipRepository.findByFollowerIdAndFollowingId(userId, targetId).isPresent()) {
			return;
		}

		// 2. Nếu tồn tại ngược chiều → accept luôn
		var reverse = friendshipRepository.findByFollowerIdAndFollowingId(targetId, userId);

		if (reverse.isPresent()) {
			Friendship fs = reverse.get();
			fs.setStatus(FriendshipStatus.ACCEPTED);
			friendshipRepository.save(fs);
			return;
		}

		// 3. Tạo request mới
		Friendship fs = new Friendship();
		fs.setFollower(new User(userId));
		fs.setFollowing(new User(targetId));
		fs.setStatus(FriendshipStatus.PENDING);

		friendshipRepository.save(fs);
	}

	public void acceptFriend(Long userId, Long targetId) {
		Friendship fs = friendshipRepository.findByFollowerIdAndFollowingId(targetId, userId).orElseThrow();

		fs.setStatus(FriendshipStatus.ACCEPTED);
		friendshipRepository.save(fs);
	}

	public void rejectFriend(Long userId, Long targetId) {
		Friendship fs = friendshipRepository.findByFollowerIdAndFollowingId(targetId, userId).orElseThrow();

		fs.setStatus(FriendshipStatus.REJECTED);
		friendshipRepository.save(fs);
	}

	public void removeFriend(Long userId, Long targetId) {
		friendshipRepository.deleteFriend(userId, targetId);
	}

	public boolean isFriend(Long userId, Long targetId) {
		return friendshipRepository.isFriend(userId, targetId);
	}

	public boolean exists(Long userId, Long targetId) {
		return friendshipRepository.exists(userId, targetId);
	}

	public List<User> getPendingRequests(Long userId) {
		return friendshipRepository.getPendingRequests(userId);
	}

	public List<User> getFriends(Long userId) {
		return friendshipRepository.getFriends(userId);
	}

	public List<User> searchFriends(Long userId, String keyword) {
		String kw = "%" + keyword + "%";

		List<User> list1 = friendshipRepository.searchFriendsFromFollower(userId, kw);
		List<User> list2 = friendshipRepository.searchFriendsFromFollowing(userId, kw);

		list1.addAll(list2);
		return list1;
	}

	public List<UserSuggestionResponseDTO> searchSuggestions(Long userId, String keyword) {
		if (keyword != null && !keyword.trim().isEmpty()) {
			return friendshipRepository.searchSuggestions(userId, keyword);
		}
		return friendshipRepository.getUserSuggestions(userId);
	}

	public List<User> searchPending(Long userId, String keyword) {
		return friendshipRepository.searchPending(userId, "%" + keyword + "%");
	}
}
