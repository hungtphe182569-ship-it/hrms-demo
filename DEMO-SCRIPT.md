# Kịch bản demo 3 chức năng Admin

## Chuẩn bị

1. Chạy `sql/01-schema.sql` và `sql/02-seed.sql`.
2. Cấu hình ba biến môi trường kết nối SQL Server.
3. Chạy `mvnw.cmd clean package`.
4. Copy WAR vào Tomcat 10.1 và mở `/hrms-admin-demo/`.

## 1. Manage Account – khoảng 90 giây

### Main flow

1. Chọn **Create account**.
2. Nhập username mới, email, password và role.
3. Nhấn **Create account**.
4. Chỉ ra record mới trong danh sách.

### Alternative flow

1. Tạo lại đúng username vừa dùng.
2. Hệ thống trả **Username already exists** và không ghi thêm record.

### Thay đổi TT4 – soft delete

1. Chọn nút xóa của một tài khoản Employee.
2. Nhập lý do và xác nhận.
3. Bật **Hiện tài khoản đã xóa** để thấy record vẫn còn với status DELETED.
4. Mở SQL Server và chỉ ra deleted_at, deleted_by, delete_reason.

## 2. Manage Roles – khoảng 45 giây

1. Tạo role `Recruiter`.
2. Cập nhật description của role.
3. Xóa role Recruiter khi chưa được gán: thành công.
4. Thử xóa role Employee đang được sử dụng: hệ thống từ chối.

## 3. Reports & Statistics – khoảng 30 giây

1. Mở trang Reports & Statistics.
2. Chỉ ra tổng account, Active, Locked và số role.
3. Giải thích số liệu được lấy bằng COUNT/GROUP BY từ SQL Server.
4. Sau khi soft delete, tải lại trang để thấy số liệu thay đổi.
