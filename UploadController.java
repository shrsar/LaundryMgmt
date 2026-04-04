package com.laundrymgmt.modern.controller;

import com.laundrymgmt.modern.dto.CommonDtos;
import com.laundrymgmt.modern.service.FileStorageService;
import com.laundrymgmt.modern.service.SessionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final FileStorageService fileStorageService;
    private final SessionService sessionService;

    public UploadController(FileStorageService fileStorageService, SessionService sessionService) {
        this.fileStorageService = fileStorageService;
        this.sessionService = sessionService;
    }

    @PostMapping("/image")
    public CommonDtos.UploadResponse uploadImage(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
        @RequestParam("file") MultipartFile file
    ) {
        sessionService.requireUser(authorizationHeader);
        return new CommonDtos.UploadResponse(fileStorageService.storeImage(file));
    }
}
