package vn.hactanco.socialnetwork.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import vn.hactanco.socialnetwork.dto.UserSuggestionResponseDTO;
import vn.hactanco.socialnetwork.model.Friendship;
import vn.hactanco.socialnetwork.model.User;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

	@Query(value = """
			    SELECT TOP 5 u.id, u.avatar, u.name
			    FROM users u
			    WHERE u.id != :userId

			    AND u.role_id != 1

			    AND u.id NOT IN (
			        SELECT
			            CASE
			                WHEN f.follower_id = :userId THEN f.following_id
			                ELSE f.follower_id
			            END
			        FROM friendships f
			        WHERE (f.follower_id = :userId OR f.following_id = :userId)
			    )
			""", nativeQuery = true)
	List<UserSuggestionResponseDTO> getUserSuggestions(Long userId);

	Optional<Friendship> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

	@Modifying
	@Transactional
	@Query("""
			    DELETE FROM Friendship f
			    WHERE
			        (f.follower.id = :userId AND f.following.id = :targetId)
			        OR
			        (f.follower.id = :targetId AND f.following.id = :userId)
			""")
	void deleteFriend(Long userId, Long targetId);

	@Query("""
			    SELECT COUNT(f) > 0
			    FROM Friendship f
			    WHERE
			        (
			            (f.follower.id = :userId AND f.following.id = :targetId)
			            OR
			            (f.follower.id = :targetId AND f.following.id = :userId)
			        )
			        AND f.status = 'ACCEPTED'
			""")
	boolean isFriend(Long userId, Long targetId);

	@Query("""
			    SELECT COUNT(f) > 0
			    FROM Friendship f
			    WHERE
			        (f.follower.id = :userId AND f.following.id = :targetId)
			        OR
			        (f.follower.id = :targetId AND f.following.id = :userId)
			""")
	boolean exists(Long userId, Long targetId);

	@Query("""
			    SELECT f.follower
			    FROM Friendship f
			    WHERE f.following.id = :userId
			      AND f.status = 'PENDING'
			""")
	List<User> getPendingRequests(Long userId);

	@Query(value = """
			    SELECT u.*
			    FROM users u
			    WHERE u.id IN (
			        SELECT
			            CASE
			                WHEN f.follower_id = :userId THEN f.following_id
			                ELSE f.follower_id
			            END
			        FROM friendships f
			        WHERE (f.follower_id = :userId OR f.following_id = :userId)
			        AND f.status = 'ACCEPTED'
			    )
			""", nativeQuery = true)
	List<User> getFriends(Long userId);

	@Query(value = """
			    SELECT u.*
			    FROM users u
			    WHERE u.id IN (
			        SELECT
			            CASE
			                WHEN f.follower_id = :userId THEN f.following_id
			                ELSE f.follower_id
			            END
			        FROM friendships f
			        WHERE (f.follower_id = :userId OR f.following_id = :userId)
			        AND f.status = 'ACCEPTED'
			    )
			""", countQuery = """
			    SELECT COUNT(*)
			    FROM users u
			    WHERE u.id IN (
			        SELECT
			            CASE
			                WHEN f.follower_id = :userId THEN f.following_id
			                ELSE f.follower_id
			            END
			        FROM friendships f
			        WHERE (f.follower_id = :userId OR f.following_id = :userId)
			        AND f.status = 'ACCEPTED'
			    )
			""", nativeQuery = true)
	Page<User> getFriendPhanTrang(Long userId, Pageable pageable);

	@Query("""
			    SELECT f.follower
			    FROM Friendship f
			    WHERE f.following.id = :userId
			    AND f.status = 'PENDING'
			""")
	Page<User> getPendingPhanTrang(Long userId, Pageable pageable);

	@Query(value = """
			    SELECT u.id, u.avatar, u.name
			    FROM users u
			    WHERE u.id != :userId

			    AND u.role_id != 1

			    AND u.id NOT IN (
			        SELECT
			            CASE
			                WHEN f.follower_id = :userId THEN f.following_id
			                ELSE f.follower_id
			            END
			        FROM friendships f
			        WHERE (f.follower_id = :userId OR f.following_id = :userId)
			    )
			""", countQuery = """
			    SELECT COUNT(*)
			    FROM users u
			    WHERE u.id != :userId

			    AND u.role_id != 1

			    AND u.id NOT IN (
			        SELECT
			            CASE
			                WHEN f.follower_id = :userId THEN f.following_id
			                ELSE f.follower_id
			            END
			        FROM friendships f
			        WHERE (f.follower_id = :userId OR f.following_id = :userId)
			    )
			""", nativeQuery = true)
	Page<UserSuggestionResponseDTO> getSuggestionPhanTrang(Long userId, Pageable pageable);

	@Query("""
			    SELECT f.following
			    FROM Friendship f
			    WHERE f.follower.id = :userId
			      AND f.status = 'PENDING'
			""")
	Page<User> getSentPhanTrang(Long userId, Pageable pageable);

	// ---------------
	@Query("""
			    SELECT f.following
			    FROM Friendship f
			    WHERE f.follower.id = :userId
			    AND f.status = 'ACCEPTED'
			    AND LOWER(f.following.name) LIKE LOWER(:keyword)
			""")
	List<User> searchFriendsFromFollower(Long userId, String keyword);

	@Query("""
			    SELECT f.follower
			    FROM Friendship f
			    WHERE f.following.id = :userId
			    AND f.status = 'ACCEPTED'
			    AND LOWER(f.follower.name) LIKE LOWER(:keyword)
			""")
	List<User> searchFriendsFromFollowing(Long userId, String keyword);

	@Query(value = """
			    SELECT u.id, u.avatar, u.name
			    FROM users u
			    WHERE u.id != :userId

			    AND u.role_id != 1

			    AND LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))

			    AND u.id NOT IN (
			        SELECT
			            CASE
			                WHEN f.follower_id = :userId THEN f.following_id
			                ELSE f.follower_id
			            END
			        FROM friendships f
			        WHERE (f.follower_id = :userId OR f.following_id = :userId)
			    )
			""", nativeQuery = true)
	List<UserSuggestionResponseDTO> searchSuggestions(Long userId, String keyword);

	@Query("""
			    SELECT f.follower
			    FROM Friendship f
			    WHERE f.following.id = :userId
			    AND f.status = 'PENDING'
			    AND LOWER(f.follower.name) LIKE LOWER(:keyword)
			""")
	List<User> searchPending(Long userId, String keyword);

	@Query("""
			    SELECT COUNT(f)
			    FROM Friendship f
			    WHERE
			        (f.follower.id = :userId OR f.following.id = :userId)
			        AND f.status = 'ACCEPTED'
			""")
	int countFriends(Long userId);

	@Query("""
			    SELECT f.following
			    FROM Friendship f
			    WHERE f.follower.id = :userId
			      AND f.status = 'PENDING'
			""")
	List<User> getSentRequests(Long userId);

	@Query("""
			    SELECT f.following
			    FROM Friendship f
			    WHERE f.follower.id = :userId
			      AND f.status = 'PENDING'
			      AND LOWER(f.following.name) LIKE LOWER(:keyword)
			""")
	List<User> searchSent(Long userId, String keyword);

	@Query("""
			    SELECT COUNT(f) > 0
			    FROM Friendship f
			    WHERE f.follower.id = :userId
			      AND f.following.id = :targetId
			      AND f.status = 'PENDING'
			""")
	boolean isPending(Long userId, Long targetId);
}
