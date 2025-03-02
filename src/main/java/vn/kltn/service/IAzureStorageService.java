package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;
import vn.kltn.common.RepoPermission;

import java.io.InputStream;
import java.util.Set;

public interface IAzureStorageService {
    String uploadChunked(InputStream data, String originalFileName, String containerName, String sasToken, long length, int chunkSize);

    void createContainerForRepository(String repoName);

    String generatePermissionRepo(String containerName, Set<RepoPermission> permissionList);

    boolean deleteContainer(String containerName);
    boolean deleteBlob(String containerName, String blobName);
}
