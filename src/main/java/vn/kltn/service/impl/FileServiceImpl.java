package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.common.RepoPermission;
import vn.kltn.dto.request.FileRequest;
import vn.kltn.dto.response.FileDataResponse;
import vn.kltn.dto.response.FileResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.File;
import vn.kltn.entity.Repo;
import vn.kltn.entity.RepoMember;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.exception.UploadFailureException;
import vn.kltn.map.FileMapper;
import vn.kltn.repository.FileRepo;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.*;
import vn.kltn.validation.HasPermission;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FILE_SERVICE")
public class FileServiceImpl implements IFileService {
    private final FileRepo fileRepo;
    private final FileMapper fileMapper;
    private final IAzureStorageService azureStorageService;
    private final IAuthenticationService authenticationService;
    private final IFileHasTagService fileHasTagService;
    private final IRepoService repoService;
    private final IUserHasKeyService userHasKeyService;
    private final IRepoMemberService repoMemberService;


    @Override
    @HasPermission(RepoPermission.CREATE)
    public FileResponse uploadFile(Long repoId, FileRequest fileRequest, MultipartFile file) {
        try {
            String publicKey = getUserPublicKey();
            byte[] fileData = file.getBytes();
            validateSignature(fileData, fileRequest.getSignature(), publicKey);
            Repo repo = repoService.getRepositoryById(repoId);
            return processValidFile(repo, fileRequest, file, publicKey);
        } catch (IOException e) {
            log.error("Error reading file: {}", file.getOriginalFilename(), e);
            throw new UploadFailureException("Lỗi khi đọc file: " + e.getMessage());
        }
    }

    private void validateSignature(byte[] fileData, String signature, String publicKey) {
        if (!verifyFileSignature(fileData, signature, publicKey)) {
            log.error("Invalid signature for file: {}", fileData);
            throw new InvalidDataException("Chữ ký không hợp lệ");
        }
    }

    // Lấy public key của user hiện tại
    private String getUserPublicKey() {
        return userHasKeyService.getPublicKeyActiveByUserAuth();
    }

    // Xử lý khi file hợp lệ và lưu vào database
    private FileResponse processValidFile(Repo repo, FileRequest fileRequest, MultipartFile file, String publicKey) {
        File fileEntity = mapToEntity(repo, fileRequest, file);
        fileEntity.setVersion(1);
        fileEntity.setPublicKey(publicKey);
        fileEntity = fileRepo.save(fileEntity);
        fileHasTagService.addFileToTag(fileEntity, fileRequest.getTags());
        // upload file to cloud
        String fileBlobName = uploadFileToCloud(file, repo.getContainerName(), repoMemberService.getSasTokenByAuthMemberWithRepo(repo));
        fileEntity.setFileBlobName(fileBlobName);
        fileEntity.setRepo(repo);
        return fileMapper.entityToResponse(fileEntity);
    }

    private File mapToEntity(Repo repo, FileRequest fileRequest, MultipartFile file) {
        File fileEntity = fileMapper.requestToEntity(fileRequest);
        fileEntity.setCheckSum(calculateChecksumHexFromFile(file));
        fileEntity.setFileSize(file.getSize());
        fileEntity.setFileType(file.getContentType());
        fileEntity.setRepo(repo);
        RepoMember uploadedBy = repoMemberService.getAuthMemberWithRepoId(repo.getId());
        fileEntity.setUploadedBy(uploadedBy);
        return fileEntity;
    }

    private String uploadFileToCloud(MultipartFile file, String containerName, String sasToken) {
        try (InputStream inputStream = file.getInputStream()) {
            return azureStorageService.uploadChunked(inputStream, file.getOriginalFilename(), containerName, sasToken, file.getSize(), 10 * 1024 * 1024);
        } catch (IOException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }

    @Override
    public String calculateChecksumHexFromFile(MultipartFile file) {
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
    public String calculateChecksumHexFromFileByte(byte[] data) {
        try {
            // Tạo instance của MessageDigest với thuật toán SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(data);
            byte[] hashedBytes = digest.digest();
            // Chuyển byte sang dạng hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidDataException(e.getMessage());
        }
    }

    @Override
    public void validateFileIntegrity(File file) {
        byte[] data = azureStorageService.downloadBlobByteData(file.getRepo().getContainerName(), file.getFileBlobName());
        String calculatedChecksum = calculateChecksumHexFromFileByte(data);
        if (!calculatedChecksum.equals(file.getCheckSum())) {
            log.error("Checksum không trùng khớp, file đã bị chỉnh sửa");
            throw new InvalidDataException("Checksum không khớp! file đã bị chỉnh sửa");
        }
    }

    @Override
    @HasPermission(RepoPermission.DELETE)
    public void deleteFile(Long fileId) {
        File file = getFileById(fileId);
        validateFileDeleted(file);
        file.setDeletedAt(LocalDateTime.now());
        Repo repo = file.getRepo();
        file.setDeletedBy(repoMemberService.getAuthMemberWithRepoId(repo.getId()));
    }

    private void validateFileDeleted(File file) {
        if (file.getDeletedAt() != null) {
            throw new InvalidDataException("File đã bị xóa");
        }
    }

    @Override
    public FileResponse restoreFile(Long fileId) {
        File file = getFileById(fileId);
        validateFileExist(file);
        validateAdminOrAuthorDeleteFile(file);
        file.setDeletedAt(null);
        file.setDeletedBy(null);
        return fileMapper.entityToResponse(file);
    }

    private void validateAdminOrAuthorDeleteFile(File file) {
        User authUser = authenticationService.getAuthUser();
        Repo repo = file.getRepo();
        User owner = repo.getOwner();
        if (authUser.getId().equals(owner.getId())) {
            return;
        }
        // chi co nguoi xoa hoac admin moi co quyen restore file
        RepoMember repoMember = repoMemberService.getAuthMemberWithRepoId(repo.getId());
        if (!repoMember.getId().equals(file.getDeletedBy().getId())) {
            throw new InvalidDataException("Không có quyền khôi phục file");
        }
    }

    private void validateFileExist(File file) {
        if (file.getDeletedAt() == null) {
            throw new InvalidDataException("File chưa bị xóa");
        }
    }

    @Override
    public File getFileById(Long fileId) {
        return fileRepo.findById(fileId).orElseThrow(() -> {
            log.warn("Không tìm thấy file với id: {}", fileId);
            return new ResourceNotFoundException("File không tồn tại");
        });
    }

    @Override
    public Long getRepoIdByFileId(Long fileId) {
        File file = getFileById(fileId);
        return file.getRepo().getId();
    }

    @Override
    @HasPermission(RepoPermission.UPDATE)
    public FileResponse updateFileMetadata(Long fileId, FileRequest fileRequest) {
        File file = getFileById(fileId);
        fileMapper.updateEntity(fileRequest, file);
        file = fileRepo.save(file);
        return fileMapper.entityToResponse(file);
    }

    @Override
    public FileDataResponse downloadFile(Long fileId) {
        File file = getFileById(fileId);
        // kiem tra tinh toan ven cua file
        validateFileIntegrity(file);
        String containerName = file.getRepo().getContainerName();
        String fileBlobName = file.getFileBlobName();
        byte[] data = azureStorageService.downloadBlobByteData(containerName, fileBlobName);
        // Xác minh chữ ký số
        verifyFileSignature(data, file.getSignature(), file.getPublicKey());
        return FileDataResponse.builder().data(data).fileType(file.getFileType()).fileName(file.getFileName() + file.getFileBlobName().substring(file.getFileBlobName().lastIndexOf('.'))).build();
    }

    public boolean verifyFileSignature(byte[] data, String signatureBase64, String publicKeyBase64) {
        try {
            // Giải mã public key từ Base64
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            // Giải mã signature từ Base64
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            // Xác minh chữ ký bằng SHA-256 với RSA
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signatureBytes);
        } catch (SignatureException | InvalidKeyException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new InvalidDataException("Lỗi xác thực chữ ký: " + e.getMessage());
        }
    }

    @Override
    public PageResponse<List<FileResponse>> advanceSearchBySpecification(Long repoId, Pageable pageable, String[] file) {
        log.info("request get all of word with specification");
        if (file != null && file.length > 0) {
            EntitySpecificationsBuilder<File> builder = new EntitySpecificationsBuilder<>();
//            Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
            Pattern pattern = Pattern.compile("([a-zA-Z0-9_.]+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
            //patten chia ra thành 5 nhóm
            // nhóm 1: từ cần tìm kiếm (có thể là tên cột hoặc tên bảng) , ví dụ: name, age, subTopic.id=> subTopic là tên bảng, id là tên cột
            // nhóm 2: toán tử tìm kiếm
            // nhóm 3: giá trị cần tìm kiếm
            // nhóm 4: dấu câu cuối cùng
            // nhóm 5: dấu câu cuối cùng
            for (String s : file) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                }
            }
            Specification<File> spec = builder.build();
            // nó trả trả về 1 spec mới
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt")));
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("repo").get("id"), repoId));
            Page<File> filePage = fileRepo.findAll(spec, pageable);
            return PaginationUtils.convertToPageResponse(filePage, pageable, fileMapper::entityToResponse);
        }
        return PaginationUtils.convertToPageResponse(fileRepo.findAll(pageable), pageable, fileMapper::entityToResponse);
    }

    @Override
    public PageResponse<List<FileResponse>> searchByTagName(Long repoId, String tagName, Pageable pageable) {
        Page<File> filePage = fileRepo.findActiveFilesByRepoIdAndTagName(repoId, tagName.trim(), pageable);
        return PaginationUtils.convertToPageResponse(filePage, pageable, fileMapper::entityToResponse);
    }

    @Override
    public PageResponse<List<FileResponse>> searchByStartDateAndEndDate(Long repoId, Pageable pageable, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startOfDay = startDate.atStartOfDay(); // 2025-03-05 00:00:00
        LocalDateTime endOfDay = endDate.atTime(23, 59, 59); // 2025-03-10 23:59:59
        Page<File> filePage = fileRepo.findActiveFilesByRepoIdAndUploadDateRange(repoId, startOfDay, endOfDay, pageable);
        return PaginationUtils.convertToPageResponse(filePage, pageable, fileMapper::entityToResponse);
    }

}
