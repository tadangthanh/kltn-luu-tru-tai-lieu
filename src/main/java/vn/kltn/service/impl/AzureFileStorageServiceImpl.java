package vn.kltn.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlockBlobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.exception.CustomBlobStorageException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.exception.UploadFailureException;
import vn.kltn.service.IFileStorageService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureFileStorageServiceImpl implements IFileStorageService {
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
}
