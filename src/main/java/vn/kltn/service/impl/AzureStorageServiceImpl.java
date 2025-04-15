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
import vn.kltn.common.CancellationToken;
import vn.kltn.entity.MemberRole;
import vn.kltn.exception.CustomBlobStorageException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.service.IAzureStorageService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureStorageServiceImpl implements IAzureStorageService {
    @Value("${azure.blob-storage.account-name}")
    private String accountName;
    private final BlobServiceClient blobServiceClient;
    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerNameDefault;

    @Override
    public String uploadChunkedWithContainerName(InputStream data, String originalFileName, String containerName, String sasToken, long length, int chunkSize) {
        try {
            String containerUrl = String.format("https://%s.blob.core.windows.net/%s?%s", accountName, containerName, sasToken);
            BlobContainerClient blobContainerClient = new BlobServiceClientBuilder().endpoint(containerUrl).buildClient().getBlobContainerClient(containerName);
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

            return blockBlobClient.getBlobName();
        } catch (IOException | BlobStorageException e) {
            log.error("Error uploading file from InputStream: {}", e.getMessage());
            throw new CustomBlobStorageException("Lỗi upload file ");
        }
    }

    @Override
    public String uploadChunkedWithContainerDefault(InputStream data, String originalFileName, long length, int chunkSize) {
        try {
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerNameDefault);
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

            return blockBlobClient.getBlobName();
        } catch (IOException | BlobStorageException e) {
            log.error("Error uploading file from InputStream: {}", e.getMessage());
            throw new CustomBlobStorageException("Lỗi upload file ");
        }
    }

    @Override
    public CompletableFuture<String> uploadChunkedWithContainerDefaultAsync(InputStream data, String originalFileName, long length, int chunkSize) {
        try {
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerNameDefault);
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
                //  In log để biết phần nào đã upload xong
                //  In log với số phần upload thành công
                System.out.println("✅ Đã upload thành công phần " + (blockNumber + 1) + " trên tổng số " + ((length + chunkSize - 1) / chunkSize) + " phần");
                blockNumber++;
            }

            // Ghép các phần lại
            blockBlobClient.commitBlockList(blockIds);

            return CompletableFuture.completedFuture(blockBlobClient.getBlobName());
        } catch (IOException | BlobStorageException e) {
            log.error("Error uploading file from InputStream: {}", e.getMessage());
            throw new CustomBlobStorageException("Lỗi upload file ");
        }
    }

    @Override
    public CompletableFuture<String> uploadChunkedWithContainerDefaultAsync(InputStream data, String originalFileName, long length, int chunkSize, CancellationToken token) {
        try {
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerNameDefault);
            String newFileName = UUID.randomUUID() + "_" + originalFileName;
            BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(newFileName).getBlockBlobClient();
            List<String> blockIds = new ArrayList<>();
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            int blockNumber = 0;

            while ((bytesRead = data.read(buffer)) != -1) {
                // Kiểm tra trạng thái hủy từ token
                if (token.isCancelled()) {
                    log.info("Upload bị hủy giữa chừng, dừng tại phần {}", blockNumber);
                    throw new CancellationException("Upload bị hủy giữa chừng");
                }

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

            return CompletableFuture.completedFuture(blockBlobClient.getBlobName());
        } catch (IOException | BlobStorageException | CancellationException e) {
            log.error("Error uploading file from InputStream: {}", e.getMessage());
            throw new CustomBlobStorageException("Lỗi upload file ");
        }
    }


    @Override
    public String copyBlob(String sourceBlobName, String destinationBlobName) {
        try {
            // Lấy client của container
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerNameDefault);

            // Tạo client cho blob nguồn và blob đích
            BlobClient sourceBlobClient = blobContainerClient.getBlobClient(sourceBlobName);
            BlobClient destinationBlobClient = blobContainerClient.getBlobClient(destinationBlobName);

            // Kiểm tra xem file nguồn có tồn tại không
            if (!sourceBlobClient.exists()) {
                throw new CustomBlobStorageException("File nguồn không tồn tại: " + sourceBlobName);
            }

            // Lấy URL của file nguồn
            String sourceUrl = sourceBlobClient.getBlobUrl();

            // Sao chép file
            destinationBlobClient.beginCopy(sourceUrl, null);

            return destinationBlobClient.getBlobName();
        } catch (BlobStorageException e) {
            log.error("Lỗi khi sao chép file: {}", e.getMessage());
            throw new CustomBlobStorageException("Lỗi khi sao chép file: " + e.getMessage());
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
     * @param containerName : tên container cần tạo SAS Token
     * @param memberRole    : quyền hạn của từng thành viên
     * @return : trả về SAS Token của thành viên với container này
     */
    @Override
    public String generatePermissionRepoByMemberRole(String containerName, MemberRole memberRole) {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        // Thiết lập thời gian hết hạn cho SAS Token
        OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(1);
        // Tạo SAS Token với quyền hạn tùy vào từng thành viên
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, generatePermissionForMember(memberRole)).setProtocol(SasProtocol.HTTPS_HTTP);  //  cho phép truy cập qua HTTPS_HTTP
        return blobContainerClient.generateSas(sasValues);
    }

    @Override
    public void deleteContainer(String containerName) {
        try {
            log.info("Deleting container:  {}", containerName);
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            if (containerClient.exists()) {
                containerClient.delete();
                log.info("Container {} delete success", containerName);
            } else {
                log.warn("Container {} not exist", containerName);
            }
        } catch (BlobStorageException e) {
            log.error("error delete container name: {}: {}", containerName, e.getMessage());
        }
    }

    @Override
    public void deleteBlob(String containerName, String blobName) {
        deleteBlobByContainerAndBlob(containerName, blobName);
    }

    @Override
    public void deleteBlob(String blobName) {
        deleteBlobByContainerAndBlob(containerNameDefault, blobName);
    }

    private void deleteBlobByContainerAndBlob(String containerName, String blobName) {
        log.info("Deleting blob '{}' in container '{}'", blobName, containerName);
        try {
            BlockBlobClient blobClient = getBlockBlobClient(containerName, blobName);

            if (!blobClient.exists()) {
                log.warn("Blob '{}' does not exist in container '{}'", blobName, containerName);
                return;
            }
            blobClient.delete();
            log.info("Deleted blob '{}' successfully", blobName);

        } catch (Exception e) {
            log.error("Failed to delete blob '{}' in container '{}': {}", blobName, containerName, e.getMessage(), e);
        }
    }

    @Override
    public void deleteBLobs(List<String> blobNames) {
        if(blobNames == null || blobNames.isEmpty()) {
            log.warn("No blobs to delete");
            return;
        }
        log.info("Deleting blobs {}", blobNames);
        for (String blobName : blobNames) {
            deleteBlobByContainerAndBlob(containerNameDefault, blobName);
        }
    }

    @Override
    public InputStream downloadBlobInputStream(String containerName, String blobName) {
        return getInputStreamBlob(containerName, blobName);
    }

    @Override
    public InputStream downloadBlobInputStream(String blobName) {
        return getInputStreamBlob(containerNameDefault, blobName);
    }

    @Override
    public CompletableFuture<InputStream> downloadBlobInputStreamAsync(String blobName) {
        return CompletableFuture.completedFuture(getInputStreamBlob(containerNameDefault, blobName));
    }

    @Override
    public File downloadToFile(String blobName, String tempDirPath) {
        try {
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerNameDefault);
            BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(blobName).getBlockBlobClient();

            // Tạo thư mục tạm nếu chưa tồn tại
            File tempDir = new File(tempDirPath);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            // Tạo file tạm để lưu
            File downloadedFile = new File(tempDirPath + File.separator + blobName);
            blockBlobClient.downloadToFile(downloadedFile.getAbsolutePath(), true); // true = overwrite if exists

            return downloadedFile;
        } catch (Exception e) {
            log.error("Lỗi khi tải file từ Azure Blob: {}", e.getMessage());
            throw new CustomBlobStorageException("Không thể tải file từ Azure Blob: " + e.getMessage());
        }
    }


    private InputStream getInputStreamBlob(String containerName, String blobName) {
        try {
            BlockBlobClient blobClient = getBlockBlobClient(containerName, blobName);
            if (!blobClient.exists()) {
                log.error("Blob not found: {}", blobName);
                throw new ResourceNotFoundException("File không tồn tại: " + blobName);
            }
            // Đọc file từ Azure Blob Storage
            return blobClient.openInputStream();
        } catch (Exception e) {
            log.error("Error downloading blob: {}", e.getMessage());
            throw new ResourceNotFoundException("Lỗi khi tải blob: " + blobName);
        }
    }

    @Override
    public byte[] downloadBlobByteData(String containerName, String blobName) {
        try (InputStream inputStream = downloadBlobInputStream(containerName, blobName)) {
            BlockBlobClient blobClient = getBlockBlobClient(containerName, blobName);

            if (!blobClient.exists()) {
                log.error("Blob not found: {}", blobName);
                throw new ResourceNotFoundException("File không tồn tại: " + blobName);
            }
            // Đọc file từ Azure Blob Storage
            return inputStream.readAllBytes();
        } catch (Exception e) {
            log.error("Error downloading blob: {}", e.getMessage());
            throw new ResourceNotFoundException("Lỗi khi tải blob: " + blobName);
        }
    }


    /**
     * Helper method to get BlockBlobClient
     */
    private BlockBlobClient getBlockBlobClient(String containerName, String blobName) {
        return blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName).getBlockBlobClient();
    }


    /**
     * Tạo quyền cho thành viên của container
     *
     * @param memberRole : quyen cua thành viên
     * @return : trả về quyền hạn của thành viên
     */
    private BlobContainerSasPermission generatePermissionForMember(MemberRole memberRole) {
        BlobContainerSasPermission permission = new BlobContainerSasPermission();
        switch (memberRole.getName()) {
            case ADMIN -> {
                permission.setCreatePermission(true);
                permission.setWritePermission(true);  // Cho phép ghi dữ liệu
                permission.setAddPermission(true);   // Cho phép thêm dữ liệu
                permission.setReadPermission(true); // Đọc nội dung tệp
                permission.setDeletePermission(true);
                permission.setListPermission(true);
            }
            case VIEWER -> {
                permission.setReadPermission(true);
                permission.setListPermission(true);
            } // Đọc nội dung tệp
            case EDITOR -> {
                permission.setCreatePermission(true);
                permission.setWritePermission(true);
                permission.setReadPermission(true);
                permission.setAddPermission(true);   // Cho phép thêm dữ liệu
                permission.setListPermission(true);
            } // UPDATE thực chất là một phần của WRITE
            default -> {
                // Không làm gì nếu quyền không được xác định
            }
        }

        return permission;
    }


}
