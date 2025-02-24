package vn.kltn.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.sas.SasProtocol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.common.RepoPermission;
import vn.kltn.exception.CustomBlobStorageException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.exception.UploadFailureException;
import vn.kltn.service.IAzureStorageService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureStorageServiceImpl implements IAzureStorageService {
    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;
    private final BlobServiceClient blobServiceClient;
    @Value("${azure.blob-storage.connection-string}")
    private String connectionString;

    @Override
    public String upload(MultipartFile file) {
        try {
            String blobFileName = System.currentTimeMillis() + "_" + UUID.randomUUID();
            BlobClient blobClient = this.blobServiceClient.getBlobContainerClient(this.containerName).getBlobClient(blobFileName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            return blobFileName;
        } catch (IOException var4) {
            throw new UploadFailureException("Lỗi khi upload file");
        }

    }

    @Override
    public String upload(InputStream data, String originalFileName, long length) {
        // lấy container lưu file
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        String newFileName = UUID.randomUUID() + "_" + originalFileName.substring(originalFileName.lastIndexOf("\\") + 1);
        // tạo blob client
        BlobClient blobClient = blobContainerClient.getBlobClient(newFileName);
        // upload file lên blob đồng thời set overwrite = true để ghi đè file cũ
        blobClient.upload(data, length, true);
        return blobClient.getBlobUrl();
    }

    @Override
    public String uploadChunked(InputStream data, String originalFileName, long length, int chunkSize) {
        try {

            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
            String newFileName = UUID.randomUUID() + "_" + originalFileName;
            BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(newFileName).getBlockBlobClient();

            List<String> blockIds = new ArrayList<>();
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            int blockNumber = 0;

            while ((bytesRead = data.read(buffer)) != -1) {
                String blockId = Base64.getEncoder().encodeToString(String.format("%06d", blockNumber).getBytes()); // Tạo Block ID
                blockBlobClient.stageBlock(blockId, new ByteArrayInputStream(buffer, 0, bytesRead), bytesRead);  // Upload từng phần
                blockIds.add(blockId);
                // 📌 In log để biết phần nào đã upload xong
                // 📌 In log với số phần upload thành công
                System.out.println("✅ Đã upload thành công phần " + (blockNumber + 1) + " trên tổng số " + ((length + chunkSize - 1) / chunkSize) + " phần");
                blockNumber++;
            }

            // Ghép các phần lại
            blockBlobClient.commitBlockList(blockIds);

            return blockBlobClient.getBlobUrl();
        } catch (IOException | BlobStorageException e) {
            log.error("Error uploading file from InputStream: {}", e.getMessage());
            throw new CustomBlobStorageException("Lỗi upload file");
        }
    }

    @Override
    public String getBlobUrl(String blobName) {
        // Truy cập Blob Storage và tạo URL không cần SAS Token
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(this.connectionString)
                .buildClient();

        BlobClient blobClient = blobServiceClient
                .getBlobContainerClient(this.containerName)
                .getBlobClient(blobName);

        // Trả về URL cơ bản của Blob
        return blobClient.getBlobUrl();
    }

    @Override
    public boolean deleteBlob(String blobName) {
        BlobClient blobClient = this.blobServiceClient.getBlobContainerClient(this.containerName).getBlobClient(blobName);
        return blobClient.deleteIfExists();
    }

    @Override
    public String upload(InputStream data, long length, String fileName, String contentType) {
        try {
            String blobFileName = System.currentTimeMillis() + "_" + fileName;
            BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobFileName);
            blobClient.upload(data, length, true);
            return blobFileName;
        } catch (Exception e) {
            log.error("Error uploading file from InputStream: {}", e.getMessage());
            throw new UploadFailureException("Lỗi khi upload file từ InputStream");
        }
    }

    @Override
    public InputStream downloadBlob(String blobName) {
        try {
            BlobClient blobClient = blobServiceClient
                    .getBlobContainerClient(containerName)
                    .getBlobClient(blobName);

            if (!blobClient.exists()) {
                log.error("Blob not found: {}", blobName);
                throw new ResourceNotFoundException("Blob không tồn tại: " + blobName);
            }

            return blobClient.openInputStream();
        } catch (Exception e) {
            log.error("Error downloading blob: {}", e.getMessage());
            throw new ResourceNotFoundException("Lỗi khi tải blob: " + blobName);
        }
    }

    /**
     * @param repoName : tên repository
     */
    @Override
    public void createContainerForRepository(String repoName) {
        // container k co ki tu dac biet, khoang trang va moi container la duy nhat
        String containerName = repoName.toLowerCase().replaceAll("[^a-z0-9-]", "").replaceAll("^-|-$", "");
        blobServiceClient.createBlobContainer(containerName);
    }

    // tạo quyền cho thành viên của repo tùy vào quyền của từng thành viên

    /**
     * @param containerName  : tên container cần tạo SAS Token
     * @param permissionList : danh sách quyền hạn của từng thành viên
     * @return : trả về SAS Token của thành viên với container này
     */
    @Override
    public String generatePermissionForMemberRepo(String containerName, Set<RepoPermission> permissionList) {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        // Thiết lập thời gian hết hạn (ví dụ: 24 giờ)
        OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(24);
        // Tạo SAS Token với quyền hạn tùy vào từng thành viên
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, generatePermissionForMember(permissionList))
                .setProtocol(SasProtocol.HTTPS_HTTP);  //  cho phép truy cập qua HTTPS_HTTP
        return blobContainerClient.generateSas(sasValues);
    }

    @Override
    public boolean deleteContainer(String containerName) {
        try {
            log.info("Deleting container:  {}", containerName);
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            if (containerClient.exists()) {
                containerClient.delete();
                log.info("Container {} delete success", containerName);
                return true;
            } else {
                log.warn("Container {} not exist", containerName);
                return false;
            }
        } catch (BlobStorageException e) {
            log.error("error delete container name: {}: {}", containerName, e.getMessage());
            return false;
        }
    }

    /**
     * Tạo quyền cho thành viên của container
     *
     * @param permissionList : danh sách quyền hạn của từng thành viên
     * @return : trả về quyền hạn của thành viên
     */
    private BlobContainerSasPermission generatePermissionForMember(Set<RepoPermission> permissionList) {
        BlobContainerSasPermission permission = new BlobContainerSasPermission();
        for (RepoPermission permissionRepo : permissionList) {
            switch (permissionRepo) {
                case CREATE:
                    permission.setCreatePermission(true);
                    break;
                case READ:
                    permission.setReadPermission(true);
                    break;
                case UPDATE, WRITE:
                    permission.setWritePermission(true);
                    break;
                case DELETE:
                    permission.setDeletePermission(true);
                    break;
                case LIST:
                    permission.setListPermission(true);
                    break;
                case ADD:
                    permission.setAddPermission(true);
                    break;
                default:
                    break;
            }
        }
        return permission;
    }

    /**
     * Tạo quyền full cho container
     *
     * @return
     */
    // tao quyen full cho container, thuong la danh cho nguoi tao container la quyen cao nhat
    private BlobContainerSasPermission generateFullPermissionContainer() {
        return new BlobContainerSasPermission()
                .setReadPermission(true)
                .setAddPermission(true)
                .setCreatePermission(true)
                .setDeletePermission(true)
                .setListPermission(true)
                .setWritePermission(true);
    }


}
