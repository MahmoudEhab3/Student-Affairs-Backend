package com.unilink.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unilink.entity.Staff;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Integer> {
    Optional<Staff> findByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Integer id);
}
