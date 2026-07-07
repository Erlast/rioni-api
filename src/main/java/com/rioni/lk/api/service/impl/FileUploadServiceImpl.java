package com.rioni.lk.api.service.impl;

import com.rioni.lk.api.service.FileUploadService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadServiceImpl.class);

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15 MB

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
        ".exe", ".bat", ".cmd", ".com", ".msi", ".scr", ".pif",
        ".vbs", ".vbe", ".js", ".jse", ".wsf", ".wsh",
        ".ps1", ".psm1", ".psd1", ".ps1xml",
        ".sh", ".bash", ".zsh",
        ".dll", ".sys", ".bin",
        ".jar", ".py", ".pyc",
        ".app", ".plugin"
    );

    @Value("${app.uploads.storage-path}")
    private String storagePath;

    @Value("${app.uploads.base-url}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        try {
            Path dir = Path.of(storagePath);
            log.info("Initializing upload storage directory: {} (absolute: {})", storagePath, dir.toAbsolutePath());
            Files.createDirectories(dir);
            log.info("Upload storage directory ready at: {}", dir.toAbsolutePath());
        } catch (IOException e) {
            log.error("Could not create upload storage directory: {} (absolute: {})", storagePath, Path.of(storagePath).toAbsolutePath(), e);
            throw new RuntimeException("Could not create upload storage directory: " + storagePath, e);
        }
    }

    @Override
    public String uploadFile(Long profileId, MultipartFile file, String path) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed size of 15 MB");
        }

        // Determine original extension and check if it is blocked
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (BLOCKED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("File extension '" + extension + "' is not allowed for security reasons");
            }
        }

        // Generate unique filename
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        // Build directory path: {storagePath}/{profileId}/{optional path}
        // Use toAbsolutePath() so file.transferTo() doesn't resolve relative to Tomcat's temp dir
        Path userDir = Paths.get(storagePath, String.valueOf(profileId)).toAbsolutePath().normalize();
        Path targetDir = userDir;

        if (path != null && !path.trim().isEmpty()) {
            // Normalize path (strip leading/trailing slashes)
            String normalizedPath = path.trim().replaceAll("^/+|/+$", "");
            if (!normalizedPath.isEmpty()) {
                targetDir = userDir.resolve(normalizedPath);
            }
        }

        log.debug("Upload target: storagePath={}, profileId={}, path='{}', targetDir={}",
                storagePath, profileId, path, targetDir);

        try {
            Files.createDirectories(targetDir);

            Path targetFile = targetDir.resolve(uniqueFileName);
            log.debug("Writing file to: {}", targetFile);
            file.transferTo(targetFile.toFile());

            // Build public URL
            String relativePath = String.valueOf(profileId);
            if (path != null && !path.trim().isEmpty()) {
                String normalizedPath = path.trim().replaceAll("^/+|/+$", "");
                if (!normalizedPath.isEmpty()) {
                    relativePath = relativePath + "/" + normalizedPath;
                }
            }
            relativePath = relativePath + "/" + uniqueFileName;

            String fileUrl = baseUrl + relativePath;
            log.info("File uploaded successfully: {} -> {}", targetFile.toAbsolutePath(), fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to save file to {} (absolute: {}): {}", targetDir, targetDir.toAbsolutePath(), e.getMessage(), e);
            throw new RuntimeException("Failed to save file", e);
        }
    }
}
