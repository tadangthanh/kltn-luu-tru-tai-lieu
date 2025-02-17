package vn.kltn.service;

import org.springframework.web.multipart.MultipartFile;
import vn.kltn.common.RepoPermission;

import java.io.InputStream;
import java.util.List;

public interface IAzureStorageService {
    String upload(MultipartFile file);

    String upload(InputStream data, String originalFileName, long length);

    String uploadChunked(InputStream data, String originalFileName, long length, int chunkSize);

    String getBlobUrl(String blobName);

    boolean deleteBlob(String blobName);

    String upload(InputStream data, long length, String fileName, String contentType);

    InputStream downloadBlob(String blobName); // Tải blob về

    String createContainerForRepository(String repoName, String uuid);

    String generatePermissionForMemberRepo(String containerName, List<RepoPermission> permissionList);

    boolean deleteContainer(String containerName);
}
