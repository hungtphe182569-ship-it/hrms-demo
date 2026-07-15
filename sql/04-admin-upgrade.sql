USE HRMS_Demo;
GO

IF OBJECT_ID('dbo.permissions', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.permissions (
        permission_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        permission_code VARCHAR(60) NOT NULL UNIQUE,
        description NVARCHAR(255) NULL
    );
END;
GO

IF OBJECT_ID('dbo.role_permissions', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.role_permissions (
        role_id BIGINT NOT NULL,
        permission_id BIGINT NOT NULL,
        CONSTRAINT PK_role_permissions PRIMARY KEY (role_id, permission_id),
        CONSTRAINT FK_role_permissions_role FOREIGN KEY (role_id) REFERENCES dbo.roles(role_id),
        CONSTRAINT FK_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES dbo.permissions(permission_id)
    );
END;
GO

IF OBJECT_ID('dbo.attendance', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.attendance (
        attendance_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        attendance_date DATE NOT NULL,
        check_in TIME NULL,
        check_out TIME NULL,
        status VARCHAR(20) NOT NULL,
        CONSTRAINT UQ_attendance_user_date UNIQUE (user_id, attendance_date),
        CONSTRAINT CK_attendance_status CHECK (status IN ('PRESENT','LATE','ABSENT','LEAVE')),
        CONSTRAINT FK_attendance_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id)
    );
    CREATE INDEX IX_attendance_date ON dbo.attendance(attendance_date);
END;
GO

MERGE dbo.permissions AS target
USING (VALUES
    ('MANAGE_ACCOUNT', N'Tạo, xem, cập nhật, xóa và reset tài khoản'),
    ('MANAGE_ROLE', N'Tạo, gán, cập nhật và xóa vai trò'),
    ('VIEW_REPORT', N'Xem báo cáo và thống kê'),
    ('EXPORT_REPORT', N'Xuất báo cáo Excel/PDF')
) AS source(permission_code, description)
ON target.permission_code = source.permission_code
WHEN NOT MATCHED THEN INSERT(permission_code, description)
VALUES(source.permission_code, source.description);
GO

INSERT INTO dbo.role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM dbo.roles r CROSS JOIN dbo.permissions p
WHERE r.role_name = N'Admin'
  AND NOT EXISTS (SELECT 1 FROM dbo.role_permissions rp
                  WHERE rp.role_id = r.role_id AND rp.permission_id = p.permission_id);
GO
