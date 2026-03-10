package vn.hactanco.socialnetwork.repository;

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
}