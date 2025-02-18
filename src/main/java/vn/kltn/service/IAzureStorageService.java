package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;
import vn.kltn.common.RepoPermission;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface IAzureStorageService {
    String upload(MultipartFile file);

    String upload(InputStream data, String originalFileName, long length);

    String uploadChunked(InputStream data, String originalFileName, long length, int chunkSize);

    String getBlobUrl(String blobName);

    boolean deleteBlob(String blobName);

    String upload(InputStream data, long length, String fileName, String contentType);

    InputStream downloadBlob(String blobName); // Tải blob về

    void createContainerForRepository(String repoName);

    String generatePermissionForMemberRepo(String containerName, Set<RepoPermission> permissionList);

    boolean deleteContainer(String containerName);
}
