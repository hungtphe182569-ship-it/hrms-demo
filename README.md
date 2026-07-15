# HRMS Admin Demo – JSP + SQL Server

Demo chỉ gồm 3 chức năng Admin:

1. Manage Account: xem, tạo, reset password và soft delete tài khoản.
2. Manage Roles: xem, tạo, cập nhật và xóa role chưa được sử dụng.
3. View Reports & Statistics: thống kê tài khoản theo trạng thái và role.

Phiên bản hiện tại bám theo UC17-UC19 trong tài liệu thiết kế:

- UC17: tạo, xem, cập nhật, gán nhiều role, chuyển trạng thái, reset password và soft delete có audit/thu hồi session.
- UC18: thống kê account, attendance, permission và xuất Excel/PDF.
- UC19: tạo, cập nhật, xóa role chưa được sử dụng; role được gán trong Account Management.
- Admin Center được bảo vệ bằng role và permission RBAC.

## Yêu cầu

- JDK 17
- Apache Maven 3.9+
- Apache Tomcat 10.1+
- Microsoft SQL Server 2019+

## Cài đặt database

1. Mở SQL Server Management Studio.
2. Chạy `sql/01-schema.sql`.
3. Chạy `sql/02-seed.sql`.

## Cấu hình kết nối

Thiết lập biến môi trường trước khi chạy Tomcat:

```text
HRMS_DB_URL=jdbc:sqlserver://localhost:1433;databaseName=HRMS_Demo;encrypt=true;trustServerCertificate=true
HRMS_DB_USER=sa
HRMS_DB_PASSWORD=YourStrong!Passw0rd
```

Nếu không đặt biến môi trường, ứng dụng dùng các giá trị mẫu trên.

## Build và chạy

Cách nhanh trên Windows (tự build, deploy và mở trình duyệt):

```powershell
.\run.ps1
```

Dừng Tomcat cục bộ:

```powershell
.\run.ps1 stop
```

Khi khởi động, migration `sql/04-admin-upgrade.sql` được chạy idempotent để nâng cấp database cũ mà không xóa dữ liệu.

Trên Windows có thể dùng Maven Wrapper đi kèm, không cần cài Maven thủ công:

```text
mvnw.cmd clean package
```

Hoặc dùng Maven đã cài sẵn:

```text
mvn clean package
```

Copy `target/hrms-admin-demo.war` vào thư mục `webapps` của Tomcat và mở:

```text
http://localhost:8080/hrms-admin-demo/
```

Tài khoản seed dùng cho kịch bản demo: `admin` / `Admin@123`.

Lưu ý: project tập trung vào ba use case Admin và không triển khai authentication hoàn chỉnh. ID Admin demo được cố định là `1` để minh họa audit và quy tắc không tự xóa.
