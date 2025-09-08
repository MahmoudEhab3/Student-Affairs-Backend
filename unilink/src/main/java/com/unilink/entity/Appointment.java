package com.unilink.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ApptID")
    private Integer apptID;

    @Column(name = "StudentID", nullable = false)
    private Integer studentID;

    @Column(name = "StaffID", nullable = false)
    private Integer staffID;

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Column(name = "Time", nullable = false)
    private LocalTime time;

    @Column(name = "Purpose", columnDefinition = "TEXT")
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private Status status = Status.Scheduled;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status {
        Scheduled, Completed, Cancelled, No_Show
    }
}
