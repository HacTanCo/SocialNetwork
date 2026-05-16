# Báo Cáo Phân Tích Mã Nguồn - Chi Tiết Từng Commit

Báo cáo này trình bày chi tiết về sự thay đổi của mã nguồn (source code) trong từng commit để phục vụ quá trình bảo vệ và báo cáo với giảng viên hướng dẫn. Các file Java (Controller, Service, Repository, Model), file giao diện (HTML/Thymeleaf) và kịch bản tĩnh (JS/CSS) được giải thích dựa trên tiến trình phát triển.

---

### 1. Khởi tạo dự án (09/03/2026)
* **Commit:** `init project`
* **Thay đổi mã nguồn:**
  * Khởi tạo file cấu hình lõi: `SocialnetworkApplication.java`, `pom.xml`.
  * **Model:** Khởi tạo các thực thể ban đầu gồm `User.java` (quản lý người dùng) và `Role.java` (quản lý quyền hạn).
  * **Repository:** Tạo interface `UserRepository`, `RoleRepository` kế thừa JpaRepository.
  * **Service:** Khởi tạo `AuthService` và `RoleService` xử lý logic.
  * **Controller:** Thêm `AuthController` (đăng nhập/đăng ký), `PostController` (chuẩn bị), `RoleController`.
  * **DTO & Exception:** Định nghĩa các class `LoginRequest`, `RegisterRequest` để giao tiếp API và các custom exception (`ResourceNotFoundException`, `ResourceAlreadyExistsException`).
  * **View:** Tạo các file HTML cơ bản như `login.html`, `register.html`, `home.html` và file CSS `auth.css`.

### 2. Bảo mật cấu hình (10/03/2026)
* **Commit:** `Remove application.properties from repo`
* **Thay đổi mã nguồn:** Xóa `application.properties` khỏi git để bảo mật chuỗi kết nối Database và thông tin nhạy cảm. Thay thế bằng file `.gitignore` để không bị push nhầm.

### 3. Đặt lại mật khẩu (10/03/2026)
* **Commit:** `Hoàn thành reset password`
* **Thay đổi mã nguồn:**
  * **Security:** Khởi tạo cấu hình bảo mật `SecurityConfig.java` và `CustomUserDetailsService.java` cho Spring Security.
  * **Model & Repository:** Tạo thực thể `OtpResetPassword` và `OtpResetPasswordRepository` để lưu trữ token.
  * **Service:** Bổ sung `EmailService.java` dùng JavaMailSender để gửi mã OTP, nâng cấp `UserService` xử lý lưu trữ mật khẩu.
  * **Controller & DTO:** Tạo các request DTO (`ForgotPasswordRequest`, `ResetPasswordRequest`, `VerifyOtpRequest`). Cập nhật `AuthController` tạo endpoints cấp lại mật khẩu.
  * **View:** Thêm các luồng giao diện: `forgot-password.html`, `reset-password.html`, `verify-otp.html`.

### 4. Giao diện và duy trì đăng nhập (12/03/2026)
* **Commit:** `thêm tính năng remember me và giao diện người dùng`
* **Thay đổi mã nguồn:**
  * **Security:** Cập nhật hàm cấu hình `SecurityConfig.java` để kích hoạt cookie "Remember Me".
  * **Giao diện:** Xây dựng cấu trúc layout chính: `sidebar-left.html`, `sidebar-right.html`, `modals.html`.
  * **Resource tĩnh:** Thêm file `instagram-layout.css` và `ai-chat.css` để thiết lập kiểu dáng UI/UX hiện đại theo chuẩn MXH.

### 5. Phát triển tính năng Bài viết - Giao diện tạo bài (13/03/2026)
* **Commit:** `hoàn thiện giao diện craete page`
* **Thay đổi mã nguồn:**
  * **Model:** Thiết kế sơ đồ quan hệ: thêm bảng `Post`, `PostMedia`, và enum `MediaType` (Image, Video).
  * **Giao diện & Logic:** Cập nhật `modals.html` để chứa popup tạo bài. Bổ sung `previewImage.js` xử lý Javascript cho phép người dùng xem trước hình ảnh/video trước khi upload.

### 6. Xử lý API Bài viết & Validation (15/03/2026)
* **Commit:** `hoàn thành tạo và xem post đã validation`
* **Thay đổi mã nguồn:**
  * **Cấu hình:** Thêm `WebConfig.java` ánh xạ thư mục upload để Spring Boot phục vụ các file media tĩnh.
  * **Tầng Database & Logic:** Tạo `PostRepository`, `PostMediaRepository` và `PostService.java`.
  * **Controller:** Hoàn thiện `PostController` tiếp nhận `MultipartFile` để xử lý tệp tin người dùng tải lên, validate kích cỡ và định dạng tệp trước khi lưu vào server.

### 7. Hoàn tất CRUD Bài viết (16/03/2026)
* **Commit:** `hoàn thiện CRUD với post`
* **Thay đổi mã nguồn:**
  * **Helper:** Bổ sung class `TinhThoiGian.java` xử lý tính toán "Thời gian trôi qua" (Ví dụ: "5 phút trước", "Vừa xong").
  * **Controller & Service:** Bổ sung API để chỉnh sửa (Update) và xóa (Delete) đối tượng Post.
  * **Frontend JS:** Bổ sung `loadFeed.js` và `editDeltePost.js` sử dụng Ajax fetch danh sách bài viết động mà không cần tải lại trang.

### 8. Tối ưu Query hiệu suất (18/03/2026)
* **Commit:** `fix N+1 query`
* **Thay đổi mã nguồn:**
  * **Repository:** Cập nhật `PostRepository` dùng `@EntityGraph` hoặc câu truy vấn `JOIN FETCH` để khắc phục lỗi truy vấn N+1 (N+1 query problem) khi lấy các Post đi kèm Media và User.
  * **DTO Mapping:** Tạo `PostResponseDTO`, `PostMediaResponseDTO` để chuyển đổi thực thể phức tạp sang dữ liệu JSON phẳng, tiết kiệm băng thông.

### 9. Xây dựng chức năng Thích (Like) (19/03/2026)
* **Commit:** `hoàn thành chức năng like`
* **Thay đổi mã nguồn:**
  * **Model & Repository:** Tạo thực thể `Like` và `LikeRepository`.
  * **Controller & Service:** Thêm `LikeController` & `LikeService` quản lý hành vi toggle (Thích/Bỏ thích).
  * **Cập nhật:** Bổ sung thuộc tính boolean `isLiked` trong `PostResponseDTO` và JS file `like.js` thao tác qua API.

### 10. Chức năng Bình luận (20/03/2026)
* **Commit:** `hoàn thiện comment`
* **Thay đổi mã nguồn:**
  * Tương tự module Like, xây dựng đầy đủ `Comment` (Model), `CommentRepository`, `CommentService`, `CommentController`.
  * Khởi tạo các `CommentResponseDTO` và bổ sung JS `commentModal.js` mở popup bình luận.

### 11. Tính năng Kết bạn (24/03/2026)
* **Commit:** `hoàn thiện kết bạn`
* **Thay đổi mã nguồn:**
  * **Model:** Tạo bảng `Friendship` với enum trạng thái `FriendshipStatus` (PENDING, ACCEPTED, BLOCKED).
  * **Logic:** Phát triển `FriendshipService` & `FriendshipController` để gửi lời mời, đồng ý hoặc từ chối kết bạn. Thêm truy vấn gợi ý bạn bè: `UserSuggestionResponseDTO`.
  * **View:** Thêm `friendship.html` và kịch bản `follow.js` quản lý logic giao diện kết bạn.

### 12 - 19. Nâng cấp Profile cá nhân và Bạn bè (26/03 - 31/03/2026)
* **Commits:** `add profile`, `add hover profile`, `update trạng thái ở profile`, `Phân trang cho page friend`, `update profile và xem ảnh`, v.v...
* **Thay đổi mã nguồn:**
  * **Controller:** Thêm `UserController` quản lý endpoint thông tin cá nhân.
  * **Giao diện Popup Profile (Hover):** Tạo `UserProfileDTO` và `popupProfile.js` – khi trỏ chuột vào Avatar, tự động gửi Ajax trả về thẻ thông tin mini (Hover Profile Card).
  * **Trang cá nhân:** Bổ sung logic lấy danh sách bài đăng theo ID cá nhân. Sửa đổi `UserService` hỗ trợ cập nhật thay đổi Avatar. Cập nhật script `previewAvatarToHon.js`.
  * **Tối ưu:** Nâng cấp `FriendshipRepository` thêm Pageable để phân trang danh sách bạn bè, chống giật lag. Thêm custom query trả về `ListFriendFromProfileResponseDTO`.

### 20. Bình luận Realtime với WebSocket (03/04/2026)
* **Commit:** `update comment realtime and add relationship of post`
* **Thay đổi mã nguồn:**
  * Cấu hình **Spring WebSockets**: Thêm file `WebSocketConfig.java` để bật STOMP endpoint (`/ws`).
  * Sửa `CommentController`: Push event qua `SimpMessagingTemplate` mỗi khi có người bình luận mới thay vì trả về HTTP Response thông thường.
  * Sửa `commentModal.js`: Client đăng ký lắng nghe (Subscribe) channel WebSocket để load comment lập tức.

### 21 - 27. Hệ thống Nhắn tin Realtime (Chat) (06/04 - 14/04/2026)
* **Commits:** Từ `thêm chức năng message` đến `Add feature preview the number of unread message`
* **Thay đổi mã nguồn:**
  * **Kiến trúc DB:** Định nghĩa thực thể `Message`, `MessageRepository` và enum `MessageType` (TEXT, IMAGE, VIDEO).
  * **Logic Chat:** Tạo `MessageService`, `MessageController`. Các tin nhắn được lưu trữ và đồng bộ hóa qua WebSocket.
  * **Giao diện Chat:** Phát triển giao diện hộp thư trong `chat.html` / `chat.css` và logic xử lý websocket trong `chat.js`.
  * **Cập nhật thêm tính năng:** Xử lý luồng tải lên file tĩnh đa phương tiện cho tin nhắn. Thêm tính năng thay đổi trạng thái "Đã xem/Đã nhận" và chức năng thu hồi (delete message). Viết logic Repository count số tin nhắn chưa đọc trả ra view.

### 28. Quản lý lỗi cục bộ & Đổi mật khẩu (16/04/2026)
* **Commit:** `add change password`
* **Thay đổi mã nguồn:**
  * Thêm `GlobalExceptionHandler.java` sử dụng `@ControllerAdvice` để gom cụm và bắt các exception trong toàn dự án, tránh throw raw error ra view.
  * Cập nhật `AuthController` và `changePassword.js` xử lý thay đổi mật khẩu khi người dùng đang ở phiên đăng nhập.

### 29 - 31. Hệ thống Thông báo (Notification) (19/04 - 21/04/2026)
* **Commits:** `Cập nhật thêm thông báo message`, `add notification`, `modify notification`
* **Thay đổi mã nguồn:**
  * Tạo thực thể và bảng cơ sở dữ liệu `Notification`.
  * Tại `LikeService`, `CommentService`, `FriendshipService`: Chèn thêm lệnh call qua `NotificationService` mỗi khi có hành động thành công.
  * Controller: `NotificationController` và JS listener `notification.js` kết hợp WebSocket để đẩy chuông thông báo đẩy trên UI góc màn hình.

### 32. Tích hợp Trò chuyện với AI (22/04/2026)
* **Commit:** `add feature chat with AI`
* **Thay đổi mã nguồn:**
  * Thêm module AI: `AIService.java` đảm nhận gửi prompt và request HTTP đến API AI sinh ngôn ngữ.
  * Khởi tạo `AIMessage` Model, Repository, Service lưu trữ lịch sử chat.
  * Cập nhật `aiChat.js` xây dựng khung chat ảo hóa riêng cho Bot.

### 33. Chức năng Gọi Video WebRTC (27/04/2026)
* **Commit:** `add feature call video`
* **Thay đổi mã nguồn:**
  * Khởi tạo WebSocket signaling `CallMessage.java`.
  * Thêm `CallController` trung chuyển tín hiệu WebRTC (Offer, Answer, ICE Candidates) giữa hai Client để thực hiện luồng Video Call Peer-to-Peer.

### 34. Bảng Điều Khiển Quản Trị (02/05/2026)
* **Commit:** `Done admin`
* **Thay đổi mã nguồn:**
  * Thêm `AdminController.java`.
  * Sửa `SecurityConfig.java` để khóa phân quyền, chỉ cho phép role `ADMIN` truy cập `/admin/**`.
  * Tạo layout quản trị viên: `admin-layout.html`, `dashboard.html`, các bảng quản lý CRUD `users.html`, `posts.html`, `comments.html`.
  * Quản trị có quyền khóa User, Xóa các bài viết rác từ người dùng. Bổ sung `access-denied.html` khi user thường cố tình truy cập vào route admin. Trang bị `admin.css`.
