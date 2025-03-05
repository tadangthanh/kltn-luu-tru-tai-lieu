package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.FileShareRequest;
import vn.kltn.dto.response.FileDataResponse;
import vn.kltn.dto.response.FileShareResponse;
import vn.kltn.entity.File;
import vn.kltn.entity.FileShare;
import vn.kltn.entity.Repo;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.FileShareMapper;
import vn.kltn.repository.FileShareRepo;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IFileService;
import vn.kltn.service.IFileShareService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j(topic = "FILE_SHARE_SERVICE")
@RequiredArgsConstructor
@Transactional
public class FileShareServiceImpl implements IFileShareService {
    private final FileShareRepo fileShareRepo;
    private final FileShareMapper fileShareMapper;
    private final PasswordEncoder passwordEncoder;
    private final IAzureStorageService azureStorageService;
    private final IFileService fileService;

    @Override
    public FileShareResponse createFileShareLink(Long fileId, FileShareRequest request) {
        File file = fileService.getFileById(fileId);
        FileShare fileShare = fileShareRepo.findByFileId(fileId).orElseGet(() -> createNewFileShare(file, request));

        updateFileShare(fileShare, request);
        fileShareRepo.save(fileShare);

        return fileShareMapper.toResponse(fileShare);
    }

    private FileShare createNewFileShare(File file, FileShareRequest request) {
        FileShare fileShare = new FileShare();
        fileShare.setToken(UUID.randomUUID().toString());
        fileShare.setFile(file);
        updateFileShare(fileShare, request);
        return fileShare;
    }

    private void updateFileShare(FileShare fileShare, FileShareRequest request) {
        fileShare.setExpireAt(request.getExpireAt());
        fileShare.setPasswordHash(
                request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : null
        );
    }


    @Override
    public FileDataResponse viewFile(String token, String password) {
        FileShare fileShare = getShareFileByToken(token);
        // Kiểm tra thời gian hết hạn
        if (isExpired(fileShare)) {
            throw new InvalidDataException("File share is expired");
        }
        String passwordHash = fileShare.getPasswordHash();
        // validate password
        validatePassword(password, passwordHash);
        return mapFileToFileDataResponse(fileShare.getFile());
    }

    private FileDataResponse mapFileToFileDataResponse(File file) {
        Repo repo = file.getRepo();
        String containerName = repo.getContainerName();
        String fileBlobName = file.getFileBlobName();
        try (InputStream inputStream = azureStorageService.downloadBlob(containerName, fileBlobName)) {

            return FileDataResponse.builder()
                    .data(inputStream.readAllBytes())
                    .fileName(file.getFileName()+file.getFileBlobName().substring(file.getFileBlobName().lastIndexOf('.')))
                    .fileType(file.getFileType())
                    .build();
        } catch (IOException e) {
            log.error("Error reading file from Azure Storage: {}", e.getMessage());
            throw new InvalidDataException("Error reading file");
        }
    }


    private void validatePassword(String password, String passwordHash) {
        if (passwordHash != null) {
            if (password == null || password.isEmpty()) {
                throw new InvalidDataException("Password is required");
            }
            if (!isMatchPassword(password, passwordHash)) {
                throw new InvalidDataException("Password is incorrect");
            }
        }
    }

    private boolean isMatchPassword(String password, String passwordHash) {
        return passwordEncoder.matches(password, passwordHash);
    }

    @Override
    public FileShare getShareFileByToken(String token) {
        return fileShareRepo.findByToken(token).orElseThrow(() -> {
            log.warn("file share not found with token: {}", token);
            return new ResourceNotFoundException("File share not found");
        });
    }

    @Override
    public void deleteFileShareById(Long id) {
        fileShareRepo.deleteById(id);
    }

    private boolean isExpired(FileShare fileShare) {
        return fileShare.getExpireAt() != null && fileShare.getExpireAt().isBefore(LocalDateTime.now());
    }
}
