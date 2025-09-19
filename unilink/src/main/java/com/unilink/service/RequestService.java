package com.unilink.service;

import com.unilink.dto.RequestDTO;
import com.unilink.dto.RequestResponseDTO;
import com.unilink.entity.Request;
import com.unilink.entity.Notification;
import com.unilink.repository.RequestRepository;
import com.unilink.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RequestService {
    private final RequestRepository repository;
    private final NotificationService notificationService;
    private final StudentRepository studentRepository;

    public RequestService(RequestRepository repository,
                          NotificationService notificationService,
                          StudentRepository studentRepository) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public Request createRequest(RequestDTO dto) {
        Request request = new Request();
        request.setTitle(dto.getTitle());
        request.setStudentID(dto.getStudentID());
        request.setType(dto.getType());
        request.setDescription(dto.getDescription());
        request.setDocument(dto.getDocument());
        request.setStatus(Request.Status.Pending);

        Request savedRequest = repository.save(request);

        // 🔔 Notification for submission
        Notification notification = new Notification();
        notification.setUserId(dto.getStudentID());
        notification.setType(Notification.NotificationType.REQUEST);
        notification.setTitle("Request Submitted");
        notification.setMessage("Your request for " + dto.getType() + " has been submitted and is under review");
        notificationService.createNotification(notification);

        return savedRequest;
    }

    private RequestResponseDTO toResponseDTO(Request request) {
        RequestResponseDTO dto = new RequestResponseDTO();
        dto.setRequestID(request.getRequestID());
        dto.setTitle(request.getTitle());
        dto.setStudentID(request.getStudentID());
        dto.setType(request.getType());
        dto.setDescription(request.getDescription());
        dto.setStatus(request.getStatus());
        dto.setCreatedDate(request.getCreatedDate());
        dto.setUpdatedDate(request.getUpdatedDate());
        dto.setDocument(request.getDocument());

        // Fetch student name
        if (request.getStudentID() != null) {
            studentRepository.findById(request.getStudentID())
                    .ifPresent(student -> dto.setStudentName(student.getName()));
        }

        return dto;
    }

    private RequestResponseDTO toResponseDTOSafe(Request request) {
        RequestResponseDTO dto = new RequestResponseDTO();
        dto.setRequestID(request.getRequestID());
        dto.setTitle(request.getTitle() != null ? request.getTitle() : "");
        dto.setStudentID(request.getStudentID() != null ? request.getStudentID() : 0);
        dto.setType(request.getType() != null ? request.getType() : "");
        dto.setDescription(request.getDescription() != null ? request.getDescription() : "");
        dto.setStatus(request.getStatus() != null ? request.getStatus() : Request.Status.Pending);
        dto.setCreatedDate(request.getCreatedDate() != null ? request.getCreatedDate() : LocalDateTime.now());
        dto.setUpdatedDate(request.getUpdatedDate() != null ? request.getUpdatedDate() : LocalDateTime.now());
        dto.setDocument(request.getDocument());

        // Safe fetch student name
        if (request.getStudentID() != null) {
            studentRepository.findById(request.getStudentID())
                    .ifPresent(student -> dto.setStudentName(student.getName()));
        } else {
            dto.setStudentName("");
        }

        return dto;
    }

    public List<RequestResponseDTO> getAllRequests() {
        return repository.findAll()
                .stream()
                .map(this::toResponseDTOSafe)
                .toList();
    }

    public List<RequestResponseDTO> getRequestsByStudentId(Integer studentId) {
        return repository.findByStudentID(studentId)
                .stream()
                .map(this::toResponseDTOSafe)
                .toList();
    }

    public Optional<RequestResponseDTO> getRequestById(Integer id) {
        return repository.findById(id)
                .map(this::toResponseDTOSafe);
    }

    @Transactional
    public Optional<RequestResponseDTO> updateRequest(Integer id, RequestDTO dto) {
        return repository.findById(id).map(existing -> {
            existing.setTitle(dto.getTitle());
            existing.setType(dto.getType());
            existing.setDescription(dto.getDescription());
            if (dto.getDocument() != null) {
                existing.setDocument(dto.getDocument());
            }
            if (dto.getStatus() != null) {
                existing.setStatus(dto.getStatus());
            }
            Request updated = repository.save(existing);
            return toResponseDTO(updated);
        });
    }

    @Transactional
    public boolean deleteRequest(Integer id) {
        return repository.findById(id).map(request -> {
            // 🔔 Notification for deletion
            Notification notification = new Notification();
            notification.setUserId(request.getStudentID());
            notification.setType(Notification.NotificationType.REQUEST);
            notification.setTitle("Request Deleted");
            notification.setMessage("Your request for " + request.getType() + " has been deleted");
            notificationService.createNotification(notification);

            repository.deleteById(id);
            return true;
        }).orElse(false);
    }

    @Transactional
    public Optional<RequestResponseDTO> updateRequestStatusAndComment(Integer id, Request.Status status, String comment) {
        return repository.findById(id).map(existing -> {
            existing.setStatus(status);
            existing.setComment(comment);

            Request updated = repository.save(existing);

            // 🔔 Notify student
            Notification notification = new Notification();
            notification.setUserId(existing.getStudentID());
            notification.setType(Notification.NotificationType.REQUEST);
            notification.setTitle("Request Status Updated");
            notification.setMessage("Your request for " + existing.getType() +
                    " is now " + status + (comment != null ? " (Note: " + comment + ")" : ""));
            notificationService.createNotification(notification);

            return toResponseDTO(updated);
        });
    }

    @Transactional
    public Optional<RequestResponseDTO> updateRequestStatus(Integer id, Request.Status status) {
        return repository.findById(id).map(existing -> {
            existing.setStatus(status);
            Request updated = repository.save(existing);

            // 🔔 Notify student of status change
            Notification notification = new Notification();
            notification.setUserId(existing.getStudentID());
            notification.setType(Notification.NotificationType.REQUEST);

            if (status == Request.Status.Approved) {
                notification.setTitle("Request Approved");
                notification.setMessage("Your request for " + existing.getType() + " has been approved");
            } else if (status == Request.Status.Rejected) {
                notification.setTitle("Request Rejected");
                notification.setMessage("Your request for " + existing.getType() + " has been rejected");
            } else if (status == Request.Status.Pending) {
                notification.setTitle("Request Status Changed");
                notification.setMessage("Your request status has been changed to pending");
            } else {
                notification.setTitle("Request Status Updated");
                notification.setMessage("Your request status has been updated to: " + status);
            }

            notificationService.createNotification(notification);

            return toResponseDTO(updated);
        });
    }
}
