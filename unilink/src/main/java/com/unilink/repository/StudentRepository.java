package com.unilink.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unilink.entity.Student;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByEmail(String email);
}
