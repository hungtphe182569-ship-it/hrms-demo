package com.group5.hrms.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {
    @Test
    void hashIsDeterministicSha256Hex() {
        String hash = PasswordUtil.hash("Admin@123");
        assertEquals(64, hash.length());
        assertEquals(hash, PasswordUtil.hash("Admin@123"));
        assertNotEquals(hash, PasswordUtil.hash("Other@123"));
    }

    @Test
    void generatedPasswordHasRequestedLength() {
        String password = PasswordUtil.randomPassword(12);
        assertEquals(12, password.length());
        assertTrue(password.chars().allMatch(c -> c >= 33 && c <= 126));
    }
}
