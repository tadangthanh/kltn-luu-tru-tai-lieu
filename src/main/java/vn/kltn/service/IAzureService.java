package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface IAzureService {
    String upload(MultipartFile file);

    String getBlobUrl(String blobName);

    boolean deleteBlob(String blobName);

    String upload(InputStream data, long length, String fileName, String contentType);

    InputStream downloadBlob(String blobName); // Tải blob về
}
