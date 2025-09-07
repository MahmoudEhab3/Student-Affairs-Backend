package com.unilink.service;

import org.springframework.stereotype.Service;
import com.unilink.dto.RequestDTO;
import com.unilink.dto.RequestResponseDTO;
import com.unilink.entity.Request;
import com.unilink.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class RequestService {

    @Autowired
    private RequestRepository repository;

    public Request createRequest(RequestDTO dto) {
        Request request = new Request();
        request.setTitle(dto.getTitle());
        request.setStudentID(dto.getStudentID());
        request.setType(dto.getType());
        request.setDescription(dto.getDescription());
        request.setDocument(dto.getDocument());
        request.setStatus(Request.Status.Pending);
        return repository.save(request);
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

    public Optional<RequestResponseDTO> updateRequest(Integer id, RequestDTO dto) {
        return repository.findById(id).map(existing -> {
            existing.setTitle(dto.getTitle());
            existing.setType(dto.getType());
            existing.setDescription(dto.getDescription());
            if (dto.getDocument() != null) {
                existing.setDocument(dto.getDocument());
            }
            existing.setStatus(existing.getStatus()); 
            Request updated = repository.save(existing);
            return toResponseDTO(updated);
        });
    }

    public boolean deleteRequest(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
