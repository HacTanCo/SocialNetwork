package vn.hactanco.socialnetwork.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hactanco.socialnetwork.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
	Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}