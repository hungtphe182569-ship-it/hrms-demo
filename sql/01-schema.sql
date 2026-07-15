IF DB_ID('HRMS_Demo') IS NULL
BEGIN
    CREATE DATABASE HRMS_Demo;
END;
GO

USE HRMS_Demo;
GO

IF OBJECT_ID('dbo.account_audit_log', 'U') IS NOT NULL DROP TABLE dbo.account_audit_log;
IF OBJECT_ID('dbo.attendance', 'U') IS NOT NULL DROP TABLE dbo.attendance;
IF OBJECT_ID('dbo.role_permissions', 'U') IS NOT NULL DROP TABLE dbo.role_permissions;
IF OBJECT_ID('dbo.permissions', 'U') IS NOT NULL DROP TABLE dbo.permissions;
IF OBJECT_ID('dbo.user_roles', 'U') IS NOT NULL DROP TABLE dbo.user_roles;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
IF OBJECT_ID('dbo.roles', 'U') IS NOT NULL DROP TABLE dbo.roles;
GO

CREATE TABLE dbo.roles (
    role_id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    role_name     NVARCHAR(50) NOT NULL UNIQUE,
    description   NVARCHAR(255) NULL,
    created_at    DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

CREATE TABLE dbo.users (
    user_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    username       NVARCHAR(50) NOT NULL UNIQUE,
    password_hash  VARCHAR(64) NOT NULL,
    email          NVARCHAR(120) NOT NULL UNIQUE,
    full_name      NVARCHAR(120) NOT NULL,
    phone          NVARCHAR(20) NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    deleted_at     DATETIME2 NULL,
    deleted_by     BIGINT NULL,
    delete_reason  NVARCHAR(500) NULL,
    CONSTRAINT CK_users_status CHECK (status IN ('PENDING_ACTIVATION','ACTIVE','INACTIVE','LOCKED','DELETED')),
    CONSTRAINT FK_users_deleted_by FOREIGN KEY (deleted_by) REFERENCES dbo.users(user_id)
);
GO

CREATE TABLE dbo.user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT PK_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT FK_user_roles_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id),
    CONSTRAINT FK_user_roles_role FOREIGN KEY (role_id) REFERENCES dbo.roles(role_id)
);
GO

CREATE TABLE dbo.permissions (
    permission_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    permission_code VARCHAR(60) NOT NULL UNIQUE,
    description     NVARCHAR(255) NULL
);
GO

CREATE TABLE dbo.role_permissions (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT PK_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT FK_role_permissions_role FOREIGN KEY (role_id) REFERENCES dbo.roles(role_id),
    CONSTRAINT FK_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES dbo.permissions(permission_id)
);
GO

CREATE TABLE dbo.attendance (
    attendance_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in        TIME NULL,
    check_out       TIME NULL,
    status          VARCHAR(20) NOT NULL,
    CONSTRAINT UQ_attendance_user_date UNIQUE (user_id, attendance_date),
    CONSTRAINT CK_attendance_status CHECK (status IN ('PRESENT','LATE','ABSENT','LEAVE')),
    CONSTRAINT FK_attendance_user FOREIGN KEY (user_id) REFERENCES dbo.users(user_id)
);
GO

CREATE TABLE dbo.account_audit_log (
    audit_id      BIGINT IDENTITY(1,1) PRIMARY KEY,
    account_id   BIGINT NOT NULL,
    action_name  VARCHAR(30) NOT NULL,
    old_status   VARCHAR(20) NULL,
    new_status   VARCHAR(20) NULL,
    reason       NVARCHAR(500) NULL,
    performed_by BIGINT NOT NULL,
    performed_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_audit_account FOREIGN KEY (account_id) REFERENCES dbo.users(user_id),
    CONSTRAINT FK_audit_admin FOREIGN KEY (performed_by) REFERENCES dbo.users(user_id)
);
GO

CREATE INDEX IX_users_status ON dbo.users(status);
CREATE INDEX IX_users_deleted_at ON dbo.users(deleted_at);
CREATE INDEX IX_user_roles_role_id ON dbo.user_roles(role_id);
CREATE INDEX IX_attendance_date ON dbo.attendance(attendance_date);
GO
