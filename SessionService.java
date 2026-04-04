package com.laundrymgmt.modern.service;

import com.laundrymgmt.modern.exception.ApiException;
import com.laundrymgmt.modern.model.Role;
import com.laundrymgmt.modern.model.UserAccount;
import com.laundrymgmt.modern.repository.UserAccountRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final Map<String, SessionRecord> sessions = new ConcurrentHashMap<>();
    private final UserAccountRepository userAccountRepository;

    public SessionService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public String createSession(UserAccount userAccount) {
        String token = UUID.randomUUID() + "-" + userAccount.getId();
        sessions.put(token, new SessionRecord(userAccount.getId(), userAccount.getRole(), Instant.now()));
        return token;
    }

    public UserAccount requireUser(String authorizationHeader) {
        SessionRecord session = requireSession(authorizationHeader);
        return userAccountRepository.findById(session.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Your session is no longer valid."));
    }

    public UserAccount requireRole(String authorizationHeader, Role role) {
        UserAccount userAccount = requireUser(authorizationHeader);
        if (userAccount.getRole() != role) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission for this action.");
        }
        return userAccount;
    }

    public void logout(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (token != null && !token.isBlank()) {
            sessions.remove(token);
        }
    }

    private SessionRecord requireSession(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (token == null || token.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing authorization token.");
        }

        SessionRecord session = sessions.get(token);
        if (session == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired session.");
        }
        return session;
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authorization header must use Bearer tokens.");
        }
        return authorizationHeader.substring(7).trim();
    }

    private record SessionRecord(Long userId, Role role, Instant createdAt) {
    }
}
