package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Document;

import java.util.List;

public interface IDocumentService {
    DocumentResponse uploadDocumentWithoutFolder(DocumentRequest documentRequest, MultipartFile file);

    void softDeleteDocumentById(Long documentId); // xóa document nhưng chưa xóa vĩnh viễn

    DocumentResponse copyDocumentById(Long documentId); //tao ban sao document

    Document getDocumentByIdOrThrow(Long documentId);

    PageResponse<List<DocumentResponse>> searchByCurrentUser(Pageable pageable, String[] documents);

    void softDeleteDocumentsByFolderId(Long folderId);

    DocumentResponse updateDocumentById(Long documentId, DocumentRequest documentRequest); //cap nhat document
}
