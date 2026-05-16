package vn.hactanco.socialnetwork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.hactanco.socialnetwork.model.CallRecording;

@Repository
public interface CallRecordingRepository extends JpaRepository<CallRecording, Long> {

    @Query("SELECT r FROM CallRecording r WHERE r.caller.id = :userId OR r.receiver.id = :userId ORDER BY r.createdAt DESC")
    List<CallRecording> findByUserId(@Param("userId") Long userId);
}
