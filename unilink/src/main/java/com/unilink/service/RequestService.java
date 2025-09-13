package com.unilink.service;

import org.springframework.stereotype.Service;
import com.unilink.dto.RequestDTO;
import com.unilink.dto.RequestResponseDTO;
import com.unilink.entity.Request;
import com.unilink.entity.Notification;
import com.unilink.repository.RequestRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RequestService {
    private final RequestRepository repository;
    private final NotificationService notificationService;

    public RequestService(RequestRepository repository, NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
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

        // Create notification for request submission
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
        return dto;
    }

    public List<RequestResponseDTO> getAllRequests() {
        return repository.findAll()
                .stream()
                .map(this::toResponseDTOSafe)
                .toList();
    }

    // ✅ Added: fetch by studentId
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
            // Create notification for request deletion
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

    // Add this method to update request status with notifications
    @Transactional
    public Optional<RequestResponseDTO> updateRequestStatus(Integer id, Request.Status status) {
        return repository.findById(id).map(existing -> {
            Request.Status oldStatus = existing.getStatus();
            existing.setStatus(status);
            Request updated = repository.save(existing);

            // Create notification for status change
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