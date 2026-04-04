package com.laundrymgmt.modern.service;

import com.laundrymgmt.modern.exception.ApiException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path uploadRoot = Paths.get("uploads").toAbsolutePath().normalize();

    @PostConstruct
    public void ensureUploadFolderExists() {
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create upload directory", exception);
        }
    }

    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "An image file is required.");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image.bin" : file.getOriginalFilename());
        String extension = "";
        int extensionIndex = originalFileName.lastIndexOf('.');
        if (extensionIndex >= 0) {
            extension = originalFileName.substring(extensionIndex);
        }

        String storedFileName = UUID.randomUUID() + extension;
        Path destination = uploadRoot.resolve(storedFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image.");
        }

        return "/uploads/" + storedFileName;
    }
}
