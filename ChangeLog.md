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