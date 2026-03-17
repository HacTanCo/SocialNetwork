package vn.hactanco.socialnetwork.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.hactanco.socialnetwork.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
	@Query("""
			    SELECT p.id FROM Post p
			    ORDER BY p.createdAt DESC
			""")
	Page<Long> findPostIds(Pageable pageable);

	@Query("""
			    SELECT DISTINCT p FROM Post p
			    LEFT JOIN FETCH p.user
			    LEFT JOIN FETCH p.medias
			    WHERE p.id IN :ids
			""")
	List<Post> findByIdInWithUserAndMedia(List<Long> ids);
}