package com.unilink.service;

import com.unilink.dto.UserUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unilink.entity.Student;
import com.unilink.repository.StudentRepository;

import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EmailService emailService;

    public Integer getStudentIdByEmail(String email) {
        return studentRepository.findByEmail(email)
                .map(Student::getId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public Optional<Student> getStudentById(Integer studentId) {
        return studentRepository.findById(studentId);
    }

    public Student updateStudent(Integer studentId, UserUpdateRequest updateRequest) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (updateRequest.getName() != null) {
            student.setName(updateRequest.getName());
        }
        if (updateRequest.getEmail() != null) {
            // Check if email is already taken by another student
            if (studentRepository.existsByEmailAndIdNot(updateRequest.getEmail(), studentId)) {
                throw new RuntimeException("Email is already in use by another student");
            }
            student.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPhone() != null) {
            student.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getDepartment() != null) {
            student.setDepartment(updateRequest.getDepartment());
        }


        Student updatedStudent = studentRepository.save(student);

        // Send email notification
        String emailMessage = String.format(
                "Your profile has been successfully updated.\n\n" +
                        "Name: %s\nEmail: %s\nPhone: %s\nDepartment: %s\nAcademic Year: %d\nStudent ID: %s",
                updatedStudent.getName(), updatedStudent.getEmail(), updatedStudent.getPhone(),
                updatedStudent.getDepartment(), updatedStudent.getAcademicYear(), updatedStudent.getStudentId()
        );
        emailService.sendEmail(updatedStudent.getEmail(), "Profile Updated", emailMessage);

        return updatedStudent;
    }
}