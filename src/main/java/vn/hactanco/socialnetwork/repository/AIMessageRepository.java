package vn.hactanco.socialnetwork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hactanco.socialnetwork.model.AIMessage;

@Repository
public interface AIMessageRepository extends JpaRepository<AIMessage, Long> {

	List<AIMessage> findByUser_IdOrderByCreatedAtAsc(Long userId);

}
