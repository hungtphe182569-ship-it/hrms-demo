USE HRMS_Demo;

-- Kiem tra account sau main flow va soft delete.
SELECT user_id, username, email, status, deleted_at, deleted_by, delete_reason
FROM users
ORDER BY user_id;

-- Kiem tra role assignments.
SELECT r.role_name, COUNT(CASE WHEN u.status <> 'DELETED' THEN 1 END) AS active_users
FROM roles r
LEFT JOIN user_roles ur ON ur.role_id = r.role_id
LEFT JOIN users u ON u.user_id = ur.user_id
GROUP BY r.role_name
ORDER BY r.role_name;

-- Kiem tra audit trail.
SELECT audit_id, account_id, action_name, old_status, new_status,
       reason, performed_by, performed_at
FROM account_audit_log
ORDER BY audit_id DESC
LIMIT 20;
