package com.unilink.controller;

import com.unilink.config.JwtUtil;
import com.unilink.dto.UserUpdateRequest;
import com.unilink.entity.Student;
import com.unilink.entity.Staff;
import com.unilink.service.StudentService;
import com.unilink.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final StudentService studentService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public ProfileController(StudentService studentService, AuthService authService, JwtUtil jwtUtil) {
        this.studentService = studentService;
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        String role = jwtUtil.getRoleFromToken(token);

        if ("STUDENT".equals(role)) {
            Integer studentId = jwtUtil.getStudentIdFromToken(token);
            return studentService.getStudentById(studentId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } else if ("STAFF".equals(role)) {
            Integer staffId = jwtUtil.getStaffIdFromToken(token);
            // Assuming AuthService has a method to get staff by ID
            return authService.getStaffById(staffId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        return ResponseEntity.badRequest().body("Unknown user role");
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody UserUpdateRequest updateRequest,
            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        String role = jwtUtil.getRoleFromToken(token);

        try {
            if ("STUDENT".equals(role)) {
                Integer studentId = jwtUtil.getStudentIdFromToken(token);
                Student updatedStudent = studentService.updateStudent(studentId, updateRequest);
                return ResponseEntity.ok(updatedStudent);
            } else if ("STAFF".equals(role)) {
                Integer staffId = jwtUtil.getStaffIdFromToken(token);
                // Assuming AuthService has a method to update staff
                Staff updatedStaff = authService.updateStaff(staffId, updateRequest);
                return ResponseEntity.ok(updatedStaff);
            }

            return ResponseEntity.badRequest().body("Unknown user role");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update profile: " + e.getMessage());
        }
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
    }
}