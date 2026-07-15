USE HRMS_Demo;
GO

-- Kiểm tra account sau main flow và soft delete.
SELECT user_id, username, email, status, deleted_at, deleted_by, delete_reason
FROM dbo.users
ORDER BY user_id;
GO

-- Kiểm tra role assignments.
SELECT r.role_name, COUNT(CASE WHEN u.status <> 'DELETED' THEN 1 END) AS active_users
FROM dbo.roles r
LEFT JOIN dbo.user_roles ur ON ur.role_id = r.role_id
LEFT JOIN dbo.users u ON u.user_id = ur.user_id
GROUP BY r.role_name
ORDER BY r.role_name;
GO

-- Kiểm tra audit trail.
SELECT TOP 20 audit_id, account_id, action_name, old_status, new_status,
       reason, performed_by, performed_at
FROM dbo.account_audit_log
ORDER BY audit_id DESC;
GO
