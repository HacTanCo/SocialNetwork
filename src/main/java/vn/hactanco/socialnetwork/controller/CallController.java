package vn.hactanco.socialnetwork.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import vn.hactanco.socialnetwork.model.User;
import vn.hactanco.socialnetwork.service.CallRecordingService;
import vn.hactanco.socialnetwork.service.CallRecordingService.RecordingDTO;
import vn.hactanco.socialnetwork.websocket.CallMessage;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class CallController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CallRecordingService callRecordingService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * WebSocket relay — chuyển tiếp mọi signal cuộc gọi (offer, answer, ice,
     * hangup, cancel, reject, recording_started, recording_stopped).
     */
    @MessageMapping("/call")
    public void handle(CallMessage msg) {
        messagingTemplate.convertAndSend("/topic/call/" + msg.getTo(), msg);
    }

    /**
     * Upload file ghi âm sau khi kết thúc cuộc gọi.
     * Frontend gửi multipart/form-data gồm: file (webm), receiverId, duration
     * (giây).
     */
    @PostMapping("/call/recording/upload")
    @ResponseBody
    public Map<String, Object> uploadRecording(
            @RequestParam("file") MultipartFile file,
            @RequestParam("receiverId") Long receiverId,
            @RequestParam(value = "duration", defaultValue = "0") Integer duration,
            HttpSession session) throws IOException {

        User currentUser = (User) session.getAttribute("USER");
        if (currentUser == null) {
            return Map.of("error", "Chưa đăng nhập");
        }

        if (file.isEmpty()) {
            return Map.of("error", "File trống");
        }

        // Giới hạn kích thước: 200MB (5 phút webm ~50-100MB tuỳ chất lượng)
        if (file.getSize() > 200L * 1024 * 1024) {
            return Map.of("error", "File quá lớn (tối đa 200MB)");
        }

        String ext = "webm";
        String fileName = UUID.randomUUID() + "." + ext;

        File dir = new File(uploadDir + "recordings/");
        if (!dir.exists())
            dir.mkdirs();

        File dest = new File(dir, fileName);
        file.transferTo(dest);

        String fileUrl = "/uploads/recordings/" + fileName;

        // Lưu metadata vào DB
        callRecordingService.save(
                currentUser.getId(), receiverId,
                fileName, fileUrl,
                file.getSize(), duration);

        return Map.of("url", fileUrl, "fileName", fileName);
    }

    /**
     * Trả danh sách ghi âm của user hiện tại (cả caller và receiver).
     */
    @GetMapping("/call/recordings")
    @ResponseBody
    public List<RecordingDTO> getRecordings(HttpSession session) {
        User currentUser = (User) session.getAttribute("USER");
        if (currentUser == null)
            return List.of();
        return callRecordingService.getByUser(currentUser.getId());
    }
}