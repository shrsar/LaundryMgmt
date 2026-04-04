package com.laundrymgmt.modern.dto;

import com.laundrymgmt.modern.model.Role;
import java.time.Instant;
import java.time.LocalDate;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(String customerCode, String password) {
    }

    public record SignupRequest(String displayName, String phone, String password, String securityKey) {
    }

    public record PasswordResetOtpRequest(String customerCode, String securityKey) {
    }

    public record PasswordResetRequest(String customerCode, String securityKey, String otpCode, String newPassword) {
    }

    public record UserProfile(Long id, String customerCode, String displayName, String phone, Role role,
                              LocalDate signupDate) {
    }

    public record AuthResponse(String token, UserProfile user) {
    }

    public record OtpResponse(String message, String otpCode, Instant expiresAt) {
    }
}
