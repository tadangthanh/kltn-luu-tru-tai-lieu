package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.common.CancellationToken;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.*;
import vn.kltn.entity.Document;

import java.io.InputStream;
import java.util.List;

public interface IDocumentService extends IItemCommonService<Document, DocumentResponse> {
    void uploadDocumentEmptyParent(List<FileBuffer> bufferedFiles, CancellationToken token);

    void softDeleteDocumentById(Long documentId); // xoa tam document

    InputStream download(Long documentId);

    void uploadDocumentWithParent(Long folderId, List<FileBuffer> bufferedFiles, CancellationToken token);

    void softDeleteDocumentsByFolderIds(List<Long> folderIds); // xóa document theo danh sach folder id nhưng chưa xóa vĩnh viễn

    void hardDeleteDocumentByFolderIds(List<Long> folderIds); // xoa vinh vien document theo danh sach folder id

    ItemResponse copyDocumentById(Long documentId); //tao ban sao document

    DocumentResponse updateDocumentById(Long documentId, DocumentRequest documentRequest); //cap nhat document

    void restoreDocumentsByFolderIds(List<Long> folderIds);

    List<DocumentIndexResponse> searchMetadata(String query, Pageable pageable);

    DocumentDataResponse openDocumentById(Long documentId);

    OnlyOfficeConfig getOnlyOfficeConfig(Long documentId); // lay thong tin config onlyoffice
}
