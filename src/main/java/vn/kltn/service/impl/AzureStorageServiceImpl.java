package vn.kltn.service.impl;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.sas.SasProtocol;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.kltn.common.RepoPermission;
import vn.kltn.exception.CustomBlobStorageException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.service.IAzureStorageService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AzureStorageServiceImpl implements IAzureStorageService {
    @Value("${azure.blob-storage.account-name}")
    private String accountName;
    private final BlobServiceClient blobServiceClient;

    @Override
    public String uploadChunked(InputStream data, String originalFileName, String containerName, String sasToken, long length, int chunkSize) {
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
    public String generatePermissionRepo(String containerName, Set<RepoPermission> permissionList) {
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        // Thiết lập thời gian hết hạn cho SAS Token
        OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(1);
        // Tạo SAS Token với quyền hạn tùy vào từng thành viên
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, generatePermissionForMember(permissionList)).setProtocol(SasProtocol.HTTPS_HTTP);  //  cho phép truy cập qua HTTPS_HTTP
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
        log.info("Deleting blob '{}' in container '{}'", blobName, containerName);

        try {
            BlockBlobClient blobClient = getBlobClient(containerName, blobName);

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
    public InputStream downloadBlob(String containerName, String blobName) {
        try {
            BlockBlobClient blobClient = getBlobClient(containerName, blobName);

            if (!blobClient.exists()) {
                log.error("Blob not found: {}", blobName);
                throw new ResourceNotFoundException("File không tồn tại: " + blobName);
            }

            // Đọc file từ Azure Blob Storage
            InputStream inputStream = blobClient.openInputStream();

            // Chuyển đổi file nếu cần thiết
            if (blobName.endsWith(".docx")) {
                return convertDocxToPdf(inputStream);
            } else if (blobName.endsWith(".xlsx")) {
                return convertXlsxToPdf(inputStream);
            } else if (blobName.endsWith(".pptx")) {
                return convertPptxToPdf(inputStream);
            } else {
                return inputStream; // Các file khác trả về nguyên bản
            }
        } catch (Exception e) {
            log.error("Error downloading blob: {}", e.getMessage());
            throw new ResourceNotFoundException("Lỗi khi tải blob: " + blobName);
        }
    }
    private InputStream convertXlsxToPdf(InputStream xlsxInputStream) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook(xlsxInputStream);
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

        Document pdfDocument = new Document();
        PdfWriter.getInstance(pdfDocument, pdfOutputStream);
        pdfDocument.open();


        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                StringBuilder rowData = new StringBuilder();
                for (Cell cell : row) {
                    rowData.append(cell.toString()).append("\t");
                }
                pdfDocument.add(new Paragraph(rowData.toString()));
            }
        }

        pdfDocument.close();
        workbook.close();

        return new ByteArrayInputStream(pdfOutputStream.toByteArray());
    }
    private InputStream convertPptxToPdf(InputStream pptxInputStream) throws Exception {
        XMLSlideShow ppt = new XMLSlideShow(pptxInputStream);
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

        Document pdfDocument = new Document();
        PdfWriter.getInstance(pdfDocument, pdfOutputStream);
        pdfDocument.open();


        for (XSLFSlide slide : ppt.getSlides()) {
            pdfDocument.add(new Paragraph("Slide: "));
            for (XSLFShape shape : slide.getShapes()) {
                if (shape instanceof XSLFTextShape textShape) {
                    pdfDocument.add(new Paragraph(textShape.getText()));
                }
            }
            pdfDocument.newPage();
        }

        pdfDocument.close();
        ppt.close();

        return new ByteArrayInputStream(pdfOutputStream.toByteArray());
    }

    private InputStream convertDocxToPdf(InputStream docxInputStream) throws Exception {
        XWPFDocument document = new XWPFDocument(docxInputStream);
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        Document pdfDocument = new Document();
        PdfWriter.getInstance(pdfDocument, pdfOutputStream);
        pdfDocument.open();

        for (var para : document.getParagraphs()) {
            pdfDocument.add(new Paragraph(para.getText()));
        }

        pdfDocument.close();
        document.close();

        return new ByteArrayInputStream(pdfOutputStream.toByteArray()); // Trả về PDF dưới dạng InputStream
    }

    /**
     * Helper method to get BlockBlobClient
     */
    private BlockBlobClient getBlobClient(String containerName, String blobName) {
        return blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(blobName)
                .getBlockBlobClient();
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


}
