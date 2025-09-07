package com.unilink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unilink.dto.LoginRequest;
import com.unilink.dto.SignupRequest;
import com.unilink.entity.Student;
import com.unilink.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.unilink.dto.ChangePasswordRequest;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        studentRepository.deleteAll();
    }


    @Test
    void signupStudent_success() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("ali");
        signupRequest.setEmail("ali@test.com");
        signupRequest.setPassword("123456");
        signupRequest.setFaculty("CS");
        signupRequest.setDepartment("AI");
        signupRequest.setPhoneNumber("01012345678");
        signupRequest.setAddress("Cairo");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("STUDENT"));

        Optional<Student> saved = studentRepository.findByEmail("ali@test.com");
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("ali");
    }


    @Test
    void signupStudent_missingName_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("");
        signupRequest.setEmail("ahmed@test.com");
        signupRequest.setPassword("123456");
        signupRequest.setFaculty("CS");
        signupRequest.setDepartment("AI");
        signupRequest.setPhoneNumber("0109999999");
        signupRequest.setAddress("Cairo");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void signupStudent_missingName2_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("ahmed@test.com");
        signupRequest.setPassword("123456");
        signupRequest.setFaculty("CS");
        signupRequest.setDepartment("AI");
        signupRequest.setPhoneNumber("0109999999");
        signupRequest.setAddress("Cairo");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void signupStudent_missingEmail_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Ahmed");
        signupRequest.setPassword("123456");
        signupRequest.setFaculty("CS");
        signupRequest.setDepartment("AI");
        signupRequest.setPhoneNumber("0109999999");
        signupRequest.setAddress("Cairo");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void signupStudent_wrongEmailFormat_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Ahmed");
        signupRequest.setEmail("ahmed-rabiebla bla");
        signupRequest.setPassword("123456");
        signupRequest.setFaculty("CS");
        signupRequest.setDepartment("AI");
        signupRequest.setPhoneNumber("0109999999");
        signupRequest.setAddress("Cairo");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                        .andExpect(status().isBadRequest());
    }


    @Test
    void signupStudent_missingPassword_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Ahmed");
        signupRequest.setEmail("ahmed@test.com");
        signupRequest.setFaculty("CS");
        signupRequest.setDepartment("AI");
        signupRequest.setPhoneNumber("0109999999");
        signupRequest.setAddress("Cairo");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                        .andExpect(status().isBadRequest());
    }


    @Test
    void signupStudent_PasswordLessThan6_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Ahmed");
        signupRequest.setPassword("12345");
        signupRequest.setEmail("ahmed@test.com");
        signupRequest.setFaculty("CS");
        signupRequest.setDepartment("AI");
        signupRequest.setPhoneNumber("0109999999");
        signupRequest.setAddress("Cairo");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                        .andExpect(status().isBadRequest());
    }


    @Test
    void signupStudent_missingDob_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Ahmed");
        signupRequest.setPassword("123456");
        signupRequest.setEmail("ahmed@test.com");
        signupRequest.setFaculty("CS");
        signupRequest.setDepartment("AI");
        signupRequest.setPhoneNumber("0109999999");
        signupRequest.setAddress("Cairo");
        signupRequest.setGender("Male");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                        .andExpect(status().isBadRequest());
    }


    @Test
    void signupStudent_missingGender_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Ahmed");
        signupRequest.setPassword("123456");
        signupRequest.setEmail("ahmed@test.com");
        signupRequest.setFaculty("CS");
        signupRequest.setDepartment("AI");
        signupRequest.setPhoneNumber("0109999999");
        signupRequest.setAddress("Cairo");
        signupRequest.setDob("2004-11-06");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                        .andExpect(status().isBadRequest());
    }

    @Test
    void signupStudent_missingPhoneNumber_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Ahmed");
        signupRequest.setPassword("123456");
        signupRequest.setEmail("ahmed@test.com");
        signupRequest.setFaculty("CS");
        signupRequest.setDepartment("AI");
//        signupRequest.setPhoneNumber("0109999999");
        signupRequest.setAddress("Cairo");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                        .andExpect(status().isBadRequest());
    }

    @Test
    void signupStudent_missingDepartment_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Ahmed");
        signupRequest.setPassword("123456");
        signupRequest.setEmail("ahmed@test.com");
        signupRequest.setFaculty("CS");
//        signupRequest.setDepartment("AI");
        signupRequest.setPhoneNumber("0109999999");
        signupRequest.setAddress("Cairo");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                        .andExpect(status().isBadRequest());
    }

    @Test
    void signupStudent_missingFaculty_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Ahmed");
        signupRequest.setPassword("123456");
        signupRequest.setEmail("ahmed@test.com");
//        signupRequest.setFaculty("CS");
        signupRequest.setDepartment("AI");
        signupRequest.setPhoneNumber("0109999999");
        signupRequest.setAddress("Cairo");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                        .andExpect(status().isBadRequest());
    }


    @Test
    void signupStudent_missingAddress_shouldReturnBadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Ahmed");
        signupRequest.setPassword("123456");
        signupRequest.setEmail("ahmed@test.com");
        signupRequest.setFaculty("CS");
        signupRequest.setDepartment("AI");
        signupRequest.setPhoneNumber("0109999999");
//        signupRequest.setAddress("Cairo");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                        .andExpect(status().isBadRequest());
    }


    @Test
    void signupThenLoginStudent_success() throws Exception {

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("User");
        signupRequest.setEmail("login@test.com");
        signupRequest.setPassword("123456");
        signupRequest.setFaculty("Engineering");
        signupRequest.setDepartment("CS");
        signupRequest.setPhoneNumber("01012341234");
        signupRequest.setAddress("Alex");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");


        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").exists())
                        .andExpect(jsonPath("$.role").value("STUDENT"));


        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@test.com");
        loginRequest.setPassword("123456");

        mockMvc.perform(post("/api/auth/student/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").exists())
                        .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void signupThenLoginStudent_missingEmail_BadRequest() throws Exception {

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("User");
        signupRequest.setEmail("login@test.com");
        signupRequest.setPassword("123456");
        signupRequest.setFaculty("Engineering");
        signupRequest.setDepartment("CS");
        signupRequest.setPhoneNumber("01012341234");
        signupRequest.setAddress("Alex");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");


        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("STUDENT"));


        LoginRequest loginRequest = new LoginRequest();
//        loginRequest.setEmail("login@test.com");
        loginRequest.setPassword("123456");

        mockMvc.perform(post("/api/auth/student/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void signupThenLoginStudent_missingPassWord_BadRequest() throws Exception {

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("User");
        signupRequest.setEmail("login@test.com");
        signupRequest.setPassword("123456");
        signupRequest.setFaculty("Engineering");
        signupRequest.setDepartment("CS");
        signupRequest.setPhoneNumber("01012341234");
        signupRequest.setAddress("Alex");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");


        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("STUDENT"));


        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@test.com");
//        loginRequest.setPassword("123456");

        mockMvc.perform(post("/api/auth/student/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void changePassword_success() throws Exception {
        // First, create a student
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Test User");
        signupRequest.setEmail("change@test.com");
        signupRequest.setPassword("oldPassword123");
        signupRequest.setFaculty("CS");
        signupRequest.setDepartment("AI");
        signupRequest.setPhoneNumber("01012345678");
        signupRequest.setAddress("Cairo");
        signupRequest.setGender("Male");
        signupRequest.setDob("2004-11-06");

        mockMvc.perform(post("/api/auth/student/signup")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Now test change password
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setEmail("change@test.com");
        changePasswordRequest.setOldPassword("oldPassword123");
        changePasswordRequest.setNewPassword("newPassword123");

        mockMvc.perform(post("/api/auth/student/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_missingEmail_shouldReturnBadRequest() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        // Email is not set
        changePasswordRequest.setOldPassword("oldPassword123");
        changePasswordRequest.setNewPassword("newPassword123");

        mockMvc.perform(post("/api/auth/student/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_emptyEmail_shouldReturnBadRequest() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setEmail("");  // Empty email
        changePasswordRequest.setOldPassword("oldPassword123");
        changePasswordRequest.setNewPassword("newPassword123");

        mockMvc.perform(post("/api/auth/student/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_invalidEmailFormat_shouldReturnBadRequest() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setEmail("invalid-email-format");  // Invalid email format
        changePasswordRequest.setOldPassword("oldPassword123");
        changePasswordRequest.setNewPassword("newPassword123");

        mockMvc.perform(post("/api/auth/student/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_missingOldPassword_shouldReturnBadRequest() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setEmail("test@test.com");
        // Old password is not set
        changePasswordRequest.setNewPassword("newPassword123");

        mockMvc.perform(post("/api/auth/student/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_emptyOldPassword_shouldReturnBadRequest() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setEmail("test@test.com");
        changePasswordRequest.setOldPassword("");  // Empty old password
        changePasswordRequest.setNewPassword("newPassword123");

        mockMvc.perform(post("/api/auth/student/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_missingNewPassword_shouldReturnBadRequest() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setEmail("test@test.com");
        changePasswordRequest.setOldPassword("oldPassword123");
        // New password is not set

        mockMvc.perform(post("/api/auth/student/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_emptyNewPassword_shouldReturnBadRequest() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setEmail("test@test.com");
        changePasswordRequest.setOldPassword("oldPassword123");
        changePasswordRequest.setNewPassword("");  // Empty new password

        mockMvc.perform(post("/api/auth/student/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_allFieldsEmpty_shouldReturnBadRequest() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setEmail("");
        changePasswordRequest.setOldPassword("");
        changePasswordRequest.setNewPassword("");

        mockMvc.perform(post("/api/auth/student/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_whitespaceEmail_shouldReturnBadRequest() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setEmail("   ");  // Whitespace only
        changePasswordRequest.setOldPassword("oldPassword123");
        changePasswordRequest.setNewPassword("newPassword123");

        mockMvc.perform(post("/api/auth/student/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest());
    }

}