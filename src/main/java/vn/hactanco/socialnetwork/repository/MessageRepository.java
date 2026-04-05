package vn.hactanco.socialnetwork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.hactanco.socialnetwork.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

	@Query("""
			    SELECT m FROM Message m
			    WHERE
			        (m.sender.id = :userId AND m.receiver.id = :friendId)
			        OR
			        (m.sender.id = :friendId AND m.receiver.id = :userId)
			    ORDER BY m.createdAt ASC
			""")
	List<Message> getChat(@Param("userId") Long userId, @Param("friendId") Long friendId);
}