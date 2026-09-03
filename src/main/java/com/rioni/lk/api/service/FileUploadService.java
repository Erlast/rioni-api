package com.rioni.lk.api.service;

import java.util.List;
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

    /**
     * Upload multiple files for the given profile.
     *
     * @param profileId the profile ID
     * @param files     the uploaded files
     * @param path      optional subdirectory path (may be null or empty)
     * @return the public URLs of the saved files
     * @throws IllegalArgumentException if files are empty, exceed size limit, etc.
     */
    List<String> uploadFiles(Long profileId, List<MultipartFile> files, String path);

    /**
     * Delete a file for the given profile.
     *
     * @param profileId the profile ID
     * @param fileName  the file name to delete
     * @param path      optional subdirectory path (may be null or empty)
     * @return true if the file was deleted, false otherwise
     */
    boolean deleteFile(Long profileId, String fileName, String path);
}
