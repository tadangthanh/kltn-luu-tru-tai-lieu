package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface IFileStorageService {
    String upload(MultipartFile file);
    String upload(InputStream data,String originalFileName,long length);
    String uploadChunked(InputStream data, String originalFileName, long length, int chunkSize) throws IOException;
    String getBlobUrl(String blobName);

    boolean deleteBlob(String blobName);

    String upload(InputStream data, long length, String fileName, String contentType);

    InputStream downloadBlob(String blobName); // Tải blob về
}
