package com.group5.hrms.service;

import com.group5.hrms.dao.AccountDao;
import com.group5.hrms.dao.RoleDao;
import com.group5.hrms.model.Account;
import com.group5.hrms.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class AccountService {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9._-]{3,50}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private final AccountDao accountDao = new AccountDao();
    private final RoleDao roleDao = new RoleDao();

    public List<Account> findAll(boolean includeDeleted) throws SQLException {
        return accountDao.findAll(includeDeleted);
    }

    public long create(String username, String email, String fullName, String phone,
                       String rawPassword, long[] roleIds) throws SQLException {
        username = value(username);
        email = value(email);
        fullName = value(fullName);
        phone = value(phone);
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username phải dài 3-50 ký tự và chỉ gồm chữ, số, . _ -");
        }
        validateProfile(email, fullName);
        validatePassword(rawPassword);
        validateRoles(roleIds);
        if (accountDao.existsUsername(username)) throw new IllegalArgumentException("Username already exists");
        if (accountDao.existsEmail(email)) throw new IllegalArgumentException("Email already exists");

        Account account = new Account();
        account.setUsername(username);
        account.setEmail(email);
        account.setFullName(fullName);
        account.setPhone(phone);
        return accountDao.create(account, PasswordUtil.hash(rawPassword), roleIds);
    }

    public void update(long accountId, String email, String fullName, String phone,
                       long[] roleIds, long adminId) throws SQLException {
        email = value(email);
        fullName = value(fullName);
        phone = value(phone);
        validateProfile(email, fullName);
        validateRoles(roleIds);
        if (accountDao.existsEmailForOtherAccount(email, accountId)) {
            throw new IllegalArgumentException("Email already exists");
        }
        Account account = new Account();
        account.setEmail(email);
        account.setFullName(fullName);
        account.setPhone(phone);
        accountDao.update(accountId, account, roleIds, adminId);
    }

    public void changeStatus(long accountId, String currentStatus, String targetStatus, long adminId)
            throws SQLException {
        if (accountId == adminId && !"ACTIVE".equals(targetStatus)) {
            throw new IllegalArgumentException("Bạn không thể khóa hoặc vô hiệu hóa tài khoản đang đăng nhập");
        }
        boolean allowed = ("ACTIVE".equals(currentStatus)
                && ("INACTIVE".equals(targetStatus) || "LOCKED".equals(targetStatus)))
                || (("INACTIVE".equals(currentStatus) || "LOCKED".equals(currentStatus))
                && "ACTIVE".equals(targetStatus));
        if (!allowed) throw new IllegalArgumentException("Chuyển trạng thái không hợp lệ");
        accountDao.changeStatus(accountId, currentStatus, targetStatus, adminId);
    }

    public void softDelete(long accountId, long adminId, String reason) throws SQLException {
        if (accountId == adminId) throw new IllegalArgumentException("Bạn không thể xóa chính tài khoản đang đăng nhập");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("Deletion reason is required");
        accountDao.softDelete(accountId, adminId, reason.trim());
    }

    public String resetPassword(long accountId, long adminId) throws SQLException {
        String temporaryPassword = PasswordUtil.randomPassword(12);
        accountDao.resetPassword(accountId, PasswordUtil.hash(temporaryPassword), adminId);
        return temporaryPassword;
    }

    private void validateProfile(String email, String fullName) {
        if (!EMAIL_PATTERN.matcher(email).matches()) throw new IllegalArgumentException("Email không đúng định dạng");
        if (fullName.isBlank()) throw new IllegalArgumentException("Full name không được để trống");
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8
                || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*")
                || !password.matches(".*\\d.*") || !password.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalArgumentException("Password phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
        }
    }

    private void validateRoles(long[] roleIds) throws SQLException {
        if (roleIds == null || roleIds.length == 0) throw new IllegalArgumentException("Account phải có ít nhất một role");
        for (long roleId : roleIds) {
            if (!roleDao.exists(roleId)) throw new IllegalArgumentException("Role không tồn tại");
        }
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }
}
