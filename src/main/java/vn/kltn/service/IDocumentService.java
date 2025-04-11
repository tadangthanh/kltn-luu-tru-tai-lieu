package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.DocumentDataResponse;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.entity.Document;
import vn.kltn.index.DocumentSegmentEntity;

import java.util.List;

public interface IDocumentService extends IResourceService<Document, DocumentResponse> {
    DocumentResponse uploadDocumentWithoutParent(DocumentRequest documentRequest, MultipartFile file);

    DocumentResponse uploadDocumentWithParent(Long folderId, DocumentRequest documentRequest, MultipartFile file);

    void softDeleteDocumentsByFolderIds(List<Long> folderIds); // xóa document theo danh sach folder id nhưng chưa xóa vĩnh viễn

    void hardDeleteDocumentByFolderIds(List<Long> folderIds); // xoa vinh vien document theo danh sach folder id

    DocumentResponse copyDocumentById(Long documentId); //tao ban sao document

    DocumentResponse updateDocumentById(Long documentId, DocumentRequest documentRequest); //cap nhat document

    void restoreDocumentsByFolderIds(List<Long> folderIds);

    List<DocumentSegmentEntity> searchMetadata(String query, Pageable pageable);

    DocumentDataResponse openDocumentById(Long documentId);
}
