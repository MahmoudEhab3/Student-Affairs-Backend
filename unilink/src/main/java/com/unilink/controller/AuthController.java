package com.unilink.controller;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

import com.unilink.dto.ChangePasswordRequest;
import com.unilink.dto.LoginRequest;
import com.unilink.dto.SignupRequest;
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

}

