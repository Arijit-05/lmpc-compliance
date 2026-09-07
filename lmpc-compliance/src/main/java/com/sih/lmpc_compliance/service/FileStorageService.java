package com.sih.lmpc_compliance.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDirectory =
            Paths.get("uploads");

    public String saveFile(MultipartFile file) throws IOException {

        if (!Files.exists(uploadDirectory)) {
            Files.createDirectories(uploadDirectory);
        }

        String originalFileName =
                file.getOriginalFilename();

        String extension = "";

        if (originalFileName != null &&
                originalFileName.contains(".")) {

            extension = originalFileName.substring(
                    originalFileName.lastIndexOf(".")
            );
        }

        String fileName =
                UUID.randomUUID() + extension;

        Path filePath =
                uploadDirectory.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                filePath
        );

        return filePath.toString();
    }
}