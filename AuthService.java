package com.laundrymgmt.modern.service;

import com.laundrymgmt.modern.dto.AuthDtos;
import com.laundrymgmt.modern.exception.ApiException;
import com.laundrymgmt.modern.model.OtpPurpose;
import com.laundrymgmt.modern.model.Role;
import com.laundrymgmt.modern.model.UserAccount;
import com.laundrymgmt.modern.repository.UserAccountRepository;
import com.laundrymgmt.modern.util.CodeFactory;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final OtpService otpService;
    private final CodeFactory codeFactory;

    public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder,
                       SessionService sessionService, OtpService otpService, CodeFactory codeFactory) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
        this.otpService = otpService;
        this.codeFactory = codeFactory;
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String customerCode = requireText(request.customerCode(), "Customer ID is required.").toUpperCase();
        String password = requireText(request.password(), "Password is required.");

        UserAccount userAccount = userAccountRepository.findByCustomerCodeIgnoreCase(customerCode)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Wrong credentials."));

        if (!passwordEncoder.matches(password, userAccount.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Wrong credentials.");
        }

        String token = sessionService.createSession(userAccount);
        return new AuthDtos.AuthResponse(token, toUserProfile(userAccount));
    }

    @Transactional
    public AuthDtos.AuthResponse signup(AuthDtos.SignupRequest request) {
        String displayName = requireText(request.displayName(), "Display name is required.");
        String phone = normalizePhone(request.phone());
        String securityKey = requireText(request.securityKey(), "Security key is required.");
        String password = requireText(request.password(), "Password is required.");

        validatePassword(password);
        if (userAccountRepository.existsByPhone(phone)) {
            throw new ApiException(HttpStatus.CONFLICT, "That phone number already exists.");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setCustomerCode(codeFactory.generate("U", userAccountRepository::existsByCustomerCodeIgnoreCase));
        userAccount.setDisplayName(displayName);
        userAccount.setPhone(phone);
        userAccount.setSecurityKey(securityKey);
        userAccount.setPasswordHash(passwordEncoder.encode(password));
        userAccount.setRole(Role.CUSTOMER);
        userAccount.setSignupDate(LocalDate.now());

        UserAccount savedAccount = userAccountRepository.save(userAccount);
        String token = sessionService.createSession(savedAccount);
        return new AuthDtos.AuthResponse(token, toUserProfile(savedAccount));
    }

    public AuthDtos.UserProfile me(String authorizationHeader) {
        return toUserProfile(sessionService.requireUser(authorizationHeader));
    }

    public void logout(String authorizationHeader) {
        sessionService.logout(authorizationHeader);
    }

    public AuthDtos.OtpResponse requestOrderOtp(String authorizationHeader) {
        UserAccount userAccount = sessionService.requireRole(authorizationHeader, Role.CUSTOMER);
        return otpService.generate(
            userAccount,
            OtpPurpose.ORDER_CONFIRMATION,
            "OTP generated for order confirmation. In this dev build the code is returned in the response."
        );
    }

    public AuthDtos.OtpResponse requestPasswordResetOtp(AuthDtos.PasswordResetOtpRequest request) {
        UserAccount userAccount = findCustomerByCode(request.customerCode());
        String securityKey = requireText(request.securityKey(), "Security key is required.");
        if (!userAccount.getSecurityKey().equals(securityKey)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid security key.");
        }

        return otpService.generate(
            userAccount,
            OtpPurpose.PASSWORD_RESET,
            "OTP generated for password reset. In this dev build the code is returned in the response."
        );
    }

    @Transactional
    public void resetPassword(AuthDtos.PasswordResetRequest request) {
        UserAccount userAccount = findCustomerByCode(request.customerCode());
        String securityKey = requireText(request.securityKey(), "Security key is required.");
        String newPassword = requireText(request.newPassword(), "New password is required.");

        if (!userAccount.getSecurityKey().equals(securityKey)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid security key.");
        }
        validatePassword(newPassword);

        boolean validOtp = otpService.verify(userAccount, OtpPurpose.PASSWORD_RESET, request.otpCode());
        if (!validOtp) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Wrong or expired OTP.");
        }

        userAccount.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.save(userAccount);
    }

    public AuthDtos.UserProfile toUserProfile(UserAccount userAccount) {
        return new AuthDtos.UserProfile(
            userAccount.getId(),
            userAccount.getCustomerCode(),
            userAccount.getDisplayName(),
            userAccount.getPhone(),
            userAccount.getRole(),
            userAccount.getSignupDate()
        );
    }

    private UserAccount findCustomerByCode(String customerCode) {
        String normalizedCode = requireText(customerCode, "Customer ID is required.").toUpperCase();
        return userAccountRepository.findByCustomerCodeIgnoreCase(normalizedCode)
            .filter(account -> account.getRole() == Role.CUSTOMER)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer account not found."));
    }

    private String normalizePhone(String phone) {
        String normalized = requireText(phone, "Phone number is required.");
        if (!normalized.matches("\\d{10}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Phone number must contain exactly 10 digits.");
        }
        return normalized;
    }

    private void validatePassword(String password) {
        if (password.length() < 6) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters long.");
        }
        if (!password.matches(".*[A-Z].*")
            || !password.matches(".*[a-z].*")
            || !password.matches(".*\\d.*")
            || password.matches("[A-Za-z0-9]*")) {
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "Password must include uppercase, lowercase, digit, and special characters."
            );
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }
}
