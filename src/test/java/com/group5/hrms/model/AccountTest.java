package com.group5.hrms.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountTest {
    @Test
    void supportsDocumentedAccountStateTransitions() {
        assertTrue(Account.isTransitionAllowed("ACTIVE", "INACTIVE"));
        assertTrue(Account.isTransitionAllowed("ACTIVE", "LOCKED"));
        assertTrue(Account.isTransitionAllowed("INACTIVE", "ACTIVE"));
        assertTrue(Account.isTransitionAllowed("LOCKED", "ACTIVE"));
    }

    @Test
    void deletedStateIsFinal() {
        assertFalse(Account.isTransitionAllowed("DELETED", "ACTIVE"));
        assertFalse(Account.isTransitionAllowed("ACTIVE", "DELETED"));
        assertFalse(Account.isTransitionAllowed("LOCKED", "INACTIVE"));
    }
}
