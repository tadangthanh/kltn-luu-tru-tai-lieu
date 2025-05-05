package vn.kltn.controller.rest;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.common.CancellationToken;
import vn.kltn.common.TokenType;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.*;
import vn.kltn.entity.Document;
import vn.kltn.entity.User;
import vn.kltn.repository.util.FileUtil;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IJwtService;
import vn.kltn.service.IUserService;
import vn.kltn.service.impl.UploadTokenManager;
import vn.kltn.validation.ValidFiles;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static vn.kltn.repository.util.FileUtil.generateFileName;

@RequiredArgsConstructor
@RequestMapping("/api/v1/documents")
@RestController
@Validated
@Slf4j(topic = "DOCUMENT_REST")
public class DocumentRest {
    private final IDocumentService documentService;
    private final UploadTokenManager uploadTokenManager;
    private final IJwtService jwtService;
    private final IUserService userService;

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

    @PostMapping("/save-editor/{accessToken}")
    public ResponseEntity<Map<String, Object>> saveDocument(@RequestBody Map<String, Object> documentRequest, @PathVariable String accessToken) {
        log.info("saveDocument: {}", documentRequest);
        String email = jwtService.extractEmail(accessToken, TokenType.ACCESS_TOKEN);
        User user = userService.getUserByEmail(email);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String documentIdStr = (String) documentRequest.get("key");
        Long documentId = null;
        try {
            documentId = Long.parseLong(documentIdStr.split("-")[0]);
        } catch (NumberFormatException e) {
            log.error(" Không thể parse documentId từ key: " + documentIdStr);
            return ResponseEntity.ok(Map.of("error", 0)); // Trả về thành công để tránh lỗi OnlyOffice
        }
        System.out.println("document request: " + documentRequest);

        Integer status = (Integer) documentRequest.get("status");
        String fileUrl = (String) documentRequest.get("url");

        if ((status == 2) && fileUrl != null) {
            try {
                // Tải file từ OnlyOffice server
                byte[] fileData = downloadFileFromOnlyOffice(fileUrl);
                documentService.updateDocumentEditor(documentId, fileData);
            } catch (IOException e) {
                // Tuyệt đối không trả lỗi cho OnlyOffice, chỉ log
            }
        } else {
            System.out.println("ℹ️ Không xử lý lưu vì status = " + status + " hoặc thiếu fileUrl.");
        }

        return ResponseEntity.ok(Map.of("error", 0));
    }

    private byte[] downloadFileFromOnlyOffice(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (InputStream in = connection.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        }
    }



    @GetMapping("/open")
    public ResponseEntity<InputStreamResource> openDoc(@RequestParam(value = "documentId") Long documentId, @RequestHeader(value = HttpHeaders.RANGE, defaultValue = "") String range) {
        DocumentDataResponse documentDataResponse = documentService.openDocumentById(documentId);

        if (!range.isEmpty()) {
            // Tạo response cho một phần của tài liệu nếu Range header được gửi
            String[] rangeParts = range.replace("bytes=", "").split("-");
            long start = Long.parseLong(rangeParts[0]);
            long end = rangeParts.length > 1 ? Long.parseLong(rangeParts[1]) : documentDataResponse.getData().length - 1;
            byte[] dataRange = Arrays.copyOfRange(documentDataResponse.getData(), (int) start, (int) end + 1);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + documentDataResponse.getName() + "\"").header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + documentDataResponse.getData().length).contentType(MediaType.parseMediaType(documentDataResponse.getType())).body(new InputStreamResource(new ByteArrayInputStream(dataRange)));
        }

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + documentDataResponse.getName() + "\"").contentType(MediaType.parseMediaType(documentDataResponse.getType())).body(new InputStreamResource(new ByteArrayInputStream(documentDataResponse.getData())));
    }

    @GetMapping("/{documentId}/download")
    public void downloadDoc(@PathVariable Long documentId, HttpServletResponse response) throws IOException {
        Document document = documentService.getItemByIdOrThrow(documentId);
        try (InputStream inputStream = documentService.download(documentId)) {
            String fileName = generateFileName(document.getCurrentVersion().getBlobName());
            String mimeType = URLConnection.guessContentTypeFromName(fileName);
            if (mimeType == null) {
                mimeType = "application/octet-stream"; // fallback
            }

            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            response.flushBuffer();
        }
    }


    @GetMapping("/{documentId}/view")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long documentId) {
        // 2. Lấy dữ liệu từ Azure Blob
        Document document = documentService.getItemByIdOrThrow(documentId);
        InputStream inputStream = documentService.download(documentId);

        // 3. Trả về stream cho OnlyOffice
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getName() + "\"").body(new InputStreamResource(inputStream));
    }

    @GetMapping("/{documentId}/onlyoffice-config")
    public ResponseData<OnlyOfficeConfig> getOnlyOfficeConfig(@PathVariable Long documentId) {
        return new ResponseData<>(200, "Thành công", documentService.getOnlyOfficeConfig(documentId));
    }

}
