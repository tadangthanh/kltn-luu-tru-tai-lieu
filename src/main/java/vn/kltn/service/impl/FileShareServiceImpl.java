package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.FileShareRequest;
import vn.kltn.dto.response.FileShareResponse;
import vn.kltn.dto.response.FileShareView;
import vn.kltn.entity.File;
import vn.kltn.entity.FileShare;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.FileShareMapper;
import vn.kltn.repository.FileRepo;
import vn.kltn.repository.FileShareRepo;
import vn.kltn.service.IAzureStorageService;
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
    private final FileRepo fileRepo;
    private final IAzureStorageService azureStorageService;

    @Override
    public FileShareResponse shareFile(FileShareRequest fileShareRequest) {
        File file = fileRepo.findById(fileShareRequest.getFileId()).orElseThrow(() ->
                new ResourceNotFoundException("File not found"));
        FileShare fileShare = fileShareMapper.toEntity(fileShareRequest);
        fileShare.setToken(UUID.randomUUID().toString());
        fileShare.setPasswordHash(passwordEncoder.encode(fileShareRequest.getPassword()));
        fileShare = fileShareRepo.save(fileShare);
        fileShare.setFile(file);

        FileShareResponse fileShareResponse = new FileShareResponse();
        fileShareResponse.setToken(fileShare.getToken());
        fileShareResponse.setFileName(file.getFileName());
        return fileShareResponse;
    }

    @Override
    public FileShareView viewFile(String token, String password) {
        FileShare fileShare = fileShareRepo.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("file share not found with token: {}", token);
                    return new ResourceNotFoundException("File share not found");
                });
        // Kiểm tra thời gian hết hạn
        if (fileShare.getExpireAt() != null && fileShare.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new InvalidDataException("File share is expired");
        }
        // Nếu có mật khẩu cài đặt, kiểm tra mật khẩu
        if (fileShare.getPasswordHash() != null) {
            if (password == null || password.isEmpty()) {
                throw new InvalidDataException("Password is required");
            }
            if (!passwordEncoder.matches(password, fileShare.getPasswordHash())) {
                throw new InvalidDataException("Password is incorrect");
            }
        }
        try (InputStream inputStream = azureStorageService.downloadBlob(fileShare.getFile().getRepo().getContainerName(), fileShare.getFile().getFileBlobName())) {
            File file = fileShare.getFile();
            FileShareView fileShareView = new FileShareView();
            fileShareView.setFileName(file.getFileName());
            fileShareView.setContentType(file.getFileType());
            fileShareView.setFileBytes(inputStream.readAllBytes());
            return fileShareView;
        } catch (IOException e) {
            log.error("Error reading file from Azure Storage: {}", e.getMessage());
            throw new InvalidDataException("Error reading file");
        }
    }
}
