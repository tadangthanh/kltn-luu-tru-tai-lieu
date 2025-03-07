package vn.kltn.service;

import vn.kltn.common.RepoPermission;

import java.io.InputStream;
import java.util.Set;

public interface IAzureStorageService {
    String uploadChunked(InputStream data, String originalFileName, String containerName, String sasToken,long length, int chunkSize);

    void createContainerForRepository(String repoName);

    String generatePermissionRepo(String containerName, Set<RepoPermission> permissionList);

    void deleteContainer(String containerName);

    void deleteBlob(String containerName, String blobName);

    InputStream downloadBlobInputStream(String containerName, String blobName); // Tải blob về

    byte[] downloadBlobByteData(String containerName, String blobName); // Tải blob về dạng byte[]
}
