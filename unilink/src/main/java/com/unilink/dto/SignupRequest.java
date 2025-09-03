package com.unilink.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private String faculty;
    private String department;
    private String phoneNumber;
    private String address;
    private String gender;
    private String dob; 
}
