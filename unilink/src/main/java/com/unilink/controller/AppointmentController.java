package com.unilink.controller;

import com.unilink.config.JwtUtil;
import com.unilink.dto.AppointmentDTO;
import com.unilink.entity.Appointment;
import com.unilink.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService service;
    private final JwtUtil jwtUtil;

    public AppointmentController(AppointmentService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

// ✅ Create appointment
@PostMapping
public ResponseEntity<?> createAppointment(@Valid @RequestBody AppointmentDTO dto, HttpServletRequest request) {
    String role = getRoleFromRequest(request);
    String token = extractToken(request);

    if ("STUDENT".equals(role)) {
        Integer studentId = jwtUtil.getStudentIdFromToken(token);
        dto.setStudentID(studentId);
    }

    if (!service.staffExists(dto.getStaffID())) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Staff with ID " + dto.getStaffID() + " does not exist"));
    }

    Appointment saved = service.createAppointment(dto);
    return new ResponseEntity<>(saved, HttpStatus.CREATED);
}


    // ✅ Students: only own; Staff: all
    @GetMapping
    public ResponseEntity<?> getAppointments(HttpServletRequest request) {
        String role = getRoleFromRequest(request);
        String token = extractToken(request);

        if ("STUDENT".equals(role)) {
            Integer studentId = jwtUtil.getStudentIdFromToken(token);
            return ResponseEntity.ok(service.getAppointmentsByStudent(studentId));
        }

        return ResponseEntity.ok(service.getAllAppointments());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable Integer id,
                                            @Valid @RequestBody AppointmentDTO dto,
                                            HttpServletRequest request) {
        String role = getRoleFromRequest(request);
        String token = extractToken(request);

        Appointment appt = service.getAppointmentById(id).orElse(null);
        if (appt == null) return ResponseEntity.notFound().build();

        if ("STUDENT".equals(role)) {
            Integer studentId = jwtUtil.getStudentIdFromToken(token);

            if (!appt.getStudentID().equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only reschedule your own appointments.");
            }

            // 🚫 Students cannot change status
            dto.setStatus(appt.getStatus());
        }

        return service.updateAppointment(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Cancel/Delete appointment
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Integer id, HttpServletRequest request) {
        String role = getRoleFromRequest(request);
        String token = extractToken(request);

        if ("STUDENT".equals(role)) {
            Integer studentId = jwtUtil.getStudentIdFromToken(token);
            Appointment appt = service.getAppointmentById(id).orElse(null);
            if (appt == null) return ResponseEntity.notFound().build();

            if (!appt.getStudentID().equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only cancel your own appointments.");
            }
        }

        return service.deleteAppointment(id) ?
                ResponseEntity.noContent().build() :
                ResponseEntity.notFound().build();
    }

    // --- 🔹 Helpers ---
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
    }

    private String getRoleFromRequest(HttpServletRequest request) {
        String token = extractToken(request);
        return (token != null) ? jwtUtil.getRoleFromToken(token) : null;
    }
}
