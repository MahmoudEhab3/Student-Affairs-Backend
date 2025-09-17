package com.unilink.controller;
import java.util.Map;

import com.unilink.dto.*;
import org.springframework.web.bind.annotation.*;

import com.unilink.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
 @PostMapping("/student/signup")
public Map<String, String> signupStudent(@Valid @RequestBody SignupRequest req) {
    String token = authService.signupStudentAndReturnToken(req);
    return Map.of("token", token, "role", "STUDENT");
}


   @PostMapping("/student/login")
public Map<String, String> loginStudent(@Valid @RequestBody LoginRequest req) {
    String token = authService.loginStudent(req);
    return Map.of("token", token, "role", "STUDENT");
}

@PostMapping("/staff/login")
public Map<String, String> loginStaff(@Valid @RequestBody LoginRequest req) {
    String token = authService.loginStaff(req);
    return Map.of("token", token, "role", "STAFF");
}

@PostMapping("/student/change-password")
public Map<String, String> changeStudentPassword(@Valid @RequestBody ChangePasswordRequest req) {
    String msg = authService.changeStudentPassword(req);
    return Map.of("message", msg);
}

@PostMapping("/staff/change-password")
public Map<String, String> changeStaffPassword(@Valid @RequestBody ChangePasswordRequest req) {
    String msg = authService.changeStaffPassword(req);
    return Map.of("message", msg);
}
    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        String message = authService.generatePasswordResetToken(req.getEmail());
        return Map.of("message", message);
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        String message = authService.resetPassword(req.getToken(), req.getNewPassword());
        return Map.of("message", message);
    }
    @GetMapping("/reset-password")
    public Map<String, String> resetPasswordGet(@RequestParam String token, @RequestParam String newPassword) {
        String message = authService.resetPassword(token, newPassword);
        return Map.of("message", message);
    }
}

