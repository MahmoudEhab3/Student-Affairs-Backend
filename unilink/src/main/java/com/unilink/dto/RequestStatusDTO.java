package com.unilink.dto;

import com.unilink.entity.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestStatusDTO {
    @NotNull(message = "Status is required")
    private Request.Status status;

    @Size(max = 255, message = "Comment cannot exceed 255 characters")
    private String comment;
}
