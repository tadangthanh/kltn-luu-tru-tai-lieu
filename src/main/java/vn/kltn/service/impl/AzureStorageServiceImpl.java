package vn.kltn.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlockBlobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.dto.response.UploadProgressDTO;
import vn.kltn.exception.CustomBlobStorageException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.service.IAzureStorageService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureStorageServiceImpl implements IAzureStorageService {
    private final BlobServiceClient blobServiceClient;
    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerNameDefault;
    private final SimpMessagingTemplate messagingTemplate;

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
            int totalChunks = (int) ((length + chunkSize - 1) / chunkSize);
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            while ((bytesRead = data.read(buffer)) != -1) {
                String blockId = Base64.getEncoder().encodeToString(String.format("%06d", blockNumber).getBytes()); // Tạo Block ID
                blockBlobClient.stageBlock(blockId, new ByteArrayInputStream(buffer, 0, bytesRead), bytesRead);  // Upload từng phần
                blockIds.add(blockId);
                //  In log để biết phần nào đã upload xong
                //  In log với số phần upload thành công
                System.out.println("✅ Đã upload thành công phần " + (blockNumber + 1) + " trên tổng số " + ((length + chunkSize - 1) / chunkSize) + " phần");
                blockNumber++;
                int progressPercent = (int) ((blockNumber * 100.0) / totalChunks);
                if (blockNumber == totalChunks) {
                    progressPercent = 100; // đảm bảo chunk cuối là 100%
                }

                messagingTemplate.convertAndSendToUser(
                        email,
                        "/topic/upload-documents",
                        new UploadProgressDTO(originalFileName, blockNumber, totalChunks, progressPercent)
                );
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
    public String copyBlob(String sourceBlobName) {
        try {
            // Lấy client của container
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerNameDefault);

            // Tạo client cho blob nguồn và blob đích
            BlobClient sourceBlobClient = blobContainerClient.getBlobClient(sourceBlobName);
            String destinationBlobName = UUID.randomUUID() + "_" + sourceBlobName.substring(sourceBlobName.indexOf("_") + 1, sourceBlobName.lastIndexOf(".") - 1) + "_copy" + sourceBlobName.substring(sourceBlobName.lastIndexOf("."));
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
        if (blobNames == null || blobNames.isEmpty()) {
            log.warn("No blobs to delete");
            return;
        }
        log.info("Deleting blobs {}", blobNames);
        for (String blobName : blobNames) {
            deleteBlobByContainerAndBlob(containerNameDefault, blobName);
        }
    }

    @Override
    public InputStream downloadBlobInputStream(String blobName) {
        return getInputStreamBlob(containerNameDefault, blobName);
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

    /**
     * Helper method to get BlockBlobClient
     */
    private BlockBlobClient getBlockBlobClient(String containerName, String blobName) {
        return blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName).getBlockBlobClient();
    }


}
