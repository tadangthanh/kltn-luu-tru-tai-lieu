package vn.kltn.service;

import vn.kltn.entity.MemberRole;

import java.io.InputStream;

public interface IAzureStorageService {
    String uploadChunkedWithContainerName(InputStream data, String originalFileName, String containerName, String sasToken, long length, int chunkSize);

    String uploadChunkedWithContainerDefault(InputStream data, String originalFileName, long length, int chunkSize);

    void createContainerForRepository(String repoName);

    String generatePermissionRepoByMemberRole(String containerName, MemberRole role);

    void deleteContainer(String containerName);

    void deleteBlob(String containerName, String blobName);

    InputStream downloadBlobInputStream(String containerName, String blobName); // Tải blob về

    byte[] downloadBlobByteData(String containerName, String blobName); // Tải blob về dạng byte[]
}
