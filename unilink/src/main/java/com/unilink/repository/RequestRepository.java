package com.unilink.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unilink.entity.Request;

public interface RequestRepository extends JpaRepository<Request, Integer> {
    List<Request> findByStudentID(Integer studentID);
}
