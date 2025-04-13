package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.*;
import vn.kltn.index.DocumentIndex;
import vn.kltn.service.IDocumentService;

import java.io.ByteArrayInputStream;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/documents")
@RestController
public class DocumentRest {
    private final IDocumentService documentService;

    @PostMapping
    public ResponseData<Void> uploadWithoutParent(@RequestPart("files") MultipartFile[] files) {
        documentService.uploadDocumentWithoutParent(files);
        return new ResponseData<>(201, "Đang tải ....");
    }

    @PostMapping("/folder/{folderId}")
    public ResponseData<DocumentResponse> upload(@PathVariable Long folderId, @RequestPart("file") MultipartFile file, @Valid @RequestPart("data") DocumentRequest documentRequest) {
        return new ResponseData<>(201, "Thành công", documentService.uploadDocumentWithParent(folderId, documentRequest, file));
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

    @GetMapping("/open")
    public ResponseEntity<InputStreamResource> openDoc(@RequestParam(value = "documentId") Long documentId) {
        DocumentDataResponse documentDataResponse = documentService.openDocumentById(documentId);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" +
                                                                           documentDataResponse.getName() + "\"")
                .contentType(MediaType.parseMediaType(documentDataResponse.getType())).body(new InputStreamResource(new
                        ByteArrayInputStream(documentDataResponse.getData())));
    }

}
