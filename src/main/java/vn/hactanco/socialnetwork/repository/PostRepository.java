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

	@Query("""
			    SELECT p.id
			    FROM Post p
			    WHERE p.user.id = :userId
			    ORDER BY p.createdAt DESC
			""")
	Page<Long> findPostIdsByUser(Long userId, Pageable pageable);

	long countByUser_Id(Long userId);

	// Admin: tất cả post phân trang
	Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

	// Admin: tìm kiếm post theo content hoặc user name
	@Query("SELECT p FROM Post p JOIN FETCH p.user WHERE LOWER(p.content) LIKE LOWER(:keyword) OR LOWER(p.user.name) LIKE LOWER(:keyword) ORDER BY p.createdAt DESC")
	List<Post> searchPostsAdmin(String keyword);

	// Admin: đếm post theo tháng (6 tháng gần nhất)
	@Query(value = "SELECT MONTH(created_at) as m, YEAR(created_at) as y, COUNT(*) as cnt FROM posts WHERE created_at >= DATEADD(MONTH, -6, GETDATE()) GROUP BY MONTH(created_at), YEAR(created_at) ORDER BY y, m", nativeQuery = true)
	List<Object[]> countPostsPerMonth();

	// Admin: lấy 5 post mới nhất
	List<Post> findTop5ByOrderByCreatedAtDesc();
}