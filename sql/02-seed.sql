USE HRMS_Demo;

INSERT IGNORE INTO roles(role_name, description)
VALUES
    ('Admin', 'Quan tri toan bo he thong'),
    ('Manager', 'Quan ly phong ban va bao cao'),
    ('Employee', 'Nhan vien su dung chuc nang ca nhan');

INSERT IGNORE INTO users(username, password_hash, email, full_name, phone, status)
VALUES
    ('admin', UPPER(SHA2('Admin@123', 256)), 'admin@hrms.local', 'System Administrator', '0901000001', 'ACTIVE'),
    ('manager01', UPPER(SHA2('Manager@123', 256)), 'manager01@hrms.local', 'Nguyen Minh Quan', '0901000002', 'ACTIVE'),
    ('employee01', UPPER(SHA2('Employee@123', 256)), 'employee01@hrms.local', 'Tran An Nhien', '0901000003', 'ACTIVE'),
    ('employee02', UPPER(SHA2('Employee@123', 256)), 'employee02@hrms.local', 'Le Hoang Mai', '0901000004', 'LOCKED');

INSERT IGNORE INTO user_roles(user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u
JOIN roles r ON
    (u.username = 'admin' AND r.role_name = 'Admin') OR
    (u.username = 'manager01' AND r.role_name = 'Manager') OR
    (u.username IN ('employee01', 'employee02') AND r.role_name = 'Employee');

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

INSERT IGNORE INTO attendance(user_id, attendance_date, check_in, check_out, status)
SELECT u.user_id,
       DATE_SUB(CURDATE(), INTERVAL v.day_offset DAY),
       CASE WHEN v.status = 'ABSENT' THEN NULL ELSE CAST(v.check_in AS TIME) END,
       CASE WHEN v.status IN ('ABSENT','LEAVE') THEN NULL ELSE CAST('17:30:00' AS TIME) END,
       v.status
FROM users u
CROSS JOIN (
    SELECT 0 AS day_offset, '08:00:00' AS check_in, 'PRESENT' AS status
    UNION ALL SELECT 1, '08:20:00', 'LATE'
    UNION ALL SELECT 2, '08:05:00', 'PRESENT'
    UNION ALL SELECT 3, '00:00:00', 'ABSENT'
    UNION ALL SELECT 4, '00:00:00', 'LEAVE'
) v
WHERE u.status <> 'DELETED';
