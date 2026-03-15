package vn.hactanco.socialnetwork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.hactanco.socialnetwork.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
	@Query("""
			    SELECT DISTINCT p
			    FROM Post p
			    LEFT JOIN FETCH p.user
			    LEFT JOIN FETCH p.medias
			    ORDER BY p.createdAt DESC
			""")
	List<Post> findFeedPosts();
}