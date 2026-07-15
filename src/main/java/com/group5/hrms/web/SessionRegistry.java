package com.group5.hrms.web;

import jakarta.servlet.http.HttpSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionRegistry {
    private static final ConcurrentHashMap<Long, Set<HttpSession>> SESSIONS = new ConcurrentHashMap<>();

    private SessionRegistry() { }

    public static void register(long accountId, HttpSession session) {
        session.setAttribute("accountId", accountId);
        SESSIONS.computeIfAbsent(accountId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public static void unregister(HttpSession session) {
        Object accountId;
        try { accountId = session.getAttribute("accountId"); }
        catch (IllegalStateException ignored) { return; }
        if (accountId instanceof Long id) {
            Set<HttpSession> sessions = SESSIONS.get(id);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) SESSIONS.remove(id);
            }
        }
    }

    public static void revokeAll(long accountId) {
        Set<HttpSession> sessions = SESSIONS.remove(accountId);
        if (sessions == null) return;
        for (HttpSession session : sessions) {
            try { session.invalidate(); } catch (IllegalStateException ignored) { }
        }
    }
}
