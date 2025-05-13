package vn.kltn.service;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IAzureStorageService {
    String uploadChunkedWithContainerDefault(InputStream data, String originalFileName, long length, int chunkSize);

    CompletableFuture<String> uploadProgress(InputStream data, String originalFileName, long length, int chunkSize);

    String copyBlob(String sourceBlobName);

    void deleteBlob(String blobName);

    void deleteBLobs(List<String> blobNames);

    InputStream downloadBlobInputStream(String blobName); // Tải blob về

    File downloadToFile(String blobName, String tempDirPath);

    String getBlobUrl(String blobName); // Lấy url của blob

}
