package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IDocumentService;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/documents")
@RestController
public class DocumentRest {
    private final IDocumentService documentService;

    @PostMapping
    public ResponseData<DocumentResponse> upload(@RequestPart("file") MultipartFile file, @Valid @RequestPart("data") DocumentRequest documentRequest) {
        return new ResponseData<>(201, "Thành công", documentService.uploadDocumentWithoutFolder(documentRequest, file));
    }

    @PostMapping("/folder/{folderId}")
    public ResponseData<DocumentResponse> upload(@PathVariable Long folderId, @RequestPart("file") MultipartFile file,
                                                 @Valid @RequestPart("data") DocumentRequest documentRequest) {
        return new ResponseData<>(201, "Thành công", documentService.uploadDocumentWithFolder(folderId, documentRequest, file));
    }

    @DeleteMapping("/{documentId}")
    public ResponseData<Void> delete(@PathVariable Long documentId) {
        documentService.softDeleteDocumentById(documentId);
        return new ResponseData<>(204, "Xóa thành công", null);
    }

    @PostMapping("/{documentId}/copy")
    public ResponseData<DocumentResponse> copy(@PathVariable Long documentId) {
        return new ResponseData<>(201, "Thành công", documentService.copyDocumentById(documentId));
    }

    @PutMapping("/{documentId}")
    public ResponseData<DocumentResponse> update(@PathVariable Long documentId, @Valid @RequestBody DocumentRequest documentRequest) {
        return new ResponseData<>(200, "Thành công", documentService.updateDocumentById(documentId, documentRequest));
    }

    @GetMapping
    public ResponseData<PageResponse<List<DocumentResponse>>> search(Pageable pageable, @RequestParam(required = false, value = "documents") String[] documents) {
        return new ResponseData<>(200, "Thành công", documentService.searchByCurrentUser(pageable, documents));
    }
}
