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
import java.util.Optional;

public class AccountDao {
    public Optional<Account> authenticate(String username, String passwordHash) throws SQLException {
        String sql = """
                SELECT u.user_id, u.username, u.email, u.full_name, u.phone, u.status,
                       u.created_at, u.updated_at, u.deleted_at, u.deleted_by, u.delete_reason,
                       r.role_id, r.role_name, r.description
                FROM users u
                LEFT JOIN user_roles ur ON ur.user_id = u.user_id
                LEFT JOIN roles r ON r.role_id = ur.role_id
                WHERE LOWER(u.username) = LOWER(?) AND u.password_hash = ? AND u.status = 'ACTIVE'
                """;
        Account account = null;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    if (account == null) account = mapAccount(rs);
                    long roleId = rs.getLong("role_id");
                    if (!rs.wasNull()) {
                        Role role = new Role();
                        role.setId(roleId);
                        role.setName(rs.getString("role_name"));
                        role.setDescription(rs.getString("description"));
                        account.getRoles().add(role);
                    }
                }
            }
        }
        return Optional.ofNullable(account);
    }

    public List<Account> findAll(boolean includeDeleted) throws SQLException {
        String sql = """
                SELECT u.user_id, u.username, u.email, u.full_name, u.phone, u.status,
                       u.created_at, u.updated_at, u.deleted_at, u.deleted_by, u.delete_reason,
                       r.role_id, r.role_name, r.description
                FROM users u
                LEFT JOIN user_roles ur ON ur.user_id = u.user_id
                LEFT JOIN roles r ON r.role_id = ur.role_id
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
        return exists("SELECT 1 FROM users WHERE username = ?", username);
    }

    public boolean existsEmail(String email) throws SQLException {
        return exists("SELECT 1 FROM users WHERE email = ?", email);
    }

    public boolean existsEmailForOtherAccount(String email, long accountId) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM users WHERE email = ? AND user_id <> ?")) {
            statement.setString(1, email);
            statement.setLong(2, accountId);
            try (ResultSet rs = statement.executeQuery()) { return rs.next(); }
        }
    }

    public long create(Account account, String passwordHash, long[] roleIds) throws SQLException {
        String insertAccount = """
                INSERT INTO users(username, password_hash, email, full_name, phone, status)
                VALUES (?, ?, ?, ?, ?, 'ACTIVE')
                """;
        String insertRole = "INSERT INTO user_roles(user_id, role_id) VALUES (?, ?)";
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
                replaceRoles(connection, accountId, roleIds, insertRole);
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

    public void update(long accountId, Account account, long[] roleIds, long adminId) throws SQLException {
        String update = """
                UPDATE users SET email = ?, full_name = ?, phone = ?, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ? AND status <> 'DELETED'
                """;
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(update)) {
                    statement.setString(1, account.getEmail());
                    statement.setString(2, account.getFullName());
                    statement.setString(3, account.getPhone());
                    statement.setLong(4, accountId);
                    if (statement.executeUpdate() != 1) throw new IllegalArgumentException("Account not found or deleted");
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM user_roles WHERE user_id = ?")) {
                    statement.setLong(1, accountId);
                    statement.executeUpdate();
                }
                replaceRoles(connection, accountId, roleIds,
                        "INSERT INTO user_roles(user_id, role_id) VALUES (?, ?)");
                audit(connection, accountId, "UPDATE", null, null, "Admin updated account", adminId);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void changeStatus(long accountId, String expectedStatus, String newStatus, long adminId) throws SQLException {
        String update = """
                UPDATE users SET status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ? AND status = ? AND status <> 'DELETED'
                """;
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(update)) {
                    statement.setString(1, newStatus);
                    statement.setLong(2, accountId);
                    statement.setString(3, expectedStatus);
                    if (statement.executeUpdate() != 1) throw new IllegalStateException("Account state has changed or action is not allowed");
                }
                audit(connection, accountId, "STATUS_CHANGE", expectedStatus, newStatus,
                        expectedStatus + " -> " + newStatus, adminId);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void softDelete(long accountId, long adminId, String reason) throws SQLException {
        String readStatus = "SELECT status FROM users WHERE user_id = ? FOR UPDATE";
        String update = """
                UPDATE users
                SET status = 'DELETED', deleted_at = CURRENT_TIMESTAMP, deleted_by = ?,
                    delete_reason = ?, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ? AND status <> 'DELETED'
                """;
        String audit = """
                INSERT INTO account_audit_log(
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
                UPDATE users SET password_hash = ?, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ? AND status <> 'DELETED'
                """;
        String audit = """
                INSERT INTO account_audit_log(account_id, action_name, reason, performed_by)
                VALUES (?, 'RESET_PASSWORD', 'Admin reset password', ?)
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

    private void replaceRoles(Connection connection, long accountId, long[] roleIds, String insertSql)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            for (long roleId : roleIds) {
                statement.setLong(1, accountId);
                statement.setLong(2, roleId);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void audit(Connection connection, long accountId, String action, String oldStatus,
                       String newStatus, String reason, long adminId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO account_audit_log(
                    account_id, action_name, old_status, new_status, reason, performed_by)
                VALUES (?, ?, ?, ?, ?, ?)
                """)) {
            statement.setLong(1, accountId);
            statement.setString(2, action);
            statement.setString(3, oldStatus);
            statement.setString(4, newStatus);
            statement.setString(5, reason);
            statement.setLong(6, adminId);
            statement.executeUpdate();
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
            if (rs.getTimestamp("updated_at") != null) {
                account.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            if (rs.getTimestamp("deleted_at") != null) {
                account.setDeletedAt(rs.getTimestamp("deleted_at").toLocalDateTime());
            }
            account.setDeleteReason(rs.getString("delete_reason"));
            long deletedBy = rs.getLong("deleted_by");
            if (!rs.wasNull()) account.setDeletedBy(deletedBy);
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
