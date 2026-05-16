package vn.hactanco.socialnetwork.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.model.CallRecording;
import vn.hactanco.socialnetwork.repository.CallRecordingRepository;
import vn.hactanco.socialnetwork.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CallRecordingService {

    private final CallRecordingRepository callRecordingRepository;
    private final UserRepository userRepository;

    public CallRecording save(Long callerId, Long receiverId, String fileName,
                               String fileUrl, Long fileSize, Integer duration) {
        CallRecording rec = CallRecording.builder()
                .caller(userRepository.findById(callerId).orElseThrow())
                .receiver(userRepository.findById(receiverId).orElseThrow())
                .fileName(fileName)
                .fileUrl(fileUrl)
                .fileSize(fileSize)
                .duration(duration)
                .build();
        return callRecordingRepository.save(rec);
    }

    /**
     * Trả về DTO đơn giản để tránh vòng lặp serialize (lazy loading).
     */
    public List<RecordingDTO> getByUser(Long userId) {
        return callRecordingRepository.findByUserId(userId)
                .stream()
                .map(r -> new RecordingDTO(
                        r.getId(),
                        r.getCaller().getId(),
                        r.getCaller().getName(),
                        r.getReceiver().getId(),
                        r.getReceiver().getName(),
                        r.getFileUrl(),
                        r.getDuration(),
                        r.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // DTO nhỏ gọn trả về client
    public record RecordingDTO(
        Long id,
        Long callerId,
        String callerName,
        Long receiverId,
        String receiverName,
        String fileUrl,
        Integer duration,
        java.time.Instant createdAt
    ) {}
}
