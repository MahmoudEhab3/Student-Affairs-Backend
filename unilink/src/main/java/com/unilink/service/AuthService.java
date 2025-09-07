package com.unilink.service;

import java.time.LocalDate;

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

    public AuthService(StudentRepository studentRepo, StaffRepository staffRepo, JwtUtil jwtUtil) {
        this.studentRepo = studentRepo;
        this.staffRepo = staffRepo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
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
        return jwtUtil.generateToken(student.getEmail(), "STUDENT", student.getId());
    }

    // ✅ Student Login -> returns JWT (with studentID in token)
    public String loginStudent(LoginRequest req) {
        Student student = studentRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (!passwordEncoder.matches(req.getPassword(), student.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return jwtUtil.generateToken(student.getEmail(), "STUDENT", student.getId());
    }

    // ✅ Staff Login -> returns JWT (role only, no studentID)
    public String loginStaff(LoginRequest req) {
        Staff staff = staffRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        if (!passwordEncoder.matches(req.getPassword(), staff.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return jwtUtil.generateToken(staff.getEmail(), staff.getRole(), null);
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
}
