package com.laundrymgmt.modern.service;

import com.laundrymgmt.modern.dto.ComplaintDtos;
import com.laundrymgmt.modern.exception.ApiException;
import com.laundrymgmt.modern.model.Complaint;
import com.laundrymgmt.modern.model.ComplaintStatus;
import com.laundrymgmt.modern.model.Role;
import com.laundrymgmt.modern.model.UserAccount;
import com.laundrymgmt.modern.repository.ComplaintRepository;
import com.laundrymgmt.modern.util.CodeFactory;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final SessionService sessionService;
    private final CodeFactory codeFactory;

    public ComplaintService(ComplaintRepository complaintRepository, SessionService sessionService,
                            CodeFactory codeFactory) {
        this.complaintRepository = complaintRepository;
        this.sessionService = sessionService;
        this.codeFactory = codeFactory;
    }

    public List<ComplaintDtos.ComplaintResponse> listComplaints(String authorizationHeader) {
        UserAccount viewer = sessionService.requireUser(authorizationHeader);
        List<Complaint> complaints = viewer.getRole() == Role.ADMIN
            ? complaintRepository.findAllByOrderByCreatedAtDescIdDesc()
            : complaintRepository.findAllByCustomer_IdOrderByCreatedAtDescIdDesc(viewer.getId());

        return complaints.stream().map(this::toResponse).toList();
    }

    @Transactional
    public ComplaintDtos.ComplaintResponse createComplaint(String authorizationHeader,
                                                           ComplaintDtos.ComplaintRequest request) {
        UserAccount customer = sessionService.requireRole(authorizationHeader, Role.CUSTOMER);
        String description = requireText(request.description(), "Complaint description is required.");

        Complaint complaint = new Complaint();
        complaint.setTicketCode(codeFactory.generate("TKT", code ->
            complaintRepository.findByTicketCodeIgnoreCase(code).isPresent()
        ));
        complaint.setCustomer(customer);
        complaint.setPhoneNumber(customer.getPhone());
        complaint.setDescription(description);
        complaint.setAttachmentUrl(normalizeOptionalText(request.attachmentUrl()));
        complaint.setStatus(ComplaintStatus.OPEN);
        complaint.setCreatedAt(LocalDateTime.now());

        return toResponse(complaintRepository.save(complaint));
    }

    @Transactional
    public ComplaintDtos.ComplaintResponse resolveComplaint(String authorizationHeader, String ticketCode) {
        sessionService.requireRole(authorizationHeader, Role.ADMIN);
        Complaint complaint = complaintRepository.findByTicketCodeIgnoreCase(ticketCode)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Complaint not found."));

        if (complaint.getStatus() == ComplaintStatus.RESOLVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Complaint is already resolved.");
        }

        complaint.setStatus(ComplaintStatus.RESOLVED);
        return toResponse(complaintRepository.save(complaint));
    }

    public ComplaintDtos.ComplaintResponse toResponse(Complaint complaint) {
        return new ComplaintDtos.ComplaintResponse(
            complaint.getId(),
            complaint.getTicketCode(),
            complaint.getCustomer().getCustomerCode(),
            complaint.getCustomer().getDisplayName(),
            complaint.getPhoneNumber(),
            complaint.getDescription(),
            complaint.getAttachmentUrl(),
            complaint.getStatus(),
            complaint.getCreatedAt()
        );
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
