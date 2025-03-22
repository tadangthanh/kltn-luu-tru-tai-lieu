package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IDocumentService;

@RequiredArgsConstructor
@RequestMapping("/api/v1/documents")
@RestController
public class DocumentRest {
    private final IDocumentService documentService;

    @PostMapping
    public ResponseData<DocumentResponse> upload(@RequestPart("file") MultipartFile file, @Valid @RequestPart("data") DocumentRequest documentRequest) {
        return new ResponseData<>(201, "Upload file successfully", documentService.uploadDocumentWithoutFolder(documentRequest, file));
    }
}
