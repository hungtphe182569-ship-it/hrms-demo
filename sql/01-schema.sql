CREATE DATABASE IF NOT EXISTS HRMS_Demo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE HRMS_Demo;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS account_audit_log;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE roles (
    role_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    user_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    full_name     VARCHAR(120) NOT NULL,
    phone         VARCHAR(20) NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at    DATETIME NULL,
    deleted_by    BIGINT NULL,
    delete_reason VARCHAR(500) NULL,
    CONSTRAINT CK_users_status CHECK (status IN ('PENDING_ACTIVATION','ACTIVE','INACTIVE','LOCKED','DELETED')),
    CONSTRAINT FK_users_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(user_id)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT PK_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT FK_user_roles_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT FK_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

CREATE TABLE permissions (
    permission_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(60) NOT NULL UNIQUE,
    description     VARCHAR(255) NULL
);

CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    CONSTRAINT PK_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT FK_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT FK_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(permission_id)
);

CREATE TABLE attendance (
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

CREATE TABLE account_audit_log (
    audit_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id   BIGINT NOT NULL,
    action_name  VARCHAR(30) NOT NULL,
    old_status   VARCHAR(20) NULL,
    new_status   VARCHAR(20) NULL,
    reason       VARCHAR(500) NULL,
    performed_by BIGINT NOT NULL,
    performed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_audit_account FOREIGN KEY (account_id) REFERENCES users(user_id),
    CONSTRAINT FK_audit_admin FOREIGN KEY (performed_by) REFERENCES users(user_id)
);

CREATE INDEX IX_users_status ON users(status);
CREATE INDEX IX_users_deleted_at ON users(deleted_at);
CREATE INDEX IX_user_roles_role_id ON user_roles(role_id);
CREATE INDEX IX_attendance_date ON attendance(attendance_date);
