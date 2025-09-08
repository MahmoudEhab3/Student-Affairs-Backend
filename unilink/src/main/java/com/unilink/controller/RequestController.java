package com.unilink.controller;

import com.unilink.config.JwtUtil;
import com.unilink.dto.RequestDTO;
import com.unilink.dto.RequestResponseDTO;
import com.unilink.entity.Request;
import com.unilink.service.RequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    @Autowired
    private RequestService service;

    @Autowired
    private JwtUtil jwtUtil;

// ✅ Staff and Students: Create request
@PostMapping(consumes = {"multipart/form-data"})
public ResponseEntity<?> createRequest(
        HttpServletRequest request,
        @RequestParam String title,
        @RequestParam(required = false) Integer studentID, // staff can pass it, student ignored
        @RequestParam String type,
        @RequestParam String description,
        @RequestParam(required = false) MultipartFile document) throws IOException {

    String role = getRoleFromRequest(request);
    String token = extractToken(request);

    Integer effectiveStudentId;
    if ("STUDENT".equals(role)) {
        // Student can only create request for themselves
        effectiveStudentId = jwtUtil.getStudentIdFromToken(token);
    } else if ("STAFF".equals(role)) {
        // Staff must provide studentID
        if (studentID == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Staff must provide a studentID when creating a request.");
        }
        effectiveStudentId = studentID;
    } else {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only staff and students can create requests.");
    }

    // 🔹 File validation
    if (document != null && !document.isEmpty()) {
        String contentType = document.getContentType();
        long fileSize = document.getSize();

        // Allowed formats: PDF, JPG, PNG
        if (!(contentType.equalsIgnoreCase("application/pdf")
                || contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/png"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Only PDF, JPG, and PNG files are allowed.");
        }

        // Max size 5MB
        if (fileSize > (5 * 1024 * 1024)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("File size must not exceed 5 MB.");
        }
    }

    RequestDTO dto = new RequestDTO();
    dto.setTitle(title);
    dto.setStudentID(effectiveStudentId);
    dto.setType(type);
    dto.setDescription(description);
    if (document != null && !document.isEmpty()) {
        dto.setDocument(document.getBytes());
    }

    Request savedRequest = service.createRequest(dto);
    return new ResponseEntity<>(savedRequest, HttpStatus.CREATED);
}

    // ✅ Students: only their own requests; Staff: all requests
    @GetMapping
    public ResponseEntity<?> getAllRequests(HttpServletRequest request) {
        String role = getRoleFromRequest(request);
        String token = extractToken(request);

        if ("STUDENT".equals(role)) {
            Integer studentId = jwtUtil.getStudentIdFromToken(token);
            List<RequestResponseDTO> list = service.getRequestsByStudentId(studentId);
            return ResponseEntity.ok(list);
        }

        List<RequestResponseDTO> list = service.getAllRequests();
        return ResponseEntity.ok(list);
    }

    // ✅ Students: only their own request; Staff: any
    @GetMapping("/{id}")
    public ResponseEntity<?> getRequestById(@PathVariable Integer id, HttpServletRequest request) {
        String role = getRoleFromRequest(request);
        String token = extractToken(request);

        return service.getRequestById(id).map(req -> {
            if ("STUDENT".equals(role)) {
                Integer studentId = jwtUtil.getStudentIdFromToken(token);
                if (!req.getStudentID().equals(studentId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only view your own requests.");
                }
            }
            return ResponseEntity.ok(req);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ✅ Staff only: update
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRequest(
            @PathVariable Integer id,
            @RequestBody RequestDTO dto,
            HttpServletRequest request) {

        String role = getRoleFromRequest(request);
        if (!"STAFF".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only staff can update requests.");
        }

        return service.updateRequest(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Staff only: delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequest(@PathVariable Integer id, HttpServletRequest request) {
        String role = getRoleFromRequest(request);
        if (!"STAFF".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only staff can delete requests.");
        }

        return service.deleteRequest(id) ?
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
