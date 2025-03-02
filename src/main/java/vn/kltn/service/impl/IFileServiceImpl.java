package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.common.RepoPermission;
import vn.kltn.dto.request.FileRequest;
import vn.kltn.dto.request.TagRequest;
import vn.kltn.dto.response.FileResponse;
import vn.kltn.entity.*;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.map.FileMapper;
import vn.kltn.map.TagMapper;
import vn.kltn.repository.*;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IFileService;
import vn.kltn.service.IRepoService;
import vn.kltn.util.SasTokenValidator;
import vn.kltn.validation.ValidatePermissionMember;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FILE_SERVICE")
public class IFileServiceImpl implements IFileService {
    private final FileRepo fileRepo;
    private final FileMapper fileMapper;
    private final RepositoryRepo repositoryRepo;
    private final IAzureStorageService azureStorageService;
    private final RepoMemberRepo repoMemberRepo;
    private final IAuthenticationService authenticationService;
    private final FileHasTagRepo fileHasTagRepo;
    private final TagRepo tagRepo;
    private final TagMapper tagMapper;
    private final IRepoService repoService;

    @Override
    @ValidatePermissionMember(RepoPermission.CREATE)
    public FileResponse uploadFile(Long repoId, FileRequest fileRequest, MultipartFile file) {
        File fileEntity = mapToEntity(repoId, fileRequest, file);
        fileEntity.setVersion(1);
        fileEntity = fileRepo.save(fileEntity);
        saveFileHasTag(fileRequest.getTags(), fileEntity);
        return fileMapper.entityToResponse(fileEntity);
    }


    private File mapToEntity(Long repoId, FileRequest fileRequest, MultipartFile file) {
        File fileEntity = fileMapper.requestToEntity(fileRequest);
        fileEntity.setCheckSum(calculateChecksum(file));
        fileEntity.setFileSize(file.getSize());
        fileEntity.setFileType(file.getContentType());
        fileEntity.setPublic(fileRequest.isPublic());
        Repo repo = repoService.getRepositoryById(repoId);
        // upload file to cloud
        String fileBlobName = uploadFileToCloud(file, repo.getContainerName(), getSasToken(repoId));
        fileEntity.setFileBlobName(fileBlobName);
        fileEntity.setRepo(repo);
        RepoMember uploadedBy = getAuthMemberByRepoId(repoId);
        fileEntity.setUploadedBy(uploadedBy);
        return fileEntity;
    }


    private String getSasToken(Long repoId) {
        User authUser = authenticationService.getAuthUser();
        Repo repo = repoService.getRepositoryById(repoId);
        if (repo.getOwner().getId().equals(authUser.getId())) {
            return azureStorageService.generatePermissionRepo(repo.getContainerName(), Set.of(RepoPermission.values()));
        }
        RepoMember repoMember = getRepoMemberByUserIdAndRepoIdOrThrow(authUser.getId(), repoId);
        String sasToken = repoMember.getSasToken();
        if (!SasTokenValidator.isSasTokenValid(sasToken)) {
            repoMember = updateSasTokenMember(repoId, authUser.getId());
        }
        return repoMember.getSasToken();
    }

    private RepoMember updateSasTokenMember(Long repoId, Long userId) {
        RepoMember repoMember = getRepoMemberByUserIdAndRepoIdOrThrow(userId, repoId);
        Repo repo = repoService.getRepositoryById(repoId);
        String newSasToken = azureStorageService.generatePermissionRepo(repo.getContainerName(), repoMember.getPermissions());
        repoMember.setSasToken(newSasToken);
        return repoMemberRepo.save(repoMember);
    }

    private RepoMember getAuthMemberByRepoId(Long repoId) {
        User authUser = authenticationService.getAuthUser();
        return getRepoMemberByUserIdAndRepoIdOrThrow(authUser.getId(), repoId);
    }

    private RepoMember getRepoMemberByUserIdAndRepoIdOrThrow(Long userId, Long repoId) {
        return repoMemberRepo.findRepoMemberByUserIdAndRepoId(userId, repoId).orElseThrow(() -> {
            log.error("Không tìm thấy repo member với user id: {} và repo id: {}", userId, repoId);
            return new RuntimeException("Không tìm thấy repo member với user id: " + userId + " và repo id: " + repoId);
        });
    }

    private String uploadFileToCloud(MultipartFile file, String containerName, String sasToken) {
        try (InputStream inputStream = file.getInputStream()) {
            return azureStorageService.uploadChunked(inputStream, file.getOriginalFilename(), containerName, sasToken, file.getSize(), 10 * 1024 * 1024);
        } catch (IOException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }

    private void saveFileHasTag(TagRequest[] tags, File fileEntity) {
        for (TagRequest tagRequest : tags) {
            if (tagRepo.existsByName(tagRequest.getName())) {
                Tag tag = tagRepo.findByName(tagRequest.getName()).orElse(null);
                saveFileHasTag(fileEntity, tag);
            } else {
                Tag tag = tagMapper.requestToEntity(tagRequest);
                tag = tagRepo.save(tag);
                saveFileHasTag(fileEntity, tag);
            }
        }
    }

    private void saveFileHasTag(File file, Tag tag) {
        FileHasTag fileHasTag = new FileHasTag();
        fileHasTag.setFile(file);
        fileHasTag.setTag(tag);
        fileHasTagRepo.save(fileHasTag);
    }


    @Override
    public String calculateChecksum(MultipartFile file) {
        try {
            // Tạo instance của MessageDigest với thuật toán SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Lấy InputStream từ MultipartFile
            try (InputStream is = file.getInputStream()) {
                byte[] byteArray = new byte[1024];
                int bytesCount;

                // Đọc từng đoạn của file và cập nhật vào MessageDigest
                while ((bytesCount = is.read(byteArray)) != -1) {
                    digest.update(byteArray, 0, bytesCount);
                }
            }

            // Tính toán giá trị hash
            byte[] hashedBytes = digest.digest();

            // Chuyển byte sang dạng hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }

    @Override
    @ValidatePermissionMember(RepoPermission.DELETE)
    public void deleteFile(Long fileId) {
        fileRepo.deleteById(fileId);
    }


}
