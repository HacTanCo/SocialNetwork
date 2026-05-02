package vn.hactanco.socialnetwork.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.hactanco.socialnetwork.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	@Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.email = :email")
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	// Admin: phân trang tất cả user (join fetch role)
	@Query("SELECT u FROM User u JOIN FETCH u.role ORDER BY u.createdAt DESC")
	List<User> findAllWithRole();

	// Admin: tìm kiếm user theo name hoặc email
	@Query("SELECT u FROM User u JOIN FETCH u.role WHERE LOWER(u.name) LIKE LOWER(:keyword) OR LOWER(u.email) LIKE LOWER(:keyword) ORDER BY u.createdAt DESC")
	List<User> searchUsersWithRole(String keyword);

	// Admin: đếm user đăng ký theo tháng (6 tháng gần nhất)
	@Query(value = "SELECT MONTH(created_at) as m, YEAR(created_at) as y, COUNT(*) as cnt FROM users WHERE created_at >= DATEADD(MONTH, -6, GETDATE()) GROUP BY MONTH(created_at), YEAR(created_at) ORDER BY y, m", nativeQuery = true)
	List<Object[]> countNewUsersPerMonth();
}