package com.laundrymgmt.modern.dto;

import com.laundrymgmt.modern.model.ComplaintStatus;
import java.time.LocalDateTime;

public final class ComplaintDtos {

    private ComplaintDtos() {
    }

    public record ComplaintRequest(String description, String attachmentUrl) {
    }

    public record ComplaintResponse(Long id, String ticketCode, String customerCode, String customerName,
                                    String phoneNumber, String description, String attachmentUrl,
                                    ComplaintStatus status, LocalDateTime createdAt) {
    }
}
