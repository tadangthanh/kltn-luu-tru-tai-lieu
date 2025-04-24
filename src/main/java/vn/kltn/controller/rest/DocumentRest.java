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
import vn.kltn.service.IDocumentSearchService;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.impl.UploadTokenManager;
import vn.kltn.validation.ValidFiles;

import java.io.ByteArrayInputStream;
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
    public ResponseData<String> upload(@PathVariable Long folderId,@ValidFiles  @RequestPart("files") MultipartFile[] files) {
        // Tạo token mới cho mỗi yêu cầu upload
        CancellationToken token = new CancellationToken();
        // Đăng ký token vào registry và lấy uploadId
        String uploadId = UUID.randomUUID().toString();
        uploadTokenManager.registerToken(uploadId, token);
        token.setUploadId(uploadId);
        documentService.uploadDocumentWithParent(folderId, FileUtil.getFileBufferList(files), token);
        return new ResponseData<>(200, "Đang tải lên...", uploadId);
    }

    @DeleteMapping("/{documentId}")
    public ResponseData<Void> softDelete(@PathVariable Long documentId) {
        documentService.deleteResourceById(documentId);
        return new ResponseData<>(204, "Xóa thành công", null);
    }

    @DeleteMapping("/{documentId}/hard")
    public ResponseData<Void> hardDelete(@PathVariable Long documentId) {
        documentService.hardDeleteResourceById(documentId);
        return new ResponseData<>(204, "Xóa thành công", null);
    }

    @PostMapping("/{documentId}/copy")
    public ResponseData<DocumentResponse> copy(@PathVariable Long documentId) {
        return new ResponseData<>(201, "Thành công", documentService.copyDocumentById(documentId));
    }

    @PostMapping("/{documentId}/restore")
    public ResponseData<DocumentResponse> restore(@PathVariable Long documentId) {
        return new ResponseData<>(200, "Thành công", documentService.restoreResourceById(documentId));
    }

    @PutMapping("/{documentId}")
    public ResponseData<DocumentResponse> update(@PathVariable Long documentId, @Valid @RequestBody DocumentRequest documentRequest) {
        return new ResponseData<>(200, "Thành công", documentService.updateDocumentById(documentId, documentRequest));
    }

    @PutMapping("/{documentId}/move/{folderId}")
    public ResponseData<DocumentResponse> moveDocumentToFolder(@PathVariable Long documentId, @PathVariable Long folderId) {
        return new ResponseData<>(200, "Thành công", documentService.moveResourceToFolder(documentId, folderId));
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
        return new ResponseData<>(200, "Thành công", documentService.getResourceById(documentId));
    }
    @PostMapping("/save-editor")
    public ResponseEntity<Map<String, Object>> saveDocument(@RequestBody Map<String, Object> documentRequest) {
        System.out.println("📥 Callback received from OnlyOffice:");
        System.out.println(documentRequest); // log để xem body OnlyOffice gửi lên

        // Luôn trả về error = 0 để tránh lỗi trên OnlyOffice, kể cả khi không có key
        String documentId = (String) documentRequest.get("key");
        if (documentId == null) {
            System.out.println("⚠️ Missing documentId (key), nhưng vẫn trả về thành công để tránh lỗi OnlyOffice.");
            return ResponseEntity.ok(Map.of("error", 0));  // vẫn trả về thành công!
        }

        // TODO: xử lý lưu file nếu status = 6 (completed)
        // hoặc bạn có thể log lại toàn bộ để test thử

        return ResponseEntity.ok(Map.of("error", 0));
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
