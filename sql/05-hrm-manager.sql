USE HRMS_Demo;

INSERT INTO roles(role_name, description)
VALUES ('HR Manager', 'Quan ly nhan su: dashboard, leave, payroll, phong ban, lich cong ty')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO permissions(permission_code, description)
VALUES
    ('VIEW_HR_DASHBOARD', 'Xem dashboard / KPI HR Manager'),
    ('VIEW_HR_ANALYTICS', 'Xem HR Analytics'),
    ('VIEW_HR_REPORTS', 'Xem Reports Dashboard HR Manager'),
    ('VIEW_ACTIVITY_CENTER', 'Xem Activity Center'),
    ('APPROVE_LEAVE', 'Duyet / tu choi / revert don nghi phep HRM'),
    ('MANAGE_PAYROLL', 'Duyet payroll cuoi va xuat CSV'),
    ('MANAGE_DEPARTMENT', 'CRUD phong ban'),
    ('MANAGE_CALENDAR', 'Quan ly lich cong ty'),
    ('MANAGE_REQUEST', 'Manage employee requests at HR level')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT IGNORE INTO role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
CROSS JOIN permissions p
WHERE r.role_name = 'HR Manager'
  AND p.permission_code IN (
      'VIEW_HR_DASHBOARD','VIEW_HR_ANALYTICS','VIEW_HR_REPORTS','VIEW_ACTIVITY_CENTER',
      'APPROVE_LEAVE','MANAGE_REQUEST','MANAGE_PAYROLL','MANAGE_DEPARTMENT','MANAGE_CALENDAR'
  );

CREATE TABLE IF NOT EXISTS departments (
    department_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_code VARCHAR(30) NOT NULL,
    department_name VARCHAR(120) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT CK_departments_status CHECK (status IN ('ACTIVE','INACTIVE')),
    CONSTRAINT UQ_departments_code UNIQUE (department_code),
    CONSTRAINT UQ_departments_name UNIQUE (department_name)
);

CREATE TABLE IF NOT EXISTS employees (
    employee_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT NOT NULL UNIQUE,
    department_id      BIGINT NULL,
    gender             VARCHAR(20) NULL,
    base_salary        DECIMAL(18,2) NOT NULL DEFAULT 0,
    hire_date          DATE NULL,
    training_progress  INT NOT NULL DEFAULT 0,
    leave_balance_days DECIMAL(8,2) NOT NULL DEFAULT 12,
    position_title     VARCHAR(120) NULL,
    status             VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT CK_employees_status CHECK (status IN ('ACTIVE','INACTIVE')),
    CONSTRAINT CK_employees_gender CHECK (gender IS NULL OR gender IN ('MALE','FEMALE','OTHER')),
    CONSTRAINT FK_employees_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT FK_employees_department FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

CREATE TABLE IF NOT EXISTS leave_requests (
    leave_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id      BIGINT NOT NULL,
    leave_type       VARCHAR(30) NOT NULL,
    start_date       DATE NOT NULL,
    end_date         DATE NOT NULL,
    days_count       INT NOT NULL,
    reason           VARCHAR(500) NULL,
    status           VARCHAR(30) NOT NULL DEFAULT 'PENDING_HRM',
    rejection_reason VARCHAR(500) NULL,
    decided_by       BIGINT NULL,
    decided_at       DATETIME NULL,
    previous_status  VARCHAR(30) NULL,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT CK_leave_type CHECK (leave_type IN ('ANNUAL','SICK','MATERNITY','LONG_TERM','UNPAID')),
    CONSTRAINT CK_leave_status CHECK (status IN ('PENDING_DEPARTMENT_MANAGER','PENDING_HRM','PENDING_HR_MANAGER','APPROVED','REJECTED')),
    CONSTRAINT FK_leave_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    CONSTRAINT FK_leave_decided_by FOREIGN KEY (decided_by) REFERENCES users(user_id)
);

CREATE INDEX IX_leave_status ON leave_requests(status);

CREATE TABLE IF NOT EXISTS payroll_batches (
    batch_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_label     VARCHAR(40) NOT NULL,
    period_start     DATE NOT NULL,
    period_end       DATE NOT NULL,
    status           VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    total_amount     DECIMAL(18,2) NOT NULL DEFAULT 0,
    employee_count   INT NOT NULL DEFAULT 0,
    locked           TINYINT(1) NOT NULL DEFAULT 0,
    rejection_reason VARCHAR(500) NULL,
    approved_by      BIGINT NULL,
    approved_at      DATETIME NULL,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT CK_payroll_status CHECK (status IN ('DRAFT','SUBMITTED','APPROVED_FINAL','REJECTED')),
    CONSTRAINT FK_payroll_approved_by FOREIGN KEY (approved_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS payslips (
    payslip_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id     BIGINT NOT NULL,
    employee_id  BIGINT NOT NULL,
    gross_pay    DECIMAL(18,2) NOT NULL,
    net_pay      DECIMAL(18,2) NOT NULL,
    bank_account VARCHAR(40) NULL,
    published    TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT FK_payslip_batch FOREIGN KEY (batch_id) REFERENCES payroll_batches(batch_id),
    CONSTRAINT FK_payslip_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

CREATE TABLE IF NOT EXISTS company_events (
    event_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(160) NOT NULL,
    event_type  VARCHAR(30) NOT NULL,
    start_at    DATETIME NOT NULL,
    end_at      DATETIME NOT NULL,
    description VARCHAR(500) NULL,
    created_by  BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT CK_event_type CHECK (event_type IN ('HOLIDAY','MEETING','DEADLINE','OTHER')),
    CONSTRAINT CK_event_range CHECK (end_at >= start_at),
    CONSTRAINT FK_event_created_by FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS activity_logs (
    activity_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id BIGINT NULL,
    action_name   VARCHAR(60) NOT NULL,
    entity_type   VARCHAR(40) NULL,
    entity_id     BIGINT NULL,
    details       VARCHAR(500) NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_activity_actor FOREIGN KEY (actor_user_id) REFERENCES users(user_id)
);

CREATE INDEX IX_activity_created ON activity_logs(created_at DESC);

CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    title           VARCHAR(160) NOT NULL,
    message         VARCHAR(500) NOT NULL,
    is_read         TINYINT(1) NOT NULL DEFAULT 0,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_notification_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS HR_REPORT (
    report_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_type      VARCHAR(30) NOT NULL,
    report_year      INT NULL,
    date_from        DATE NULL,
    date_to          DATE NULL,
    review_period    VARCHAR(50) NULL,
    department_id    BIGINT NULL,
    report_payload   TEXT NOT NULL,
    generated_at     DATETIME NOT NULL,
    cache_expires_at DATETIME NOT NULL,
    CONSTRAINT CK_HR_REPORT_type CHECK (report_type IN ('OVERVIEW','ATTENDANCE','LEAVE','PAYROLL','PERFORMANCE')),
    CONSTRAINT CK_HR_REPORT_date_range CHECK (date_from IS NULL OR date_to IS NULL OR date_from <= date_to),
    CONSTRAINT FK_HR_REPORT_department FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

CREATE INDEX idx_hr_report_filter
    ON HR_REPORT(report_type, department_id, report_year, date_from, date_to);

CREATE TABLE IF NOT EXISTS AUDIT_LOG (
    audit_log_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id        BIGINT NOT NULL,
    action_code     VARCHAR(30) NOT NULL,
    report_id       BIGINT NULL,
    report_type     VARCHAR(30) NOT NULL,
    filter_snapshot TEXT NULL,
    result_status   VARCHAR(20) NOT NULL,
    occurred_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT CK_AUDIT_LOG_action CHECK (action_code IN ('VIEW_REPORT','EXPORT_REPORT')),
    CONSTRAINT CK_AUDIT_LOG_result CHECK (result_status IN ('SUCCESS','NO_DATA')),
    CONSTRAINT FK_AUDIT_LOG_HR_REPORT FOREIGN KEY (report_id) REFERENCES HR_REPORT(report_id)
);

CREATE TABLE IF NOT EXISTS employee_requests (
    request_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id          BIGINT NOT NULL,
    category             VARCHAR(40) NOT NULL,
    title                VARCHAR(160) NOT NULL,
    description          VARCHAR(500) NULL,
    status               VARCHAR(30) NOT NULL DEFAULT 'PENDING_HR',
    target_department_id BIGINT NULL,
    proposed_salary      DECIMAL(18,2) NULL,
    proposed_position    VARCHAR(120) NULL,
    rejection_reason     VARCHAR(500) NULL,
    decided_by           BIGINT NULL,
    decided_at           DATETIME NULL,
    created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT CK_employee_request_category CHECK (category IN ('SALARY_CHANGE','PROMOTION','TRANSFER','BENEFIT','EQUIPMENT','OTHER')),
    CONSTRAINT CK_employee_request_status CHECK (status IN ('PENDING_HR','PENDING_HR_MANAGER','PENDING_ADMIN','APPROVED','REJECTED')),
    CONSTRAINT FK_employee_request_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    CONSTRAINT FK_employee_request_target_dept FOREIGN KEY (target_department_id) REFERENCES departments(department_id),
    CONSTRAINT FK_employee_request_decided_by FOREIGN KEY (decided_by) REFERENCES users(user_id)
);

INSERT INTO users(username, password_hash, email, full_name, phone, status)
SELECT 'hrm01', UPPER(SHA2('Hrm@123', 256)), 'hrm01@hrms.local', 'Pham Lan HRM', '0901000010', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'hrm01');

INSERT IGNORE INTO user_roles(user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u
JOIN roles r ON r.role_name = 'HR Manager'
WHERE u.username = 'hrm01';

INSERT IGNORE INTO departments(department_code, department_name)
VALUES ('HR', 'Human Resources'),
       ('IT', 'Information Technology'),
       ('FIN', 'Finance'),
       ('SALES', 'Sales');

INSERT INTO employees(user_id, department_id, gender, base_salary, hire_date, training_progress, status)
SELECT u.user_id, d.department_id, v.gender, v.salary,
       DATE_SUB(CURDATE(), INTERVAL v.years_count YEAR), v.training, 'ACTIVE'
FROM users u
JOIN (
    SELECT 'manager01' AS username, 'IT' AS dept_code, 'MALE' AS gender, 22000000 AS salary, 3 AS years_count, 80 AS training
    UNION ALL SELECT 'employee01', 'IT', 'FEMALE', 14000000, 1, 65
    UNION ALL SELECT 'employee02', 'FIN', 'FEMALE', 15000000, 2, 40
    UNION ALL SELECT 'hrm01', 'HR', 'FEMALE', 25000000, 4, 90
) v ON u.username = v.username
JOIN departments d ON d.department_code = v.dept_code
WHERE NOT EXISTS (SELECT 1 FROM employees e WHERE e.user_id = u.user_id);

INSERT INTO employee_requests(employee_id, category, title, description, status, proposed_salary)
SELECT e.employee_id, 'SALARY_CHANGE', 'Adjust base salary', 'Annual salary adjustment request', 'PENDING_HR', 16500000
FROM employees e JOIN users u ON u.user_id = e.user_id
WHERE u.username = 'employee01'
  AND NOT EXISTS (SELECT 1 FROM employee_requests WHERE category = 'SALARY_CHANGE');

INSERT INTO employee_requests(employee_id, category, title, description, status, target_department_id)
SELECT e.employee_id, 'TRANSFER', 'Transfer to Sales', 'Move employee to Sales department', 'PENDING_HR',
       (SELECT department_id FROM departments WHERE department_code = 'SALES')
FROM employees e JOIN users u ON u.user_id = e.user_id
WHERE u.username = 'employee02'
  AND NOT EXISTS (SELECT 1 FROM employee_requests WHERE category = 'TRANSFER');

INSERT INTO employee_requests(employee_id, category, title, description, status, proposed_position)
SELECT e.employee_id, 'PROMOTION', 'Promotion proposal', 'Promotion requires Admin approval after HR review', 'PENDING_HR_MANAGER', 'Senior Specialist'
FROM employees e JOIN users u ON u.user_id = e.user_id
WHERE u.username = 'manager01'
  AND NOT EXISTS (SELECT 1 FROM employee_requests WHERE category = 'PROMOTION');

INSERT INTO leave_requests(employee_id, leave_type, start_date, end_date, days_count, reason, status)
SELECT e.employee_id, 'MATERNITY', DATE_ADD(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 40 DAY), 30, 'Nghi thai san', 'PENDING_HRM'
FROM employees e JOIN users u ON u.user_id = e.user_id
WHERE u.username = 'employee01'
  AND NOT EXISTS (SELECT 1 FROM leave_requests WHERE leave_type = 'MATERNITY');

INSERT INTO leave_requests(employee_id, leave_type, start_date, end_date, days_count, reason, status)
SELECT e.employee_id, 'LONG_TERM', DATE_ADD(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY), 15, 'Dieu tri dai han', 'PENDING_HRM'
FROM employees e JOIN users u ON u.user_id = e.user_id
WHERE u.username = 'employee02'
  AND NOT EXISTS (SELECT 1 FROM leave_requests WHERE leave_type = 'LONG_TERM');

INSERT INTO leave_requests(employee_id, leave_type, start_date, end_date, days_count, reason, status, decided_by, decided_at)
SELECT e.employee_id, 'ANNUAL', DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 18 DAY), 3, 'Nghi phep nam',
       'APPROVED', (SELECT user_id FROM users WHERE username = 'hrm01'), DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 2 HOUR)
FROM employees e JOIN users u ON u.user_id = e.user_id
WHERE u.username = 'manager01'
  AND NOT EXISTS (SELECT 1 FROM leave_requests WHERE leave_type = 'ANNUAL' AND status = 'APPROVED');

INSERT INTO payroll_batches(period_label, period_start, period_end, status, total_amount, employee_count)
SELECT '2026-06', '2026-06-01', '2026-06-30', 'SUBMITTED', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM payroll_batches);

SET @batchId = (SELECT batch_id FROM payroll_batches WHERE period_label = '2026-06' ORDER BY batch_id LIMIT 1);

INSERT INTO payslips(batch_id, employee_id, gross_pay, net_pay, bank_account, published)
SELECT @batchId, e.employee_id, e.base_salary, e.base_salary * 0.9, CONCAT('BANK-', e.employee_id), 0
FROM employees e
WHERE e.status = 'ACTIVE'
  AND @batchId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM payslips p WHERE p.batch_id = @batchId AND p.employee_id = e.employee_id);

UPDATE payroll_batches b
JOIN (
    SELECT batch_id, COALESCE(SUM(net_pay), 0) AS total_net, COUNT(*) AS cnt
    FROM payslips
    WHERE batch_id = @batchId
    GROUP BY batch_id
) s ON s.batch_id = b.batch_id
SET b.total_amount = s.total_net,
    b.employee_count = s.cnt
WHERE b.batch_id = @batchId;

SET @hrmId = (SELECT user_id FROM users WHERE username = 'hrm01');

INSERT INTO company_events(title, event_type, start_at, end_at, description, created_by)
SELECT x.title, x.event_type, x.start_at, x.end_at, x.description, @hrmId
FROM (
    SELECT 'Cong ty nghi Quoc khanh' AS title, 'HOLIDAY' AS event_type,
           CAST(DATE_ADD(CURDATE(), INTERVAL 20 DAY) AS DATETIME) AS start_at,
           DATE_ADD(CAST(DATE_ADD(CURDATE(), INTERVAL 20 DAY) AS DATETIME), INTERVAL 23 HOUR) AS end_at,
           'Nghi toan cong ty' AS description
    UNION ALL
    SELECT 'Hop kickoff Q3', 'MEETING',
           DATE_ADD(CAST(DATE_ADD(CURDATE(), INTERVAL 3 DAY) AS DATETIME), INTERVAL 9 HOUR),
           DATE_ADD(CAST(DATE_ADD(CURDATE(), INTERVAL 3 DAY) AS DATETIME), INTERVAL 11 HOUR),
           'Phong hop A'
    UNION ALL
    SELECT 'Han chot bang luong', 'DEADLINE',
           DATE_ADD(CAST(DATE_ADD(CURDATE(), INTERVAL 7 DAY) AS DATETIME), INTERVAL 17 HOUR),
           DATE_ADD(CAST(DATE_ADD(CURDATE(), INTERVAL 7 DAY) AS DATETIME), INTERVAL 17 HOUR),
           'Payroll cutoff'
) x
WHERE @hrmId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM company_events);

INSERT INTO activity_logs(actor_user_id, action_name, entity_type, entity_id, details)
SELECT u.user_id, 'HRM_BOOTSTRAP', 'SYSTEM', NULL, 'Khoi tao module HR Manager'
FROM users u
WHERE u.username = 'hrm01'
  AND NOT EXISTS (SELECT 1 FROM activity_logs WHERE action_name = 'HRM_BOOTSTRAP');
