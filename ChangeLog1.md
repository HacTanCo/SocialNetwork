# Tài liệu tham khảo
1. Hỏi Dân IT: https://docs.google.com/document/d/16TK2P-WS3d_MZhSNNYKzDKLFYLvbHYQ3VwbgY6-Nk1Y/edit?tab=t.61mqx9t3bvvk
2. Spring Document
- JPA: https://docs.spring.io/spring-data/jpa/reference/jpa/getting-started.html
- Query Methods: https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
- Repository query keywords: https://docs.spring.io/spring-data/jpa/reference/repositories/query-keywords-reference.html
- Validating Form Input: https://spring.io/guides/gs/validating-form-input
# Authentication 
## Register
## Login ( có image minh họa workflow)
## Forgot password
Optional<OtpResetPassword> findTopByUserAndOtpCodeAndIsUsedFalseAndExpiredAtAfter(User user, String otpCode,
			Instant now);
same = SELECT *
        FROM otp_reset_passwords
        WHERE user_id = ?
        AND otp_code = ?
        AND is_used = false
        AND expired_at > ?
        LIMIT 1;
## Change password
## Remember me
1. cài thư viện
        <dependency>
                <groupId>org.springframework.session</groupId>
	        <artifactId>spring-session-jdbc</artifactId>
	</dependency>
2. viết SpringSessionRememberMeServices trong SecurityConfig

# Post
 ## List Post
1. page có các hàm sau
- getContent():	Lấy list dữ liệu của page
- getTotalElements(): Tổng số record trong DB
- getTotalPages(): Tổng số trang
- getNumber(): Trang hiện tại
- getSize(): Số phần tử mỗi trang

2. List<Post> findByIdInWithUserAndMedia(List<Long> ids);
select distinct posts.id,posts.content,posts.created_at,
                pm.post_id,pm.id,pm.created_at,pm.media_type,pm.media_url,
                users.*
from posts left join users on users.id=posts.user_id 
                left join post_medias pm on posts.id=pm.post_id 
where posts.id in (?,?,?,?,?,?,?,?,?,?) cái này là list id lấy từ page.getContent()

3. Map<Long, Post> map = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
Collectors.toMap(Key, Value)
map = {
  1 -> Post(id=1, title="A"),
  2 -> Post(id=2, title="B")
}

 ## Create Post
1. file.transferTo(dest);
- dùng để đọc dữ liệu từ file ( cụ thể là File dest = new File(imageDir, fileName);) đơn giản là lấy 
đường dẫn rồi tạo 1 file lưu lại trên folder upload/
- const url = URL.createObjectURL(file); Mục đích: tạo ra một URL tạm thời (blob URL) để trình duyệt có thể hiển thị file (ảnh/video) ngay lập tức mà không cần upload lên server.
 ## Delete Post
1. String filePath = uploadDir + mediaPath.replace("/uploads/", "");
vd: mediaPath = "/uploads/images/abc123.jpg"
- mediaPath.replace("/uploads/", "") => "images/abc123.jpg""
- uploadDir (uploadDir = "C:/project/uploads/") + mediaPath.replace("/uploads/", "") 
        => "C:/project/uploads/images/abc123.jpg"

 ## Update Post

# Like

# Comment 

 ## Create Comment
 ## Update Commnet
 ## List Comment, List Reply Comment

  ### FLOW Commnet with socket ( làm giống room chat)
1. Client mở WebSocket
const socket = new SockJS('/ws');
stompClient = Stomp.over(socket);

2. Subscribe
stompClient.subscribe(`/topic/comments/${postId}`, function (message) {
    const res = JSON.parse(message.body);
});

3. Gửi request commnet
POST /comment/create

4. Server xử lý
- sau khi lưu vào DB, Server đẩy message ra tất cả client đang subscribe
messagingTemplate.convertAndSend(
    "/topic/comments/" + postId,
    message
);

5. Các User đang subscribe sẽ nhận đc comment realtime
function(message) {
    const res = JSON.parse(message.body);
    appendNewComment(res.data);
}

# Friendship
 ## Page Friend
 ## Toggle Kết bạn/hủy lời mời kết bạn
 ## Send request add friend
 ## Accept request add friend
 ## Reject request add friend
 ## Remove friend
 ## Remove friend from profile page

# Profile
 ## View profile with id
 ## View profile when we hover at avatar ("/api/user/{id}")
 - const rect = target.getBoundingClientRect(); Lấy vị trí của element được click/hover
 - target = element bạn vừa click (avatar, username...)
 - getBoundingClientRect() trả về vị trí của nó trên màn hình
 - cache dữ liệu user tránh gọi nhiều lân ( gọi api thì userCache[userId] = user;)
 ## List frind at profile page ("/api/friends/{userId}")
 ## Update profile

# Messages
1. ở trong file WebSocketConfig.java sẽ có 2 chỗ cho user sư dụng và 1 nơi kết nối đến ws
- enableSimpleBroker: là nơi mà user sẽ nhận được data realtime khi subscribe vào nội dung đó
- setApplicationDestinationPrefixes: là nơi mà user sẽ gửi các thông tin đi 
- addEndpoint: chỗ đăng ký ws
2. @MessageMapping giống với @PostMapping nhưng dành cho Websocket
## Send message
1. khi gửi tin nhắn sẽ đánh dấu là đã nhận trong db đối với người nhận, xong rồi gửi ra tin nhắn đó ra cho các user đang subscribe đoạn tin nhắn có thể đọc real time

## Seen message
1. khi người dùng click vào ô input ở gửi tin nhắn nếu có tin nhắn mới chưa đọc thì sẽ gửi seen vào controller vào đánh dấu đã đọc

## Load message
1. Load nội dung đoạn chat và gửi kèm seen message  

## upload image/video
## delete message
## update message
## unread-count: dùng cho hiển thị số tin nhắn của mỗi người trong trang bạn bè
## unread-total: dùng để hiển thị ở thanh sidebar left, sử dụng ở notification

# Notification
1. gắn các notification ở các nơi cần để tạo notification
## load notification
## notification unread count
## mark read

# AI 
1. Work flow:
Nhận message từ user
Tạo JSON request
Gửi POST tới Groq
Nhận JSON response
Lấy content
Trả về frontend

# Call video
1. Luồng 1 — Gọi bình thường (Happy path)
Caller bấm icon camera → startVideoCall(friendId) chạy: bật camera/mic, tạo RTCPeerConnection, tạo offer SDP rồi gửi qua WebSocket đến /app/call. Server forward đến /topic/call/{calleeId}. Callee nhận offer → handleIncomingCall() → hiện modal. Callee bấm Nhận → acceptCall(): bật camera, tạo answer SDP gửi ngược lại. Caller nhận answer → setRemoteDescription() + clearTimeout(). Sau đó cả hai trao đổi ICE candidates qua lại cho đến khi P2P kết nối thành công và video hiển thị.

2. Luồng 2 — Từ chối
Callee bấm Từ chối → gửi type: "reject" → Caller nhận → alert + endCall(). Modal bên Callee tự đóng.

3. Luồng 3 — Không phản hồi (timeout 5s)
Caller đặt callTimeout ngay sau khi gửi offer. Sau 5 giây nếu chưa có answer → tự động gửi type: "cancel" cho Callee → modal bên Callee tự đóng → Caller endCall().

4. Luồng 4 — Mất kết nối giữa chừng
onconnectionstatechange theo dõi trạng thái P2P. Nếu disconnected hoặc failed → tự động gọi endCall().

5. Luồng 5 — Kết thúc chủ động
Bấm nút End → endCall(): đóng peerConnection, dừng tất cả track của localStream, ẩn callModal. Nếu có currentCallRemoteId thì gửi thêm type: "end" để bên kia cũng tự đóng.

pendingCandidates[] là buffer quan trọng — ICE candidates có thể đến trước khi remoteDescription được set, nên phải lưu tạm và add sau khi setRemoteDescription() hoàn thành.