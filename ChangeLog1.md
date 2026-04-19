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
 ## View profile with ib
 ## View profile when we hover at avatar ("/api/user/{id}")
 - const rect = target.getBoundingClientRect(); Lấy vị trí của element được click/hover
 - target = element bạn vừa click (avatar, username...)
 - getBoundingClientRect() trả về vị trí của nó trên màn hình
 - cache dữ liệu user tránh gọi nhiều lân ( gọi api thì userCache[userId] = user;)
 ## List frind at profile page ("/api/friends/{userId}")
 ## Update profile

# Messages

## Load Chat