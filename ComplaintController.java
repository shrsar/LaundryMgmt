package com.laundrymgmt.modern.controller;

import com.laundrymgmt.modern.dto.ComplaintDtos;
import com.laundrymgmt.modern.service.ComplaintService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    public List<ComplaintDtos.ComplaintResponse> listComplaints(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return complaintService.listComplaints(authorizationHeader);
    }

    @PostMapping
    public ComplaintDtos.ComplaintResponse createComplaint(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        @RequestBody ComplaintDtos.ComplaintRequest request
    ) {
        return complaintService.createComplaint(authorizationHeader, request);
    }

    @PostMapping("/{ticketCode}/resolve")
    public ComplaintDtos.ComplaintResponse resolveComplaint(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        @PathVariable String ticketCode
    ) {
        return complaintService.resolveComplaint(authorizationHeader, ticketCode);
    }
}
