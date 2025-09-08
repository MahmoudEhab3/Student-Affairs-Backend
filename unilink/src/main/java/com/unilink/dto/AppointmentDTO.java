package com.unilink.dto;

import com.unilink.entity.Appointment;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentDTO {
    @NotNull(message = "Student ID is required")
    private Integer studentID;

    @NotNull(message = "Staff ID is required")
    private Integer staffID;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Appointment date must be today or in the future")
    private LocalDate date;

    @NotNull(message = "Time is required")
    private LocalTime time;

    @NotBlank(message = "Purpose is required")
    @Size(max = 255, message = "Purpose cannot exceed 255 characters")
    private String purpose;

    @NotNull(message = "Status is required")
    private Appointment.Status status;
}
