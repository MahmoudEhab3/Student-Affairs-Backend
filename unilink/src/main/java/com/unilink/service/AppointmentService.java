package com.unilink.service;

import com.unilink.dto.AppointmentDTO;
import com.unilink.entity.Appointment;
import com.unilink.repository.AppointmentRepository;
import com.unilink.repository.StaffRepository; // 🔹 import staff repo
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    private final AppointmentRepository repository;
    private final StaffRepository staffRepository; // 🔹 add staff repo

    public AppointmentService(AppointmentRepository repository, StaffRepository staffRepository) {
        this.repository = repository;
        this.staffRepository = staffRepository;
    }

    // --- 🔹 Validation helper ---
    private void validateAppointmentTime(AppointmentDTO dto) {
        // Working days: Sunday → Thursday
        DayOfWeek day = dto.getDate().getDayOfWeek();
        if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
            throw new RuntimeException("Appointments can only be booked from Sunday to Thursday.");
        }

        // Working hours: 08:00 → 14:00
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(14, 0);
        if (dto.getTime().isBefore(start) || dto.getTime().isAfter(end)) {
            throw new RuntimeException("Appointments can only be booked between 08:00 and 14:00.");
        }

        // Only 30-minute slots allowed (e.g., 08:00, 08:30, 09:00...)
        int minute = dto.getTime().getMinute();
        if (minute != 0 && minute != 30) {
            throw new RuntimeException("Appointments must start on the hour or half-hour (e.g., 08:00, 08:30).");
        }
    }

    private void checkOverlap(LocalDate date, LocalTime time) {
        LocalTime slotStart = time;
        LocalTime slotEnd = time.plusMinutes(30);

        List<Appointment> sameDayAppointments = repository.findAll().stream()
                .filter(a -> a.getDate().equals(date))
                .toList();

        for (Appointment existing : sameDayAppointments) {
            LocalTime existingStart = existing.getTime();
            LocalTime existingEnd = existingStart.plusMinutes(30);

            boolean overlaps = !slotEnd.isBefore(existingStart) && !slotStart.isAfter(existingEnd.minusSeconds(1));
            if (overlaps) {
                throw new RuntimeException("This time slot overlaps with an existing appointment.");
            }
        }
    }

    // 🔹 New method to check staff existence
    public boolean staffExists(Integer staffId) {
        return staffRepository.existsById(staffId);
    }

    // Create appointment if time slot is free
    public Appointment createAppointment(AppointmentDTO dto) {
        validateAppointmentTime(dto);
        checkOverlap(dto.getDate(), dto.getTime());

        Appointment appt = new Appointment();
        appt.setStudentID(dto.getStudentID());
        appt.setStaffID(dto.getStaffID());
        appt.setDate(dto.getDate());
        appt.setTime(dto.getTime());
        appt.setPurpose(dto.getPurpose());
        appt.setStatus(Appointment.Status.Scheduled);

        return repository.save(appt);
    }

    public List<Appointment> getAllAppointments() {
        return repository.findAll();
    }

    public List<Appointment> getAppointmentsByStudent(Integer studentId) {
        return repository.findByStudentID(studentId);
    }

    public Optional<Appointment> getAppointmentById(Integer id) {
        return repository.findById(id);
    }

    public Optional<Appointment> updateAppointment(Integer id, AppointmentDTO dto) {
        validateAppointmentTime(dto);
        checkOverlap(dto.getDate(), dto.getTime());

        return repository.findById(id).map(appt -> {
            appt.setDate(dto.getDate());
            appt.setTime(dto.getTime());
            appt.setPurpose(dto.getPurpose());
            appt.setStatus(dto.getStatus());
            return repository.save(appt);
        });
    }

    public boolean deleteAppointment(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
