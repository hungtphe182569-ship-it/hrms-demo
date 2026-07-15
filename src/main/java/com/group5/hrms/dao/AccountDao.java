package com.group5.hrms.dao;

import com.group5.hrms.config.Database;
import com.group5.hrms.model.Account;
import com.group5.hrms.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccountDao {
    public List<Account> findAll(boolean includeDeleted) throws SQLException {
        String sql = """
                SELECT u.user_id, u.username, u.email, u.full_name, u.phone, u.status,
                       u.created_at, u.deleted_at, u.delete_reason,
                       r.role_id, r.role_name, r.description
                FROM dbo.users u
                LEFT JOIN dbo.user_roles ur ON ur.user_id = u.user_id
                LEFT JOIN dbo.roles r ON r.role_id = ur.role_id
                WHERE (? = 1 OR u.status <> 'DELETED')
                ORDER BY u.created_at DESC, u.user_id DESC
                """;
        Map<Long, Account> accounts = new LinkedHashMap<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, includeDeleted);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    long accountId = rs.getLong("user_id");
                    Account account = accounts.computeIfAbsent(accountId, id -> mapAccount(rs));
                    long roleId = rs.getLong("role_id");
                    if (!rs.wasNull()) {
                        Role role = new Role();
                        role.setId(roleId);
                        role.setName(getQuietly(rs, "role_name"));
                        role.setDescription(getQuietly(rs, "description"));
                        account.getRoles().add(role);
                    }
                }
            }
        }
        return new ArrayList<>(accounts.values());
    }

    public boolean existsUsername(String username) throws SQLException {
        return exists("SELECT 1 FROM dbo.users WHERE username = ?", username);
    }

    public boolean existsEmail(String email) throws SQLException {
        return exists("SELECT 1 FROM dbo.users WHERE email = ?", email);
    }

    public long create(Account account, String passwordHash, long roleId) throws SQLException {
        String insertAccount = """
                INSERT INTO dbo.users(username, password_hash, email, full_name, phone, status)
                VALUES (?, ?, ?, ?, ?, 'ACTIVE')
                """;
        String insertRole = "INSERT INTO dbo.user_roles(user_id, role_id) VALUES (?, ?)";
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long accountId;
                try (PreparedStatement statement = connection.prepareStatement(
                        insertAccount, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, account.getUsername());
                    statement.setString(2, passwordHash);
                    statement.setString(3, account.getEmail());
                    statement.setString(4, account.getFullName());
                    statement.setString(5, account.getPhone());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Cannot read generated account ID");
                        accountId = keys.getLong(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(insertRole)) {
                    statement.setLong(1, accountId);
                    statement.setLong(2, roleId);
                    statement.executeUpdate();
                }
                connection.commit();
                return accountId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void softDelete(long accountId, long adminId, String reason) throws SQLException {
        String readStatus = "SELECT status FROM dbo.users WITH (UPDLOCK, ROWLOCK) WHERE user_id = ?";
        String update = """
                UPDATE dbo.users
                SET status = 'DELETED', deleted_at = SYSDATETIME(), deleted_by = ?,
                    delete_reason = ?, updated_at = SYSDATETIME()
                WHERE user_id = ? AND status <> 'DELETED'
                """;
        String audit = """
                INSERT INTO dbo.account_audit_log(
                    account_id, action_name, old_status, new_status, reason, performed_by)
                VALUES (?, 'SOFT_DELETE', ?, 'DELETED', ?, ?)
                """;

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String oldStatus = null;
                try (PreparedStatement statement = connection.prepareStatement(readStatus)) {
                    statement.setLong(1, accountId);
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) oldStatus = rs.getString(1);
                    }
                }
                if (oldStatus == null) throw new IllegalArgumentException("Account not found");
                if ("DELETED".equals(oldStatus)) throw new IllegalStateException("Account already deleted");

                try (PreparedStatement statement = connection.prepareStatement(update)) {
                    statement.setLong(1, adminId);
                    statement.setString(2, reason);
                    statement.setLong(3, accountId);
                    if (statement.executeUpdate() != 1) throw new IllegalStateException("Delete failed");
                }
                try (PreparedStatement statement = connection.prepareStatement(audit)) {
                    statement.setLong(1, accountId);
                    statement.setString(2, oldStatus);
                    statement.setString(3, reason);
                    statement.setLong(4, adminId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void resetPassword(long accountId, String passwordHash, long adminId) throws SQLException {
        String update = """
                UPDATE dbo.users SET password_hash = ?, updated_at = SYSDATETIME()
                WHERE user_id = ? AND status <> 'DELETED'
                """;
        String audit = """
                INSERT INTO dbo.account_audit_log(account_id, action_name, reason, performed_by)
                VALUES (?, 'RESET_PASSWORD', N'Admin reset password', ?)
                """;
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(update)) {
                    statement.setString(1, passwordHash);
                    statement.setLong(2, accountId);
                    if (statement.executeUpdate() != 1) {
                        throw new IllegalArgumentException("Account not found or deleted");
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(audit)) {
                    statement.setLong(1, accountId);
                    statement.setLong(2, adminId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private boolean exists(String sql, String value) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Account mapAccount(ResultSet rs) {
        Account account = new Account();
        try {
            account.setId(rs.getLong("user_id"));
            account.setUsername(rs.getString("username"));
            account.setEmail(rs.getString("email"));
            account.setFullName(rs.getString("full_name"));
            account.setPhone(rs.getString("phone"));
            account.setStatus(rs.getString("status"));
            account.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            if (rs.getTimestamp("deleted_at") != null) {
                account.setDeletedAt(rs.getTimestamp("deleted_at").toLocalDateTime());
            }
            account.setDeleteReason(rs.getString("delete_reason"));
            return account;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getQuietly(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
