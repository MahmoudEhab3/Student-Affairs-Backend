package com.unilink.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import com.unilink.dto.UserUpdateRequest;
import org.springframework.scheduling.annotation.Scheduled;
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
import com.unilink.entity.PasswordResetToken;
import com.unilink.repository.PasswordResetTokenRepository;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {
    private final StudentRepository studentRepo;
    private final StaffRepository staffRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService; // Add EmailService
    private final PasswordResetTokenRepository tokenRepository;

    public AuthService(StudentRepository studentRepo, StaffRepository staffRepo,
                       JwtUtil jwtUtil, EmailService emailService,
                       PasswordResetTokenRepository tokenRepository) {
        this.studentRepo = studentRepo;
        this.staffRepo = staffRepo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
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
    public String generatePasswordResetToken(String email) {
        // Check if user exists (student or staff)
        Optional<Student> student = studentRepo.findByEmail(email);
        Optional<Staff> staff = staffRepo.findByEmail(email);

        if (student.isEmpty() && staff.isEmpty()) {
            // Don't reveal that the email doesn't exist for security reasons
            return "If this email is registered, you will receive a password reset link";
        }

        // Generate a unique token
        String token = UUID.randomUUID().toString();

        // Set expiry date to 1 hour from now
        LocalDateTime expiryDate = LocalDateTime.now().plus(1, ChronoUnit.HOURS);
        System.out.println("Received reset token: " + token);

        // Delete any existing tokens for this email
        tokenRepository.findByEmail(email).ifPresent(existingToken -> {
            tokenRepository.delete(existingToken);
        });

        // Create and save the token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setEmail(email);
        resetToken.setExpiryDate(expiryDate);
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        // Send email with reset link
        String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + token;
        String emailMessage = String.format(
                "You requested a password reset. Click the link below to reset your password:\n\n" +
                        "%s\n\n" +
                        "If you didn't request this, please ignore this email.\n\n" +
                        "This link will expire in 1 hour.\n\n" +
                        "Best regards,\nStudent Affairs System",
                resetLink
        );

        emailService.sendEmail(email, "Password Reset Request", emailMessage);

        return "Password reset email sent successfully";
    }

    // Reset password using token
    public String resetPassword(String token, String newPassword) {
        // Validate the token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        // Check if token is expired
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Reset token has expired");
        }

        // Check if token has already been used
        if (resetToken.getUsed()) {
            throw new RuntimeException("Reset token has already been used");
        }

        String email = resetToken.getEmail();

        // Update password for student or staff
        Optional<Student> student = studentRepo.findByEmail(email);
        if (student.isPresent()) {
            Student s = student.get();
            s.setPassword(passwordEncoder.encode(newPassword));
            studentRepo.save(s);
        } else {
            Optional<Staff> staff = staffRepo.findByEmail(email);
            if (staff.isPresent()) {
                Staff st = staff.get();
                st.setPassword(passwordEncoder.encode(newPassword));
                staffRepo.save(st);
            } else {
                throw new RuntimeException("User not found");
            }
        }

        // Mark token as used
        tokenRepository.markAsUsed(token);

        // Send confirmation email
        String emailMessage = String.format(
                "Your password has been successfully reset.\n\n" +
                        "If you didn't initiate this change, please contact support immediately.\n\n" +
                        "Best regards,\nStudent Affairs System"
        );

        emailService.sendEmail(email, "Password Reset Successful", emailMessage);

        return "Password reset successfully";
    }

    // Add a method to clean up expired tokens (call this periodically)
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void cleanupExpiredTokens() {
        tokenRepository.deleteAllExpiredSince(LocalDateTime.now());
        System.out.println("Cleaned up expired password reset tokens");
    }}