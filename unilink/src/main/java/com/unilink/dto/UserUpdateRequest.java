package com.unilink.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String name;
    private String email;
    private String phone;
    private String department;



    // Staff-specific fields
    private String position;
    private String staffId;
}