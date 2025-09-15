package com.unilink.repository;

import com.unilink.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByEmail(String email);

    // Add this method to check if email exists for other students
    boolean existsByEmailAndIdNot(String email, Integer id);
}