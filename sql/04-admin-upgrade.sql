USE HRMS_Demo;

CREATE TABLE IF NOT EXISTS permissions (
    permission_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(60) NOT NULL UNIQUE,
    description     VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT PK_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT FK_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT FK_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(permission_id)
);

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in        TIME NULL,
    check_out       TIME NULL,
    status          VARCHAR(20) NOT NULL,
    CONSTRAINT UQ_attendance_user_date UNIQUE (user_id, attendance_date),
    CONSTRAINT CK_attendance_status CHECK (status IN ('PRESENT','LATE','ABSENT','LEAVE')),
    CONSTRAINT FK_attendance_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX IX_attendance_date ON attendance(attendance_date);

INSERT INTO permissions(permission_code, description)
VALUES
    ('MANAGE_ACCOUNT', 'Tao, xem, cap nhat, xoa va reset tai khoan'),
    ('MANAGE_ROLE', 'Tao, gan, cap nhat va xoa vai tro'),
    ('VIEW_REPORT', 'Xem bao cao va thong ke'),
    ('EXPORT_REPORT', 'Xuat bao cao Excel/PDF')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT IGNORE INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r CROSS JOIN permissions p
WHERE r.role_name = 'Admin';
