package vn.hactanco.socialnetwork.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.hactanco.socialnetwork.model.Like;
import vn.hactanco.socialnetwork.model.Post;
import vn.hactanco.socialnetwork.model.User;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

	Optional<Like> findByUserAndPost(User user, Post post);

	long countByPost(Post post);

	@Query("""
			    SELECT l.post.id, COUNT(l)
			    FROM Like l
			    WHERE l.post.id IN :postIds
			    GROUP BY l.post.id
			""")
	List<Object[]> countLikesByPostIds(List<Long> postIds);

	@Query("""
			    SELECT l.post.id
			    FROM Like l
			    WHERE l.user.id = :userId AND l.post.id IN :postIds
			""")
	List<Long> findLikedPostIds(Long userId, List<Long> postIds);
}
