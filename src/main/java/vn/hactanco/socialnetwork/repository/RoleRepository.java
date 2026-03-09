package vn.hactanco.socialnetwork.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hactanco.socialnetwork.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, long id);

	Optional<Role> findByName(String name);

}