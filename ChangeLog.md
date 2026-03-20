# Social Network Project - Development Log

## 5-6/3: Thiết kế Database và Setup Project

### Mục tiêu cập nhật project
1. **Tech Stack**

**Backend**
- Spring Boot
- Spring Data JPA
- Spring Security

**Frontend**
- HTML
- CSS
- JavaScript
- Bootstrap

**View Engine**
- Thymeleaf

2. **Project hoàn thiện đầy đủ chức năng từ Client đến Admin**

---

# Chức năng hệ thống

## Client

### 1. Authentication
- Đăng ký
- Đăng nhập
- Đăng xuất
- Đổi mật khẩu
- Quên mật khẩu (OTP qua JavaMail)
- Password được mã hóa bằng **BCrypt**

### 2. Post Management
- CRUD bài đăng
- Upload hình ảnh
- Tự động xóa file khi xóa bài viết

### 4. Friend & Follow System
- Gửi lời mời kết bạn
- Chấp nhận lời mời
- Từ chối lời mời
- Theo dõi / Bỏ theo dõi
- Danh sách bạn bè

### 5. Profile Management
- Xem thông tin cá nhân
- Cập nhật thông tin cá nhân

Sử dụng **DTO** cho dữ liệu từ:
- User
- Post
- ...

### 6. AI Chat Integration
- Tích hợp AI (Groq API)
- Trò chuyện với AI
- Lưu lịch sử hội thoại

### 7. Real-time Chat
- Nhắn tin thời gian thực với **WebSocket**
- Lưu lịch sử tin nhắn

### 8. Notification System

# Admin

### 1. Quản lý User
- Xem danh sách user
- Khóa / mở khóa tài khoản (isActive = 0)
- Xóa user (soft delete với isDeleted)

**Ý nghĩa khóa**
- Không đăng bài được
- Hoặc không đăng nhập được

### 2. Quản lý bài viết thông qua Report

### 3. Dashboard
- Thống kê hệ thống

---

# Database Design

1. **Roles**

2. **Users**

3. **OtpResetPasswords**

4. **Posts**

5. **PostMedias**

6. **Comments**

7. **Likes**

8. **Friendships**

9. **AiMessages**

10. **Messages**

11. **Notifications**

12. **ReportPosts**

13. **ReportComments**

---

# Planned Features (If Time Allows)

- Saved Posts
- Stories

---

# Development Timeline

### 9/3
- Làm **CRUD Role**
- Làm quen với **Thymeleaf View Engine**

### 10/3
- Làm **Authentication**

### 12/3
- Bổ sung **Remember Me**
- Sử dụng **Spring Session JDBC**
- Thiết kế giao diện cho **User**
### 13/3
- Hoàn thiện **UI tạo bài viết (Create Post Modal)**

#### Media Upload Preview
- Cho phép upload **multiple images / videos**
- Hiển thị preview trước khi đăng bài
- Sử dụng **Bootstrap Carousel** để hiển thị media dạng slider

#### Media Slider Improvements
- Giới hạn chiều cao preview để tránh modal bị kéo dài
- Căn giữa ảnh/video bằng **flexbox**
- Hỗ trợ hiển thị media với kích thước khác nhau

#### Media Delete Before Upload
- Thêm nút **❌ xóa media trước khi đăng**
- Hover vào media sẽ hiển thị nút xóa
- Cập nhật lại danh sách file bằng **DataTransfer API**

#### Media Counter
- Thêm **media counter (1 / N)** giống Facebook
- Counter cập nhật khi chuyển slide bằng:
  - `slid.bs.carousel` event của Bootstrap

#### Carousel UI Improvements
- Thu nhỏ vùng click của **prev/next control**
- Căn giữa nút `<` `>` theo chiều dọc
- Thêm background tròn cho icon
- Chỉ hiển thị arrow khi hover slider

#### UX Improvements
- Hover media hiển thị nút delete
- Hover delete button đổi màu đỏ
- Counter nằm giữa phía dưới slider

#### Kỹ thuật sử dụng
- **JavaScript DOM Manipulation**
- **DataTransfer API**
- **Bootstrap Carousel Events**
- **Flexbox Layout**

### 15/03

- Hoàn thiện **Media Upload Validation** cho chức năng tạo bài viết

#### File Validation
- Chỉ cho phép upload **image / video hợp lệ**
- Kiểm tra **MIME type** của file trước khi xử lý
- Ngăn người dùng upload các file không phải media (ví dụ `.txt`)

#### Validation Improvements
- Thêm kiểm tra **content type** khi nhận file từ form
- Nếu file không hợp lệ sẽ **từ chối upload và hiển thị thông báo lỗi**

#### Kỹ thuật sử dụng
- **Spring Boot MultipartFile**
- **Content-Type Validation**
- **Server-side Validation**

##### NOTE
- file.upload-dir=${user.dir}/uploads/
user.dir là System Property của Java. ví dụ project nằm ở  D:\Learning\Khoa-Luan-Tot-Nghiep\socialnetwork thì  = với user.dir => upload sẽ nằm ở project/upload
---

### 16/03

- Hoàn thiện **Post Header UI** và **Time Ago Display**

#### Post Action Menu (Three Dots)
- Thêm menu **⋯** cho mỗi bài đăng
- Nếu là **chủ bài đăng**:
  - Hiển thị **Sửa bài viết**
  - Hiển thị **Xóa bài viết**
- Nếu **không phải chủ bài đăng**:
  - Hiển thị **Báo cáo bài viết**

#### Post Header Layout
- Căn chỉnh icon **three dots** sang góc phải
- Sử dụng **Flexbox** để bố cục avatar, tên và action menu

#### Time Ago Display
- Hiển thị thời gian đăng bài dạng:
  - `5 phút trước`
  - `2 giờ trước`
  - `1 ngày trước`
- Tạo helper `TinhThoiGian.timeAgo()` để chuyển `createdAt` sang thời gian tương đối

#### Delete Post
Cho phép **chủ bài viết xóa bài đăng**.

- Chỉ user tạo bài viết mới có quyền xóa
- Sử dụng **POST request** để thực hiện xóa

#### Delete Flow

1. User nhấn **Xóa bài viết**
2. JavaScript gửi request:

POST /post/delete/{id}

3. Server:
- Kiểm tra **quyền sở hữu bài viết**
- Xóa **media liên quan**
- Xóa **bài viết trong database**

#### Media Cleanup

Khi xóa bài viết:
- Xóa **file media trong thư mục uploads**
- Xóa **record PostMedias trong database**

- Hoàn thiện **Delete Post Without Page Reload (AJAX)**

#### AJAX Delete

Khi user xóa bài viết:
- Không cần **reload toàn bộ trang**

#### Flow

User click delete  
↓  
JavaScript `fetch()`  
↓  
POST `/post/delete/{id}`  
↓  
Server xử lý  
↓  
JavaScript xóa post khỏi DOM

#### DOM Manipulation

Sau khi server xử lý xong: javascript btn.closest(".post").remove();
- Hoàn thiện **Edit Post Functionality**

#### Edit Post

Cho phép **chủ bài viết chỉnh sửa nội dung bài đăng**.

#### UI

Sử dụng **Bootstrap Modal** để chỉnh sửa bài viết.

Modal chứa:

- `textarea` để chỉnh sửa nội dung

#### User Flow

Click **Sửa bài viết**  
↓  
Mở modal chỉnh sửa  
↓  
Chỉnh sửa nội dung  
↓  
Click **Lưu**  
↓  
POST `/post/edit/{id}`

#### Server xử lý

Controller:
- Nhận `postId`
- Nhận `content`
- Lấy `USER` từ `Authentication`

Service:
- Kiểm tra **post tồn tại**
- Kiểm tra **user có phải chủ bài viết**
- Update nội dung bài viết
##### NOTE
-  th:text="${T(vn.hactanco.socialnetwork.helper.TinhThoiGian).timeAgo(post.createdAt)}"> có nghĩa như ri T(...) là cú pháp của thằng thymleaf để gọi class trong java, gọi đc class thì sài như java thôi :V


###### Ở trên này có nhờ AI viết

### 17/03

#### Sửa lại lại Post tránh N+1 query bằng DTO và 2 query

1. select 2 lần
 - đầu tiên lấy tất cả id các bài post
 - tiếp theo lấy toàn bộ thông tin các bài post như users.* và medias.*
    + Trước khi sửa nếu lấy 10 post thì sẽ chạy 13 query: 1 query là post, 1 query user, 1 query là đếm số post để phân trang, và 10 query để lấy medias
    ( cái này k nhớ chính xác lắm, sửa lại quên note :V )
    + Sau chỉ cần 3 query: 1 query là lấy id post (trong bài lấy 10), 1 query là đếm số post để phân trang, query còn lại để lấy các thông tin liên quan tới post như user, post, media

2. sử dụng Builder của lombook cho đỡ ghi kiểu new ra 1 đối tượng rồi set data mệt


#### Thêm like bài post

##### Note 1 số hàm
 - dto.setLikeCount(likeCountMap.getOrDefault(post.getId(), 0L)); :
  getOrDefault: Lấy value theo key, nếu key không tồn tại thì trả về giá trị mặc định
- dto.setLiked(likedPostIds.contains(post.getId())); :
  contains: Kiểm tra xem phần tử có tồn tại trong List (hoặc Set) không
			
### 19/03

#### Hoàn thiện comment nhưng cần chỉnh sửa lại cho phù hợp

1. hoàn thiện CRUD
2. cần chỉnh sửa UI lại cho phù hợp

#### đã hoàn thiện UI 

