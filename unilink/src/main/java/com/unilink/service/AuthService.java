package com.unilink.service;

import java.time.LocalDate;
import java.util.Optional;

import com.unilink.dto.UserUpdateRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.unilink.config.JwtUtil;
import com.unilink.dto.ChangePasswordRequest;
import com.unilink.dto.LoginRequest;
import com.unilink.dto.SignupRequest;
import com.unilink.entity.Staff;
import com.unilink.entity.Student;
import com.unilink.repository.StaffRepository;
import com.unilink.repository.StudentRepository;

@Service
public class AuthService {
    private final StudentRepository studentRepo;
    private final StaffRepository staffRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService; // Add EmailService

    public AuthService(StudentRepository studentRepo, StaffRepository staffRepo,
                       JwtUtil jwtUtil, EmailService emailService) {
        this.studentRepo = studentRepo;
        this.staffRepo = staffRepo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.emailService = emailService; // Initialize EmailService
    }

    // ✅ Student Signup -> returns JWT (with studentID in token)
    public String signupStudentAndReturnToken(SignupRequest req) {
        if (studentRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Student student = new Student();
        student.setName(req.getName());
        student.setEmail(req.getEmail());
        student.setPassword(passwordEncoder.encode(req.getPassword()));
        student.setFaculty(req.getFaculty());
        student.setDepartment(req.getDepartment());
        student.setPhoneNumber(req.getPhoneNumber());
        student.setAddress(req.getAddress());
        student.setGender(Student.Gender.valueOf(req.getGender()));

        // parse DOB safely
        if (req.getDob() != null && !req.getDob().isEmpty()) {
            student.setDob(LocalDate.parse(req.getDob())); // expects "YYYY-MM-DD"
        }

        studentRepo.save(student);

        // return JWT for new student (with studentID included)
        return jwtUtil.generateToken(student.getEmail(), "STUDENT", student.getId(), null);
    }

    // ✅ Student Login -> returns JWT (with studentID in token)
    public String loginStudent(LoginRequest req) {
        Student student = studentRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (!passwordEncoder.matches(req.getPassword(), student.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return jwtUtil.generateToken(student.getEmail(), "STUDENT", student.getId(), null);
    }

    // ✅ Staff Login -> returns JWT (with staffID in token)
    public String loginStaff(LoginRequest req) {
        Staff staff = staffRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        if (!passwordEncoder.matches(req.getPassword(), staff.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return jwtUtil.generateToken(staff.getEmail(), staff.getRole(), null, staff.getStaffID());
    }

    public String changeStudentPassword(ChangePasswordRequest req) {
        Student student = studentRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!passwordEncoder.matches(req.getOldPassword(), student.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        student.setPassword(passwordEncoder.encode(req.getNewPassword()));
        studentRepo.save(student);

        return "Password updated successfully!";
    }

    public String changeStaffPassword(ChangePasswordRequest req) {
        Staff staff = staffRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        if (!passwordEncoder.matches(req.getOldPassword(), staff.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        staff.setPassword(passwordEncoder.encode(req.getNewPassword()));
        staffRepo.save(staff);

        return "Password updated successfully!";
    }
    // ✅ Update staff profile (fixed version)
    public Staff updateStaff(Integer staffId, UserUpdateRequest updateRequest) {
        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        if (updateRequest.getName() != null) {
            staff.setName(updateRequest.getName());
        }
        if (updateRequest.getEmail() != null) {
            // Check if email is already taken by another staff
            if (staffRepo.existsByEmailAndIdNot(updateRequest.getEmail(), staffId)) {
                throw new RuntimeException("Email is already in use by another staff member");
            }
            staff.setEmail(updateRequest.getEmail());
        }


        Staff updatedStaff = staffRepo.save(staff);

        // Send email notification
        String emailMessage = String.format(
                "Your profile has been successfully updated.\n\n" +
                        "Name: %s\nEmail: %s\nPhone: %s\nDepartment: %s\nPosition: %s",
                updatedStaff.getName(), updatedStaff.getEmail()
        );
        emailService.sendEmail(updatedStaff.getEmail(), "Profile Updated", emailMessage);

        return updatedStaff;
    }

    // ✅ Get staff by ID
    public Optional<Staff> getStaffById(Integer staffId) {
        return staffRepo.findById(staffId);
    }
}
