# API REST Quizflow

## Tổng quan
Đây là API REST backend cho ứng dụng trắc nghiệm Quizflow, được xây dựng bằng Spring Boot. API cung cấp tất cả các endpoint cần thiết để hỗ trợ ứng dụng di động Android Quizflow, bao gồm xác thực người dùng, quản lý bài trắc nghiệm, khả năng chơi nhiều người thông qua WebSockets và các chức năng cốt lõi khác.

## Tính năng
- **Xác thực người dùng**
  - Đăng ký với xác minh email (OTP)
  - Đăng nhập
  - Chức năng đặt lại mật khẩu
  - Quản lý hồ sơ
- **Quản lý bài trắc nghiệm**
  - Tạo, đọc, cập nhật bài trắc nghiệm
  - Tổ chức bài trắc nghiệm theo chủ đề
  - Theo dõi lượt thử và phản hồi bài trắc nghiệm
- **Hỗ trợ chơi nhiều người**
  - Tích hợp WebSocket cho gameplay thời gian thực
  - Quản lý phòng chờ cho các phiên chơi nhiều người
- **Khám phá nội dung**
  - Danh sách chủ đề
  - Tìm kiếm bài trắc nghiệm theo từ khóa
  - Nội dung nổi bật (bài trắc nghiệm được thử nhiều nhất, bài trắc nghiệm gần đây)
- **Xếp hạng người dùng**
  - Theo dõi hiệu suất và điểm số người dùng
  - Bảng xếp hạng (hàng ngày, hàng tuần, mọi thời đại)

## Công nghệ sử dụng
- **Spring Boot 3.4.1** - Framework chính
- **Spring Security** - Xác thực và phân quyền
- **Spring Data JPA** - Tương tác cơ sở dữ liệu
- **Spring WebSocket** - Giao tiếp thời gian thực
- **MySQL** - Cơ sở dữ liệu
- **H2** - Cơ sở dữ liệu phát triển/kiểm thử
- **Java 21** - Ngôn ngữ lập trình
- **Lombok** - Giảm thiểu mã boilerplate
- **Spring Mail** - Dịch vụ email cho xác minh OTP

## Cài đặt và triển khai

### Yêu cầu
- Java 21
- Maven
- MySQL (cho môi trường production)

### Các bước
1. Clone repository
   ```
   git clone https://github.com/yourusername/REST_API.git
   ```

2. Cấu hình cơ sở dữ liệu
   - Chỉnh sửa `src/main/resources/application-dev.properties` cho môi trường phát triển
   - Chỉnh sửa `src/main/resources/application-prod.properties` cho môi trường sản xuất

3. Cấu hình email (cho chức năng OTP)
   - Cập nhật cài đặt email trong `application.properties`

## Các Endpoint API

### Xác thực
- `POST /api/auth/register` - Đăng ký tài khoản mới
- `POST /api/auth/sign-up` - Endpoint đăng ký thay thế
- `POST /api/auth/verify-otp` - Xác minh mã OTP
- `POST /api/auth/resend-otp` - Gửi lại mã OTP
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/forgot-password` - Yêu cầu đặt lại mật khẩu
- `POST /api/auth/reset-password` - Đặt lại mật khẩu
- `POST /api/auth/update-password` - Cập nhật mật khẩu
- `POST /api/auth/update-profile` - Cập nhật hồ sơ người dùng

### Bài trắc nghiệm
- `GET /api/quiz/{qid}` - Lấy bài trắc nghiệm theo ID
- `POST /api/quiz/create` - Tạo bài trắc nghiệm mới
- `POST /api/quiz/update` - Cập nhật bài trắc nghiệm hiện có
- `GET /api/quiz/top10` - Lấy 10 bài trắc nghiệm có nhiều lượt thử nhất
- `GET /api/quiz/created-past-7days` - Lấy các bài trắc nghiệm được tạo trong 7 ngày qua
- `GET /api/quiz/{topic}/public` - Lấy các bài trắc nghiệm công khai theo chủ đề
- `GET /api/quiz/public/{keyword}` - Tìm kiếm bài trắc nghiệm công khai theo từ khóa

### Chủ đề
- `GET /api/topic/all` - Lấy tất cả chủ đề

### Chơi nhiều người
- `Các endpoint WebSocket` - Giao tiếp thời gian thực cho trò chơi nhiều người
