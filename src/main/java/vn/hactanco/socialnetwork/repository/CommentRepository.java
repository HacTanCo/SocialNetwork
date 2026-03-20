package vn.hactanco.socialnetwork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hactanco.socialnetwork.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtDesc(Long postId);
}