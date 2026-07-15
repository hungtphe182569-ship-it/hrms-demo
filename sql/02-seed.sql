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
