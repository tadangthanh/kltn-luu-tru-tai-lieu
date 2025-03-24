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

    DocumentResponse uploadDocumentWithFolder(Long folderId, DocumentRequest documentRequest, MultipartFile file);

    void softDeleteDocumentById(Long documentId); // xóa document nhưng chưa xóa vĩnh viễn

    void softDeleteDocumentsByFolderIds(List<Long> folderIds); // xóa document theo danh sach folder id nhưng chưa xóa vĩnh viễn

    void hardDeleteDocumentByFolderIds(List<Long> folderIds); // xoa vinh vien document theo danh sach folder id

    void hardDeleteDocumentById(Long documentId);

    DocumentResponse restoreDocumentById(Long documentId);

    DocumentResponse moveDocumentToFolder(Long documentId, Long folderId);

    DocumentResponse copyDocumentById(Long documentId); //tao ban sao document

    Document getDocumentByIdOrThrow(Long documentId);

    PageResponse<List<DocumentResponse>> searchByCurrentUser(Pageable pageable, String[] documents);

    DocumentResponse getDocumentById(Long documentId);

    DocumentResponse updateDocumentById(Long documentId, DocumentRequest documentRequest); //cap nhat document

    void restoreDocumentsByFolderIds(List<Long> folderIds);
}
