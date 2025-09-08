package com.unilink.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.unilink.config.JwtUtil;
import com.unilink.dto.AppointmentDTO;
import com.unilink.entity.Appointment;
import com.unilink.entity.Student;
import com.unilink.entity.Staff;
import com.unilink.repository.AppointmentRepository;
import com.unilink.repository.StudentRepository;
import com.unilink.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper;
    private String studentToken;
    private String staffToken;
    private String anotherStudentToken;
    private Integer studentId;
    private Integer staffId;
    private Integer anotherStudentId;

    @BeforeEach
    void setup() {
        // Configure ObjectMapper to handle Java time
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());


        appointmentRepository.deleteAll();
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


        Student anotherStudent = new Student();
        anotherStudent.setName("Another Student");
        anotherStudent.setEmail("another@test.com");
        anotherStudent.setPassword(passwordEncoder.encode("password123"));
        anotherStudent.setFaculty("Engineering");
        anotherStudent.setDepartment("CS");
        anotherStudent.setPhoneNumber("01098765432");
        anotherStudent.setAddress("Alexandria");
        anotherStudent.setGender(Student.Gender.valueOf("Female"));
        anotherStudent.setDob(LocalDate.parse("2001-01-01"));
        anotherStudent = studentRepository.save(anotherStudent);
        anotherStudentId = anotherStudent.getStudentID();
        anotherStudentToken = jwtUtil.generateToken("another@test.com", "STUDENT", anotherStudentId);

        // Create test staff
        Staff staff = new Staff();
        staff.setName("Test Staff");
        staff.setEmail("staff@test.com");
        staff.setPassword(passwordEncoder.encode("password123"));
        staff.setRole("STAFF");
        staff = staffRepository.save(staff);
        staffId = staff.getStaffID();
        staffToken = jwtUtil.generateToken("staff@test.com", "STAFF", null);
    }



    @Test
    void createAppointment_studentForSelf_success() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setStudentID(studentId);

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentID").value(studentId))
                .andExpect(jsonPath("$.staffID").value(staffId))
                .andExpect(jsonPath("$.purpose").value("Academic advising"))
                .andExpect(jsonPath("$.status").value("Scheduled"));

        assertThat(appointmentRepository.count()).isEqualTo(1);
    }

    @Test
    void createAppointment_studentForOther_forbidden() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setStudentID(anotherStudentId); // Trying to create for another student

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You can only create your own appointments."));
    }

    @Test
    void createAppointment_staffCanCreateForAnyStudent_success() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setStudentID(anotherStudentId);

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void createAppointment_missingStudentID_badRequest() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setStudentID(null);

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAppointment_missingStaffID_badRequest() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setStaffID(null);

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAppointment_missingDate_badRequest() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setDate(null);

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAppointment_pastDate_badRequest() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setDate(LocalDate.now().minusDays(1)); // Yesterday

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAppointment_missingTime_badRequest() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setTime(null);

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAppointment_emptyPurpose_badRequest() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setPurpose("");

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAppointment_purposeTooLong_badRequest() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setPurpose("A".repeat(256)); // 256 characters (max is 255)

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAppointment_noAuth_forbidden() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // GET APPOINTMENTS TESTS

    @Test
    void getAppointments_staffSeesAll() throws Exception {
        // Create appointments for different students
        createTestAppointment(studentId, "Student 1 appointment");
        createTestAppointment(anotherStudentId, "Student 2 appointment");

        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAppointments_studentSeesOnlyOwn() throws Exception {
        // Create appointments for different students
        createTestAppointment(studentId, "My appointment");
        createTestAppointment(anotherStudentId, "Other student appointment");

        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].purpose").value("My appointment"));
    }

    // UPDATE APPOINTMENT TESTS

    @Test
    void updateAppointment_studentOwnAppointment_success() throws Exception {
        Appointment appointment = createTestAppointment(studentId, "Original purpose");

        AppointmentDTO updateDto = createValidAppointmentDTO();
        updateDto.setPurpose("Updated purpose");
        updateDto.setDate(LocalDate.now().plusDays(3));

        mockMvc.perform(put("/api/appointments/{id}", appointment.getApptID())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purpose").value("Updated purpose"));
    }

    @Test
    void updateAppointment_studentOtherAppointment_forbidden() throws Exception {
        Appointment appointment = createTestAppointment(anotherStudentId, "Other student appointment");

        AppointmentDTO updateDto = createValidAppointmentDTO();
        updateDto.setStudentID(anotherStudentId);

        mockMvc.perform(put("/api/appointments/{id}", appointment.getApptID())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You can only reschedule your own appointments."));
    }

    @Test
    void updateAppointment_staffCanUpdateAny_success() throws Exception {
        Appointment appointment = createTestAppointment(anotherStudentId, "Original");

        AppointmentDTO updateDto = createValidAppointmentDTO();
        updateDto.setStudentID(anotherStudentId);
        updateDto.setPurpose("Staff updated");
        updateDto.setTime(LocalTime.of(11, 0));

        mockMvc.perform(put("/api/appointments/{id}", appointment.getApptID())
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purpose").value("Staff updated"));
    }

    @Test
    void updateAppointment_nonExistent_notFound() throws Exception {
        AppointmentDTO updateDto = createValidAppointmentDTO();

        mockMvc.perform(put("/api/appointments/{id}", 99999)
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    // DELETE APPOINTMENT TESTS

    @Test
    void deleteAppointment_studentOwnAppointment_success() throws Exception {
        Appointment appointment = createTestAppointment(studentId, "To delete");

        mockMvc.perform(delete("/api/appointments/{id}", appointment.getApptID())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNoContent());

        assertThat(appointmentRepository.existsById(appointment.getApptID())).isFalse();
    }


    @Test
    void deleteAppointment_studentOtherAppointment_forbidden() throws Exception {
        Appointment appointment = createTestAppointment(anotherStudentId, "Other student appointment");

        mockMvc.perform(delete("/api/appointments/{id}", appointment.getApptID())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You can only cancel your own appointments."));
    }

    @Test
    void deleteAppointment_staffCanDeleteAny_success() throws Exception {
        Appointment appointment = createTestAppointment(anotherStudentId, "To delete by staff");

        mockMvc.perform(delete("/api/appointments/{id}", appointment.getApptID())
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNoContent());

        assertThat(appointmentRepository.existsById(appointment.getApptID())).isFalse();
    }

    @Test
    void deleteAppointment_nonExistent_notFound() throws Exception {
        mockMvc.perform(delete("/api/appointments/{id}", 99999)
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNotFound());
    }


    @Test
    void preventDoubleBooKing_sameExactTime_shouldFail() throws Exception {
        AppointmentDTO dto1 = createValidAppointmentDTO();
        dto1.setTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        AppointmentDTO dto2 = createValidAppointmentDTO();
        dto2.setStudentID(anotherStudentId);
        dto2.setTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This time slot overlaps with an existing appointment."));
    }

    @Test
    void preventDoubleBooKing_partialOverlap_shouldFail() throws Exception {
        AppointmentDTO dto1 = createValidAppointmentDTO();
        dto1.setTime(LocalTime.of(10, 0));
        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        AppointmentDTO dto2 = createValidAppointmentDTO();
        dto2.setStudentID(anotherStudentId);
        dto2.setTime(LocalTime.of(10, 15));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Appointments must start on the hour or half-hour (e.g., 08:00, 08:30)."));
    }

    @Test
    void allowBooking_adjacentTimeSlots_shouldSucceed() throws Exception {
        LocalDate validDate = getNextValidDate();

        AppointmentDTO dto1 = createValidAppointmentDTO();
        dto1.setDate(validDate);
        dto1.setTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        AppointmentDTO dto2 = createValidAppointmentDTO();
        dto2.setStudentID(anotherStudentId);
        dto2.setDate(validDate);
        dto2.setTime(LocalTime.of(10, 30));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        AppointmentDTO dto3 = createValidAppointmentDTO();
        dto3.setDate(validDate);
        dto3.setTime(LocalTime.of(11, 0));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto3)))
                .andExpect(status().isCreated());
    }

    @Test
    void allowBooking_sameTimeDifferentDays_shouldSucceed() throws Exception {
        LocalDate day1 = getNextValidDate();
        LocalDate day2 = day1.plusDays(1);

        // Skip if day2 is Friday or Saturday
        while (day2.getDayOfWeek() == DayOfWeek.FRIDAY || day2.getDayOfWeek() == DayOfWeek.SATURDAY) {
            day2 = day2.plusDays(1);
        }

        // Create appointment on day1 at 10:00
        AppointmentDTO dto1 = createValidAppointmentDTO();
        dto1.setDate(day1);
        dto1.setTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        // Create appointment on day2 at same time (10:00)
        AppointmentDTO dto2 = createValidAppointmentDTO();
        dto2.setStudentID(anotherStudentId);
        dto2.setDate(day2);
        dto2.setTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());
    }

    @Test
    void preventDoubleBooKing_updateToOccupiedSlot_shouldFail() throws Exception {
        LocalDate validDate = getNextValidDate();


        Appointment appt1 = new Appointment();
        appt1.setStudentID(studentId);
        appt1.setStaffID(staffId);
        appt1.setDate(validDate);
        appt1.setTime(LocalTime.of(10, 0));
        appt1.setPurpose("First appointment");
        appt1.setStatus(Appointment.Status.Scheduled);
        appt1 = appointmentRepository.save(appt1);


        Appointment appt2 = new Appointment();
        appt2.setStudentID(anotherStudentId);
        appt2.setStaffID(staffId);
        appt2.setDate(validDate);
        appt2.setTime(LocalTime.of(11, 0));
        appt2.setPurpose("Second appointment");
        appt2.setStatus(Appointment.Status.Scheduled);
        appt2 = appointmentRepository.save(appt2);


        AppointmentDTO updateDto = createValidAppointmentDTO();
        updateDto.setStudentID(anotherStudentId);
        updateDto.setDate(validDate);
        updateDto.setTime(LocalTime.of(10, 0));

        mockMvc.perform(put("/api/appointments/{id}", appt2.getApptID())
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("This time slot overlaps with an existing appointment")));

    }

    @Test
    void allowBooking_fullDaySchedule_noOverlaps() throws Exception {
        LocalDate validDate = getNextValidDate();

        // Book appointments for the entire day (8:00 to 14:00)
        LocalTime[] times = {
                LocalTime.of(8, 0),
                LocalTime.of(8, 30),
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                LocalTime.of(11, 0),
                LocalTime.of(11, 30),
                LocalTime.of(12, 0),
                LocalTime.of(12, 30),
                LocalTime.of(13, 0),
                LocalTime.of(13, 30)
        };

        for (int i = 0; i < times.length; i++) {
            AppointmentDTO dto = createValidAppointmentDTO();
            // Alternate between students
            dto.setStudentID(i % 2 == 0 ? studentId : anotherStudentId);
            dto.setDate(validDate);
            dto.setTime(times[i]);
            dto.setPurpose("Appointment " + (i + 1));

            String token = i % 2 == 0 ? studentToken : anotherStudentToken;

            mockMvc.perform(post("/api/appointments")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        // Verify all 12 slots are booked
        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12));
    }

    @Test
    void allowBooking_afterCancellation_shouldSucceed() throws Exception {
        LocalDate validDate = getNextValidDate();

        // Create an appointment
        Appointment appointment = new Appointment();
        appointment.setStudentID(studentId);
        appointment.setStaffID(staffId);
        appointment.setDate(validDate);
        appointment.setTime(LocalTime.of(10, 0));
        appointment.setPurpose("To be cancelled");
        appointment.setStatus(Appointment.Status.Scheduled);
        appointment = appointmentRepository.save(appointment);

        // Delete the appointment
        mockMvc.perform(delete("/api/appointments/{id}", appointment.getApptID())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNoContent());

        // Now try to book the same slot - should succeed
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setStudentID(anotherStudentId);
        dto.setDate(validDate);
        dto.setTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }


    @Test
    void updateAppointment_changeStatus_success() throws Exception {
        Appointment appointment = createTestAppointment(studentId, "Scheduled appointment");

        AppointmentDTO updateDto = createValidAppointmentDTO();
        updateDto.setStatus(Appointment.Status.Cancelled);
        updateDto.setTime(LocalTime.of(11, 0)); // Change to a different time slot (original is 10:00)

        mockMvc.perform(put("/api/appointments/{id}", appointment.getApptID())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Cancelled"));
    }

    @Test
    void createAppointment_todayAppointment_success() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setStudentID(studentId);
        dto.setDate(LocalDate.now());
        dto.setTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getAppointments_multipleAppointmentsForStudent_returnsAll() throws Exception {
        // Create multiple appointments for the same student
        createTestAppointment(studentId, "Morning appointment");
        createTestAppointment(studentId, "Afternoon appointment");
        createTestAppointment(studentId, "Evening appointment");

        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }


    @Test
    void updateAppointment_rescheduleToNewDateTime_success() throws Exception {
        Appointment appointment = createTestAppointment(studentId, "Original appointment");

        // Reschedule to a different date and time
        AppointmentDTO updateDto = createValidAppointmentDTO();
        updateDto.setDate(LocalDate.now().plusWeeks(1));
        updateDto.setTime(LocalTime.of(13, 30));
        updateDto.setPurpose("Rescheduled appointment");

        mockMvc.perform(put("/api/appointments/{id}", appointment.getApptID())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(LocalDate.now().plusWeeks(1).toString()))
                .andExpect(jsonPath("$.time").value("13:30:00"));
    }

    // EDGE CASES

    @Test
    void createAppointment_withNonExistentStaffId_shouldReturnNotFound() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setStudentID(studentId);
        dto.setStaffID(99999); // Non-existent staff

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Staff with ID 99999 does not exist"));
    }

    @Test
    void createAppointment_nullStatus_defaultsToScheduled() throws Exception {
        AppointmentDTO dto = createValidAppointmentDTO();
        dto.setStudentID(studentId);
        dto.setStatus(null); // Should use default

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Because @NotNull on status
    }

    // HELPER METHODS

    private LocalDate getNextValidDate() {
        LocalDate date = LocalDate.now();
        // If today is Friday or Saturday, move to next Sunday
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.FRIDAY) {
            date = date.plusDays(2); // Move to Sunday
        } else if (dayOfWeek == DayOfWeek.SATURDAY) {
            date = date.plusDays(1); // Move to Sunday
        }
        // If date is in the past, use tomorrow
        if (!date.isAfter(LocalDate.now())) {
            date = LocalDate.now().plusDays(1);
            // Check again if tomorrow is Friday or Saturday
            dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek == DayOfWeek.FRIDAY) {
                date = date.plusDays(2);
            } else if (dayOfWeek == DayOfWeek.SATURDAY) {
                date = date.plusDays(1);
            }
        }
        return date;
    }

    private AppointmentDTO createValidAppointmentDTO() {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setStudentID(studentId);
        dto.setStaffID(staffId);
        dto.setDate(getNextValidDate());
        dto.setTime(LocalTime.of(10, 0));
        dto.setPurpose("Academic advising");
        dto.setStatus(Appointment.Status.Scheduled);
        return dto;
    }

    private Appointment createTestAppointment(Integer studentId, String purpose) {
        Appointment appointment = new Appointment();
        appointment.setStudentID(studentId);
        appointment.setStaffID(staffId);
        appointment.setDate(getNextValidDate());
        appointment.setTime(LocalTime.of(10, 0));
        appointment.setPurpose(purpose);
        appointment.setStatus(Appointment.Status.Scheduled);
        return appointmentRepository.save(appointment);
    }
}