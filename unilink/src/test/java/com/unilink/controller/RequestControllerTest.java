package com.unilink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unilink.config.JwtUtil;
import com.unilink.dto.RequestDTO;
import com.unilink.entity.Request;
import com.unilink.entity.Student;
import com.unilink.entity.Staff;
import com.unilink.repository.RequestRepository;
import com.unilink.repository.StudentRepository;
import com.unilink.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Integer otherStudentId;

    private String staffToken;
    private String studentToken;
    private Integer studentId;
    private Integer staffId;

    @BeforeEach
    void setup() {
        requestRepository.deleteAll();
        studentRepository.deleteAll();
        staffRepository.deleteAll();


        Student student = new Student();
        student.setName("Test Student");
        student.setEmail("student@test.com");
        student.setPassword(passwordEncoder.encode("password123"));
        student.setFaculty("CS");
        student.setDepartment("AI");
        student.setPhoneNumber("01012345678");
        student.setAddress("Cairo");
        student.setGender(Student.Gender.valueOf("Male"));
        student.setDob(LocalDate.parse("2000-01-01"));
        student = studentRepository.save(student);
        studentId = student.getStudentID();
        studentToken = jwtUtil.generateToken("student@test.com", "STUDENT", studentId);


        Student otherStudent = new Student();
        otherStudent.setName("Other Student");
        otherStudent.setEmail("other@test.com");
        otherStudent.setPassword(passwordEncoder.encode("password123"));
        otherStudent.setFaculty("Engineering");
        otherStudent.setDepartment("CS");
        otherStudent.setPhoneNumber("01098765432");
        otherStudent.setAddress("Alexandria");
        otherStudent.setGender(Student.Gender.valueOf("Female"));
        otherStudent.setDob(LocalDate.parse("2001-01-01"));
        otherStudent = studentRepository.save(otherStudent);
        otherStudentId = otherStudent.getStudentID();


        Staff staff = new Staff();
        staff.setName("Test Staff");
        staff.setEmail("staff@test.com");
        staff.setPassword(passwordEncoder.encode("password123"));
        staff.setRole("ADMIN");
        staff = staffRepository.save(staff);
        staffId = staff.getStaffID();
        staffToken = jwtUtil.generateToken("staff@test.com", "STAFF", null);
    }



    @Test
    void createRequest_staffUser_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "document",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        mockMvc.perform(multipart("/api/requests")
                        .file(file)
                        .param("title", "Test Request")
                        .param("studentID", studentId.toString())
                        .param("type", "Academic")
                        .param("description", "Test description")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Request"))
                .andExpect(jsonPath("$.studentID").value(studentId))
                .andExpect(jsonPath("$.type").value("Academic"))
                .andExpect(jsonPath("$.status").value("Pending"));

        assertThat(requestRepository.count()).isEqualTo(1);
    }

    @Test
    void createRequest_studentUser_forbidden() throws Exception {
        mockMvc.perform(multipart("/api/requests")
                        .param("title", "Test Request")
                        .param("studentID", studentId.toString())
                        .param("type", "Academic")
                        .param("description", "Test description")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only staff can create requests."));
    }

    @Test
    void createRequest_noAuth_unauthorized() throws Exception {
        mockMvc.perform(multipart("/api/requests")
                        .param("title", "Test Request")
                        .param("studentID", studentId.toString())
                        .param("type", "Academic")
                        .param("description", "Test description"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createRequest_invalidFileFormat_badRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "document",
                "test.txt",
                "text/plain",
                "Text content".getBytes()
        );

        mockMvc.perform(multipart("/api/requests")
                        .file(file)
                        .param("title", "Test Request")
                        .param("studentID", studentId.toString())
                        .param("type", "Academic")
                        .param("description", "Test description")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only PDF, JPG, and PNG files are allowed."));
    }

    @Test
    void createRequest_fileTooLarge_badRequest() throws Exception {
        byte[] largeContent = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "document",
                "large.pdf",
                "application/pdf",
                largeContent
        );

        mockMvc.perform(multipart("/api/requests")
                        .file(file)
                        .param("title", "Test Request")
                        .param("studentID", studentId.toString())
                        .param("type", "Academic")
                        .param("description", "Test description")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File size must not exceed 5 MB."));
    }

    @Test
    void createRequest_withoutDocument_success() throws Exception {
        mockMvc.perform(multipart("/api/requests")
                        .param("title", "Test Request")
                        .param("studentID", studentId.toString())
                        .param("type", "Academic")
                        .param("description", "Test description")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isCreated());
    }


    @Test
    void getAllRequests_asStaff_returnsAllRequests() throws Exception {
        // Create multiple requests for different students
        createTestRequest(studentId, "Request 1");
        createTestRequest(otherStudentId, "Request 2"); // Different student

        mockMvc.perform(get("/api/requests")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllRequests_asStudent_returnsOnlyOwnRequests() throws Exception {

        createTestRequest(studentId, "My Request");
        createTestRequest(otherStudentId, "Other Request");

        mockMvc.perform(get("/api/requests")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("My Request"));
    }

    @Test
    void getAllRequests_noAuth_unauthorized() throws Exception {
        mockMvc.perform(get("/api/requests"))
                .andExpect(status().isForbidden());
    }


    @Test
    void getRequestById_staffCanViewAny() throws Exception {
        Request request = createTestRequest(otherStudentId, "Other Student Request");

        mockMvc.perform(get("/api/requests/{id}", request.getRequestID())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Other Student Request"));
    }


    @Test
    void getRequestById_studentCanViewOwnOnly() throws Exception {
        Request request = createTestRequest(studentId, "My Request");

        mockMvc.perform(get("/api/requests/{id}", request.getRequestID())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("My Request"));
    }

    @Test
    void getRequestById_studentViewingOthers_forbidden() throws Exception {
        Request request = createTestRequest(otherStudentId, "Other Student Request");

        mockMvc.perform(get("/api/requests/{id}", request.getRequestID())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You can only view your own requests."));
    }

    @Test
    void getRequestById_nonExistent_notFound() throws Exception {
        mockMvc.perform(get("/api/requests/{id}", 99999)
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNotFound());
    }


    @Test
    void updateRequest_staffOnly_success() throws Exception {
        Request request = createTestRequest(studentId, "Original Title");

        RequestDTO updateDto = new RequestDTO();
        updateDto.setTitle("Updated Title");
        updateDto.setType("Administrative");
        updateDto.setDescription("Updated description");
        updateDto.setStudentID(studentId);

        mockMvc.perform(put("/api/requests/{id}", request.getRequestID())
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.type").value("Administrative"));
    }

    @Test
    void updateRequest_asStudent_forbidden() throws Exception {
        Request request = createTestRequest(studentId, "Original Title");

        RequestDTO updateDto = new RequestDTO();
        updateDto.setTitle("Updated Title");
        updateDto.setType("Administrative");
        updateDto.setDescription("Updated description");
        updateDto.setStudentID(studentId);

        mockMvc.perform(put("/api/requests/{id}", request.getRequestID())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only staff can update requests."));
    }

    @Test
    void updateRequest_nonExistent_notFound() throws Exception {
        RequestDTO updateDto = new RequestDTO();
        updateDto.setTitle("Updated Title");
        updateDto.setType("Administrative");
        updateDto.setDescription("Updated description");
        updateDto.setStudentID(studentId);

        mockMvc.perform(put("/api/requests/{id}", 99999)
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }


    @Test
    void deleteRequest_staffOnly_success() throws Exception {
        Request request = createTestRequest(studentId, "To Delete");

        mockMvc.perform(delete("/api/requests/{id}", request.getRequestID())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNoContent());

        assertThat(requestRepository.existsById(request.getRequestID())).isFalse();
    }

    @Test
    void deleteRequest_asStudent_forbidden() throws Exception {
        Request request = createTestRequest(studentId, "To Delete");

        mockMvc.perform(delete("/api/requests/{id}", request.getRequestID())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only staff can delete requests."));
    }

    @Test
    void deleteRequest_nonExistent_notFound() throws Exception {
        mockMvc.perform(delete("/api/requests/{id}", 99999)
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRequest_withJpgFile_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "document",
                "test.jpg",
                "image/jpeg",
                "JPG content".getBytes()
        );

        mockMvc.perform(multipart("/api/requests")
                        .file(file)
                        .param("title", "Test Request")
                        .param("studentID", studentId.toString())
                        .param("type", "Academic")
                        .param("description", "Test description")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isCreated());
    }

    @Test
    void createRequest_withPngFile_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "document",
                "test.png",
                "image/png",
                "PNG content".getBytes()
        );

        mockMvc.perform(multipart("/api/requests")
                        .file(file)
                        .param("title", "Test Request")
                        .param("studentID", studentId.toString())
                        .param("type", "Academic")
                        .param("description", "Test description")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isCreated());
    }

    private Request createTestRequest(Integer studentId, String title) {
        Request request = new Request();
        request.setTitle(title);
        request.setStudentID(studentId);
        request.setType("Academic");
        request.setDescription("Test description");
        request.setStatus(Request.Status.Pending);
        return requestRepository.save(request);
    }
    private Student createAnotherStudent(String email, String name) {
        Student student = new Student();
        student.setName(name);
        student.setEmail(email);
        student.setPassword(passwordEncoder.encode("password123"));
        student.setFaculty("CS");
        student.setDepartment("AI");
        student.setPhoneNumber("01012345679");
        student.setAddress("Cairo");
        student.setGender(Student.Gender.valueOf("Female"));
        student.setDob(LocalDate.parse("2000-01-01"));
        return studentRepository.save(student);
    }
}