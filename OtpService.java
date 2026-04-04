package com.laundrymgmt.modern.service;

import com.laundrymgmt.modern.dto.AuthDtos;
import com.laundrymgmt.modern.model.OtpPurpose;
import com.laundrymgmt.modern.model.UserAccount;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();
    private final Duration otpLifetime = Duration.ofMinutes(5);

    public AuthDtos.OtpResponse generate(UserAccount userAccount, OtpPurpose purpose, String message) {
        String code = Integer.toString(ThreadLocalRandom.current().nextInt(100000, 1_000_000));
        Instant expiresAt = Instant.now().plus(otpLifetime);
        otpStore.put(key(userAccount.getId(), purpose), new OtpRecord(code, expiresAt));
        return new AuthDtos.OtpResponse(message, code, expiresAt);
    }

    public boolean verify(UserAccount userAccount, OtpPurpose purpose, String otpCode) {
        if (otpCode == null || otpCode.isBlank()) {
            return false;
        }

        String key = key(userAccount.getId(), purpose);
        OtpRecord record = otpStore.get(key);
        if (record == null || record.expiresAt().isBefore(Instant.now())) {
            otpStore.remove(key);
            return false;
        }
        if (!record.code().equals(otpCode.trim())) {
            return false;
        }

        otpStore.remove(key);
        return true;
    }

    private String key(Long userId, OtpPurpose purpose) {
        return userId + ":" + purpose.name();
    }

    private record OtpRecord(String code, Instant expiresAt) {
    }
}
