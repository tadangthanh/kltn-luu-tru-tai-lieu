package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.common.CancellationToken;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.*;
import vn.kltn.repository.util.FileUtil;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.impl.UploadTokenManager;
import vn.kltn.validation.ValidFiles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/documents")
@RestController
@Validated
public class DocumentRest {
    private final IDocumentService documentService;
    private final UploadTokenManager uploadTokenManager;

    @PostMapping
    public ResponseData<String> uploadWithoutParent(@ValidFiles @RequestPart("files") MultipartFile[] files) {
        // Tạo token mới cho mỗi yêu cầu upload
        CancellationToken token = new CancellationToken();
        // Đăng ký token vào registry và lấy uploadId
        String uploadId = UUID.randomUUID().toString();
        uploadTokenManager.registerToken(uploadId, token);
        token.setUploadId(uploadId);
        documentService.uploadDocumentEmptyParent(FileUtil.getFileBufferList(files), token);
        return new ResponseData<>(200, "Đang tải ....", uploadId);
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelUpload(@RequestParam("uploadId") String uploadId) {
        Optional<CancellationToken> token = uploadTokenManager.getToken(uploadId);
        if (token.isPresent()) {
            token.get().cancel();
            // Sau khi hủy, bạn có thể xóa token khỏi registry nếu không cần thiết nữa
            uploadTokenManager.removeToken(uploadId);
            return ResponseEntity.ok("Upload với uploadId " + uploadId + " đã bị hủy.");
        } else {
            return ResponseEntity.badRequest().body("Không tìm thấy upload với id: " + uploadId);
        }
    }

    @PostMapping("/folder/{folderId}")
    public ResponseData<String> upload(@PathVariable Long folderId, @ValidFiles @RequestPart("files") MultipartFile[] files) {
        // Tạo token mới cho mỗi yêu cầu upload
        CancellationToken token = new CancellationToken();
        // Đăng ký token vào registry và lấy uploadId
        String uploadId = UUID.randomUUID().toString();
        uploadTokenManager.registerToken(uploadId, token);
        token.setUploadId(uploadId);
        documentService.uploadDocumentWithParent(folderId, FileUtil.getFileBufferList(files), token);
        return new ResponseData<>(200, "Đang tải lên...", uploadId);
    }


    @DeleteMapping("/{documentId}/hard")
    public ResponseData<Void> hardDelete(@PathVariable Long documentId) {
        documentService.hardDeleteItemById(documentId);
        return new ResponseData<>(204, "Xóa thành công", null);
    }

    @PostMapping("/{documentId}/copy")
    public ResponseData<ItemResponse> copy(@PathVariable Long documentId) {
        return new ResponseData<>(201, "Thành công", documentService.copyDocumentById(documentId));
    }

    @PostMapping("/{documentId}/restore")
    public ResponseData<DocumentResponse> restore(@PathVariable Long documentId) {
        return new ResponseData<>(200, "Thành công", documentService.restoreItemById(documentId));
    }

    @PutMapping("/{documentId}")
    public ResponseData<DocumentResponse> update(@PathVariable Long documentId, @Valid @RequestBody DocumentRequest documentRequest) {
        return new ResponseData<>(200, "Thành công", documentService.updateDocumentById(documentId, documentRequest));
    }

    @PutMapping("/{documentId}/move/{folderId}")
    public ResponseData<DocumentResponse> moveDocumentToFolder(@PathVariable Long documentId, @PathVariable Long folderId) {
        return new ResponseData<>(200, "Thành công", documentService.moveItemToFolder(documentId, folderId));
    }

    @GetMapping
    public ResponseData<PageResponse<List<DocumentResponse>>> search(Pageable pageable, @RequestParam(required = false, value = "documents") String[] documents) {
        return new ResponseData<>(200, "Thành công", documentService.searchByCurrentUser(pageable, documents));
    }

    @GetMapping("/search-metadata")
    public ResponseData<List<DocumentIndexResponse>> searchMetadata(@RequestParam(required = false, value = "query") String query, Pageable pageable) {
        return new ResponseData<>(200, "Thành công", documentService.searchMetadata(query, pageable));
    }

    @GetMapping("/{documentId}")
    public ResponseData<DocumentResponse> getDocumentById(@PathVariable Long documentId) {
        return new ResponseData<>(200, "Thành công", documentService.getItemById(documentId));
    }

    @PostMapping("/save-editor")
    public ResponseEntity<Map<String, Object>> saveDocument(@RequestBody Map<String, Object> documentRequest) {
        System.out.println("📥 Callback received from OnlyOffice:");
        System.out.println(documentRequest); // Log để kiểm tra body OnlyOffice gửi lên

        // Kiểm tra xem có tồn tại "key" (documentId) không
        String documentId = (String) documentRequest.get("key");
        if (documentId == null) {
            System.out.println("⚠️ Missing documentId (key), nhưng vẫn trả về thành công để tránh lỗi OnlyOffice.");
            return ResponseEntity.ok(Map.of("error", 0));  // Vẫn trả về thành công!
        }

        // Lấy thông tin status từ OnlyOffice callback
        Integer status = (Integer) documentRequest.get("status");

        // Kiểm tra nếu status là 6 (hoàn thành) hoặc 2 (chỉnh sửa)
        if (status != null && (status == 6 || status == 2)) {
//            // TODO: Xử lý lưu file khi status = 6 hoặc 2
//            // Trong trường hợp status = 6, bạn có thể tải file từ OnlyOffice về và lưu vào Azure Blob Storage.
//
//            String fileUrl = (String) documentRequest.get("url"); // URL tải tài liệu sau khi chỉnh sửa
//            if (fileUrl != null) {
//                // Ví dụ bạn có thể tải file về từ URL này và lưu lại trên Azure
//                byte[] fileData = downloadFile(fileUrl); // Hàm tải file từ URL (cần implement)
//
//                // Gọi service để lưu file lên Azure Blob
//                azureStorageService.uploadChunkedWithContainerDefault(fileData, "documents/" + documentId + ".docx");
//
//                System.out.println("📤 File đã được lưu lên Azure Blob Storage.");
//            } else {
//                System.out.println("⚠️ Không có URL file trong callback.");
//            }
        }

        // Trả về thành công dù có lỗi hay không, tránh lỗi OnlyOffice
        return ResponseEntity.ok(Map.of("error", 0));
    }

    // Hàm tải file từ URL
    private byte[] downloadFile(String fileUrl) {
        // Sử dụng HttpClient hoặc thư viện thích hợp để tải file về
        // Đây chỉ là một ví dụ đơn giản, bạn cần triển khai lại phương thức này theo cách của mình.
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            InputStream inputStream = connection.getInputStream();

            return inputStream.readAllBytes(); // Đọc toàn bộ nội dung của file
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @GetMapping("/open")
    public ResponseEntity<InputStreamResource> openDoc(@RequestParam(value = "documentId") Long documentId,
                                                       @RequestHeader(value = HttpHeaders.RANGE, defaultValue = "") String range) {
        DocumentDataResponse documentDataResponse = documentService.openDocumentById(documentId);

        if (!range.isEmpty()) {
            // Tạo response cho một phần của tài liệu nếu Range header được gửi
            String[] rangeParts = range.replace("bytes=", "").split("-");
            long start = Long.parseLong(rangeParts[0]);
            long end = rangeParts.length > 1 ? Long.parseLong(rangeParts[1]) : documentDataResponse.getData().length - 1;
            byte[] dataRange = Arrays.copyOfRange(documentDataResponse.getData(), (int) start, (int) end + 1);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + documentDataResponse.getName() + "\"")
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + documentDataResponse.getData().length)
                    .contentType(MediaType.parseMediaType(documentDataResponse.getType()))
                    .body(new InputStreamResource(new ByteArrayInputStream(dataRange)));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + documentDataResponse.getName() + "\"")
                .contentType(MediaType.parseMediaType(documentDataResponse.getType()))
                .body(new InputStreamResource(new ByteArrayInputStream(documentDataResponse.getData())));
    }

}
