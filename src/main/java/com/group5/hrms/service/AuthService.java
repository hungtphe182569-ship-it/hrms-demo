package com.group5.hrms.service;

import com.group5.hrms.dao.AccountDao;
import com.group5.hrms.model.Account;
import com.group5.hrms.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private final AccountDao accountDao = new AccountDao();

    public Optional<Account> login(String username, String password) throws SQLException {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return Optional.empty();
        }
        return accountDao.authenticate(username.trim(), PasswordUtil.hash(password));
    }
}
