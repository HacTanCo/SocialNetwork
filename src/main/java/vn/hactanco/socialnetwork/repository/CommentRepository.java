package vn.hactanco.socialnetwork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.hactanco.socialnetwork.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtDesc(Long postId);

	@Query("""
			    SELECT c.post.id, COUNT(c)
			    FROM Comment c
			    WHERE c.post.id IN :postIds
			    GROUP BY c.post.id
			""")
	List<Object[]> countCommentsByPostIds(List<Long> postIds);

	long countByPost_Id(Long postId);
}