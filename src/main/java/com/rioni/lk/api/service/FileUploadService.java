package com.rioni.lk.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    /**
     * Upload a file for the given profile.
     *
     * @param profileId the profile ID
     * @param file      the uploaded file
     * @param path      optional subdirectory path (may be null or empty)
     * @return the public URL of the saved file
     * @throws IllegalArgumentException if file is empty, exceeds size limit, etc.
     */
    String uploadFile(Long profileId, MultipartFile file, String path);
}
