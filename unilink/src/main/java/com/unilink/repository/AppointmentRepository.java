package com.unilink.repository;

import com.unilink.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByStudentID(Integer studentID);

    Optional<Appointment> findByDateAndTime(LocalDate date, LocalTime time);
}
