package com.laundrymgmt.modern.controller;

import com.laundrymgmt.modern.dto.AuthDtos;
import com.laundrymgmt.modern.dto.CommonDtos;
import com.laundrymgmt.modern.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/signup")
    public AuthDtos.AuthResponse signup(@RequestBody AuthDtos.SignupRequest request) {
        return authService.signup(request);
    }

    @GetMapping("/me")
    public AuthDtos.UserProfile me(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return authService.me(authorizationHeader);
    }

    @PostMapping("/logout")
    public CommonDtos.MessageResponse logout(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        authService.logout(authorizationHeader);
        return new CommonDtos.MessageResponse("Logged out.");
    }

    @PostMapping("/request-order-otp")
    public AuthDtos.OtpResponse requestOrderOtp(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return authService.requestOrderOtp(authorizationHeader);
    }

    @PostMapping("/request-password-reset-otp")
    public AuthDtos.OtpResponse requestPasswordResetOtp(@RequestBody AuthDtos.PasswordResetOtpRequest request) {
        return authService.requestPasswordResetOtp(request);
    }

    @PostMapping("/reset-password")
    public CommonDtos.MessageResponse resetPassword(@RequestBody AuthDtos.PasswordResetRequest request) {
        authService.resetPassword(request);
        return new CommonDtos.MessageResponse("Password changed successfully.");
    }
}
