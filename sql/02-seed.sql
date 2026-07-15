USE HRMS_Demo;
GO

INSERT INTO dbo.roles(role_name, description)
VALUES
    (N'Admin', N'Quản trị toàn bộ hệ thống'),
    (N'Manager', N'Quản lý phòng ban và báo cáo'),
    (N'Employee', N'Nhân viên sử dụng chức năng cá nhân');
GO

INSERT INTO dbo.users(username, password_hash, email, full_name, phone, status)
VALUES
    (N'admin', CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', 'Admin@123'), 2), N'admin@hrms.local', N'System Administrator', N'0901000001', 'ACTIVE'),
    (N'manager01', CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', 'Manager@123'), 2), N'manager01@hrms.local', N'Nguyễn Minh Quản', N'0901000002', 'ACTIVE'),
    (N'employee01', CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', 'Employee@123'), 2), N'employee01@hrms.local', N'Trần An Nhiên', N'0901000003', 'ACTIVE'),
    (N'employee02', CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', 'Employee@123'), 2), N'employee02@hrms.local', N'Lê Hoàng Mai', N'0901000004', 'LOCKED');
GO

INSERT INTO dbo.user_roles(user_id, role_id)
SELECT u.user_id, r.role_id
FROM dbo.users u
JOIN dbo.roles r ON
    (u.username = N'admin' AND r.role_name = N'Admin') OR
    (u.username = N'manager01' AND r.role_name = N'Manager') OR
    (u.username IN (N'employee01', N'employee02') AND r.role_name = N'Employee');
GO

INSERT INTO dbo.permissions(permission_code, description)
VALUES
    ('MANAGE_ACCOUNT', N'Tạo, xem, cập nhật, xóa và reset tài khoản'),
    ('MANAGE_ROLE', N'Tạo, gán, cập nhật và xóa vai trò'),
    ('VIEW_REPORT', N'Xem báo cáo và thống kê'),
    ('EXPORT_REPORT', N'Xuất báo cáo Excel/PDF');
GO

INSERT INTO dbo.role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM dbo.roles r CROSS JOIN dbo.permissions p
WHERE r.role_name = N'Admin';
GO

INSERT INTO dbo.attendance(user_id, attendance_date, check_in, check_out, status)
SELECT u.user_id, CAST(DATEADD(DAY, -v.day_offset, GETDATE()) AS DATE),
       CASE WHEN v.status = 'ABSENT' THEN NULL ELSE CAST(v.check_in AS TIME) END,
       CASE WHEN v.status IN ('ABSENT','LEAVE') THEN NULL ELSE CAST('17:30' AS TIME) END,
       v.status
FROM dbo.users u
CROSS APPLY (VALUES
    (0, '08:00', 'PRESENT'),
    (1, '08:20', 'LATE'),
    (2, '08:05', 'PRESENT'),
    (3, '00:00', 'ABSENT'),
    (4, '00:00', 'LEAVE')
) v(day_offset, check_in, status)
WHERE u.status <> 'DELETED';
GO
