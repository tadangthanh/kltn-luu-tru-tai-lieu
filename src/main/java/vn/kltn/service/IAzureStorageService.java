package vn.kltn.service;

import vn.kltn.entity.MemberRole;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IAzureStorageService {
    String uploadChunkedWithContainerName(InputStream data, String originalFileName, String containerName, String sasToken, long length, int chunkSize);

    String uploadChunkedWithContainerDefault(InputStream data, String originalFileName, long length, int chunkSize);

    CompletableFuture<String> uploadChunkedWithContainerDefaultAsync(InputStream data, String originalFileName, long length, int chunkSize);

    String copyBlob(String sourceBlobName, String destinationBlobName);

    void createContainerForRepository(String repoName);

    String generatePermissionRepoByMemberRole(String containerName, MemberRole role);

    void deleteContainer(String containerName);

    void deleteBlob(String containerName, String blobName);

    void deleteBlob(String blobName);

    void deleteBLobs(List<String> blobNames);

    InputStream downloadBlobInputStream(String containerName, String blobName); // Tải blob về

    InputStream downloadBlobInputStream(String blobName); // Tải blob về

    CompletableFuture<InputStream> downloadBlobInputStreamAsync(String blobName); // Tải blob về

    File downloadToFile(String blobName, String tempDirPath);


    byte[] downloadBlobByteData(String containerName, String blobName); // Tải blob về dạng byte[]
}
