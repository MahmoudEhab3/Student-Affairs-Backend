package com.unilink.dto;

import com.unilink.entity.Appointment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AppointmentStatusDTO {
    @NotNull(message = "Status is required")
    private Appointment.Status status;

    @Size(max = 255, message = "Comment cannot exceed 255 characters")
    private String comment;
}
