USE HRMS_Demo;
GO

/* ========== Role + permissions (idempotent) ========== */
MERGE dbo.roles AS target
USING (VALUES (N'HR Manager', N'Quản lý nhân sự: dashboard, leave, payroll, phòng ban, lịch công ty')) AS source(role_name, description)
ON target.role_name = source.role_name
WHEN NOT MATCHED THEN INSERT(role_name, description)
VALUES(source.role_name, source.description);
GO

MERGE dbo.permissions AS target
USING (VALUES
    ('VIEW_HR_DASHBOARD', N'Xem dashboard / KPI HR Manager'),
    ('VIEW_HR_ANALYTICS', N'Xem HR Analytics'),
    ('VIEW_ACTIVITY_CENTER', N'Xem Activity Center'),
    ('APPROVE_LEAVE', N'Duyệt / từ chối / revert đơn nghỉ phép HRM'),
    ('MANAGE_PAYROLL', N'Duyệt payroll cuối và xuất CSV'),
    ('MANAGE_DEPARTMENT', N'CRUD phòng ban'),
    ('MANAGE_CALENDAR', N'Quản lý lịch công ty')
) AS source(permission_code, description)
ON target.permission_code = source.permission_code
WHEN NOT MATCHED THEN INSERT(permission_code, description)
VALUES(source.permission_code, source.description);
GO

INSERT INTO dbo.role_permissions(role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM dbo.roles r
CROSS JOIN dbo.permissions p
WHERE r.role_name = N'HR Manager'
  AND p.permission_code IN (
      'VIEW_HR_DASHBOARD','VIEW_HR_ANALYTICS','VIEW_ACTIVITY_CENTER',
      'APPROVE_LEAVE','MANAGE_PAYROLL','MANAGE_DEPARTMENT','MANAGE_CALENDAR'
  )
  AND NOT EXISTS (
      SELECT 1 FROM dbo.role_permissions rp
      WHERE rp.role_id = r.role_id AND rp.permission_id = p.permission_id
  );
GO

/* ========== Domain tables ========== */
IF OBJECT_ID('dbo.departments', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.departments (
        department_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
        department_code NVARCHAR(30) NOT NULL,
        department_name NVARCHAR(120) NOT NULL,
        status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
        created_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        updated_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT CK_departments_status CHECK (status IN ('ACTIVE','INACTIVE')),
        CONSTRAINT UQ_departments_code UNIQUE (department_code),
        CONSTRAINT UQ_departments_name UNIQUE (department_name)
    );
END;
GO

IF OBJECT_ID('dbo.employees', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.employees (
        employee_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id         BIGINT NOT NULL UNIQUE,
        department_id   BIGINT NULL,
        gender          VARCHAR(20) NULL,
        base_salary     DECIMAL(18,2) NOT NULL DEFAULT 0,
        hire_date       DATE NULL,
        training_progress INT NOT NULL DEFAULT 0,
        status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
        CONSTRAINT CK_employees_status CHECK (status IN ('ACTIVE','INACTIVE')),
        CONSTRAINT CK_employees_gender CHECK (gender IS NULL OR gender IN ('MALE','FEMALE','OTHER')),
        CONSTRAINT FK_employees_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id),
        CONSTRAINT FK_employees_department FOREIGN KEY (department_id) REFERENCES dbo.departments(department_id)
    );
END;
GO

IF OBJECT_ID('dbo.leave_requests', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.leave_requests (
        leave_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
        employee_id     BIGINT NOT NULL,
        leave_type      VARCHAR(30) NOT NULL,
        start_date      DATE NOT NULL,
        end_date        DATE NOT NULL,
        days_count      INT NOT NULL,
        reason          NVARCHAR(500) NULL,
        status          VARCHAR(30) NOT NULL DEFAULT 'PENDING_HRM',
        rejection_reason NVARCHAR(500) NULL,
        decided_by      BIGINT NULL,
        decided_at      DATETIME2 NULL,
        previous_status VARCHAR(30) NULL,
        created_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        updated_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT CK_leave_type CHECK (leave_type IN ('ANNUAL','SICK','MATERNITY','LONG_TERM','UNPAID')),
        CONSTRAINT CK_leave_status CHECK (status IN ('PENDING_HRM','APPROVED','REJECTED')),
        CONSTRAINT FK_leave_employee FOREIGN KEY (employee_id) REFERENCES dbo.employees(employee_id),
        CONSTRAINT FK_leave_decided_by FOREIGN KEY (decided_by) REFERENCES dbo.users(user_id)
    );
    CREATE INDEX IX_leave_status ON dbo.leave_requests(status);
END;
GO

IF OBJECT_ID('dbo.payroll_batches', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.payroll_batches (
        batch_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
        period_label    NVARCHAR(40) NOT NULL,
        period_start    DATE NOT NULL,
        period_end      DATE NOT NULL,
        status          VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
        total_amount    DECIMAL(18,2) NOT NULL DEFAULT 0,
        employee_count  INT NOT NULL DEFAULT 0,
        locked          BIT NOT NULL DEFAULT 0,
        rejection_reason NVARCHAR(500) NULL,
        approved_by     BIGINT NULL,
        approved_at     DATETIME2 NULL,
        created_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        updated_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT CK_payroll_status CHECK (status IN ('DRAFT','SUBMITTED','APPROVED_FINAL','REJECTED')),
        CONSTRAINT FK_payroll_approved_by FOREIGN KEY (approved_by) REFERENCES dbo.users(user_id)
    );
END;
GO

IF OBJECT_ID('dbo.payslips', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.payslips (
        payslip_id      BIGINT IDENTITY(1,1) PRIMARY KEY,
        batch_id        BIGINT NOT NULL,
        employee_id     BIGINT NOT NULL,
        gross_pay       DECIMAL(18,2) NOT NULL,
        net_pay         DECIMAL(18,2) NOT NULL,
        bank_account    NVARCHAR(40) NULL,
        published       BIT NOT NULL DEFAULT 0,
        CONSTRAINT FK_payslip_batch FOREIGN KEY (batch_id) REFERENCES dbo.payroll_batches(batch_id),
        CONSTRAINT FK_payslip_employee FOREIGN KEY (employee_id) REFERENCES dbo.employees(employee_id)
    );
END;
GO

IF OBJECT_ID('dbo.company_events', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.company_events (
        event_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
        title           NVARCHAR(160) NOT NULL,
        event_type      VARCHAR(30) NOT NULL,
        start_at        DATETIME2 NOT NULL,
        end_at          DATETIME2 NOT NULL,
        description     NVARCHAR(500) NULL,
        created_by      BIGINT NOT NULL,
        created_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        updated_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT CK_event_type CHECK (event_type IN ('HOLIDAY','MEETING','DEADLINE','OTHER')),
        CONSTRAINT CK_event_range CHECK (end_at >= start_at),
        CONSTRAINT FK_event_created_by FOREIGN KEY (created_by) REFERENCES dbo.users(user_id)
    );
END;
GO

IF OBJECT_ID('dbo.activity_logs', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.activity_logs (
        activity_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
        actor_user_id   BIGINT NULL,
        action_name     VARCHAR(60) NOT NULL,
        entity_type     VARCHAR(40) NULL,
        entity_id       BIGINT NULL,
        details         NVARCHAR(500) NULL,
        created_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_activity_actor FOREIGN KEY (actor_user_id) REFERENCES dbo.users(user_id)
    );
    CREATE INDEX IX_activity_created ON dbo.activity_logs(created_at DESC);
END;
GO

IF OBJECT_ID('dbo.notifications', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.notifications (
        notification_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id         BIGINT NOT NULL,
        title           NVARCHAR(160) NOT NULL,
        message         NVARCHAR(500) NOT NULL,
        is_read         BIT NOT NULL DEFAULT 0,
        created_at      DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_notification_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id)
    );
END;
GO

/* ========== Seed HR Manager account ========== */
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'hrm01')
BEGIN
    INSERT INTO dbo.users(username, password_hash, email, full_name, phone, status)
    VALUES (
        N'hrm01',
        CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', 'Hrm@123'), 2),
        N'hrm01@hrms.local',
        N'Phạm Lan HRM',
        N'0901000010',
        'ACTIVE'
    );
END;
GO

INSERT INTO dbo.user_roles(user_id, role_id)
SELECT u.user_id, r.role_id
FROM dbo.users u
JOIN dbo.roles r ON r.role_name = N'HR Manager'
WHERE u.username = N'hrm01'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.user_roles ur
      WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id
  );
GO

/* ========== Seed departments / employees / sample data ========== */
IF NOT EXISTS (SELECT 1 FROM dbo.departments)
BEGIN
    INSERT INTO dbo.departments(department_code, department_name)
    VALUES (N'HR', N'Human Resources'),
           (N'IT', N'Information Technology'),
           (N'FIN', N'Finance'),
           (N'SALES', N'Sales');
END;
GO

INSERT INTO dbo.employees(user_id, department_id, gender, base_salary, hire_date, training_progress, status)
SELECT u.user_id, d.department_id, v.gender, v.salary, DATEADD(YEAR, -v.years, CAST(GETDATE() AS DATE)), v.training, 'ACTIVE'
FROM dbo.users u
JOIN (VALUES
    (N'manager01', N'IT', 'MALE', 22000000, 3, 80),
    (N'employee01', N'IT', 'FEMALE', 14000000, 1, 65),
    (N'employee02', N'FIN', 'FEMALE', 15000000, 2, 40),
    (N'hrm01', N'HR', 'FEMALE', 25000000, 4, 90)
) v(username, dept_code, gender, salary, years, training)
  ON u.username = v.username
JOIN dbo.departments d ON d.department_code = v.dept_code
WHERE NOT EXISTS (SELECT 1 FROM dbo.employees e WHERE e.user_id = u.user_id);
GO

IF NOT EXISTS (SELECT 1 FROM dbo.leave_requests)
BEGIN
    INSERT INTO dbo.leave_requests(employee_id, leave_type, start_date, end_date, days_count, reason, status)
    SELECT e.employee_id, 'MATERNITY', DATEADD(DAY, 10, CAST(GETDATE() AS DATE)), DATEADD(DAY, 40, CAST(GETDATE() AS DATE)), 30, N'Nghỉ thai sản', 'PENDING_HRM'
    FROM dbo.employees e JOIN dbo.users u ON u.user_id = e.user_id WHERE u.username = N'employee01';

    INSERT INTO dbo.leave_requests(employee_id, leave_type, start_date, end_date, days_count, reason, status)
    SELECT e.employee_id, 'LONG_TERM', DATEADD(DAY, 5, CAST(GETDATE() AS DATE)), DATEADD(DAY, 20, CAST(GETDATE() AS DATE)), 15, N'Điều trị dài hạn', 'PENDING_HRM'
    FROM dbo.employees e JOIN dbo.users u ON u.user_id = e.user_id WHERE u.username = N'employee02';

    INSERT INTO dbo.leave_requests(employee_id, leave_type, start_date, end_date, days_count, reason, status)
    SELECT e.employee_id, 'ANNUAL', DATEADD(DAY, -20, CAST(GETDATE() AS DATE)), DATEADD(DAY, -18, CAST(GETDATE() AS DATE)), 3, N'Nghỉ phép năm', 'APPROVED'
    FROM dbo.employees e JOIN dbo.users u ON u.user_id = e.user_id WHERE u.username = N'manager01';

    UPDATE dbo.leave_requests
    SET decided_by = (SELECT user_id FROM dbo.users WHERE username = N'hrm01'),
        decided_at = DATEADD(HOUR, -2, SYSDATETIME())
    WHERE status = 'APPROVED';
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.payroll_batches)
BEGIN
    DECLARE @batchId BIGINT;
    INSERT INTO dbo.payroll_batches(period_label, period_start, period_end, status, total_amount, employee_count)
    VALUES (N'2026-06', '2026-06-01', '2026-06-30', 'SUBMITTED', 0, 0);
    SET @batchId = SCOPE_IDENTITY();

    INSERT INTO dbo.payslips(batch_id, employee_id, gross_pay, net_pay, bank_account, published)
    SELECT @batchId, e.employee_id, e.base_salary, e.base_salary * 0.9, N'BANK-' + CAST(e.employee_id AS NVARCHAR(20)), 0
    FROM dbo.employees e WHERE e.status = 'ACTIVE';

    UPDATE b
    SET total_amount = s.total_net,
        employee_count = s.cnt
    FROM dbo.payroll_batches b
    CROSS JOIN (
        SELECT SUM(net_pay) AS total_net, COUNT(*) AS cnt
        FROM dbo.payslips WHERE batch_id = @batchId
    ) s
    WHERE b.batch_id = @batchId;
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.company_events)
BEGIN
    DECLARE @hrmId BIGINT = (SELECT user_id FROM dbo.users WHERE username = N'hrm01');
    INSERT INTO dbo.company_events(title, event_type, start_at, end_at, description, created_by)
    VALUES
        (N'Công ty nghỉ Quốc khánh', 'HOLIDAY', DATEADD(DAY, 20, CAST(CAST(GETDATE() AS DATE) AS DATETIME2)), DATEADD(DAY, 20, DATEADD(HOUR, 23, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), N'Nghỉ toàn công ty', @hrmId),
        (N'Họp kickoff Q3', 'MEETING', DATEADD(DAY, 3, DATEADD(HOUR, 9, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), DATEADD(DAY, 3, DATEADD(HOUR, 11, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), N'Phòng họp A', @hrmId),
        (N'Hạn chốt bảng lương', 'DEADLINE', DATEADD(DAY, 7, DATEADD(HOUR, 17, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), DATEADD(DAY, 7, DATEADD(HOUR, 17, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), N'Payroll cutoff', @hrmId);
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.activity_logs)
BEGIN
    INSERT INTO dbo.activity_logs(actor_user_id, action_name, entity_type, entity_id, details)
    SELECT u.user_id, 'HRM_BOOTSTRAP', 'SYSTEM', NULL, N'Khởi tạo module HR Manager'
    FROM dbo.users u WHERE u.username = N'hrm01';
END;
GO
