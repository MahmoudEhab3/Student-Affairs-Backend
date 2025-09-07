package com.unilink.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unilink.entity.Student;
import com.unilink.repository.StudentRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository repository;

    public Integer getStudentIdByEmail(String email) {
        return repository.findByEmail(email)
                         .map(Student::getStudentID)
                         .orElseThrow(() -> new RuntimeException("Student not found"));
    }
}
