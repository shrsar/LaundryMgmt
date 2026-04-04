package com.laundrymgmt.modern.dto;

import java.time.Instant;

public final class CommonDtos {

    private CommonDtos() {
    }

    public record MessageResponse(String message) {
    }

    public record UploadResponse(String url) {
    }

    public record ErrorResponse(String message, Instant timestamp) {
    }
}
