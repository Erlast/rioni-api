package com.rioni.lk.api.controller;

import com.rioni.lk.api.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    private final FileUploadService fileUploadService;

    @Autowired
    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    private Long getCurrentProfileId() {
        Integer profileId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return profileId.longValue();
    }

    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "path", required = false) String path) {
        Long profileId = getCurrentProfileId();
        try {
            List<String> fileUrls = fileUploadService.uploadFiles(profileId, files, path);
            return new ResponseEntity<>(Map.of("urls", fileUrls), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("File upload validation failed for profile {}: {}", profileId, e.getMessage());
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("File upload failed for profile {}: {}: {}", profileId, e.getClass().getName(), e.getMessage(), e);
            return new ResponseEntity<>(Map.of("error", "Failed to upload files"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
