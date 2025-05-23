package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.common.CancellationToken;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.request.DocumentRequest;
import vn.kltn.dto.response.*;
import vn.kltn.entity.Document;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface IDocumentService extends IItemCommonService<Document, DocumentResponse> {
    void uploadDocumentNullParentSync(List<FileBuffer> bufferedFiles, CancellationToken token);

    void uploadDocumentWithParentSync(Long folderId, List<FileBuffer> bufferedFiles, CancellationToken token);

    void uploadDocumentNullParentBlocking(List<FileBuffer> bufferedFiles, CancellationToken token);

    void uploadDocumentWithParentBlocking(Long folderId, List<FileBuffer> bufferedFiles, CancellationToken token);


    void updateDocumentEditor(Long documentId, byte[] data); // cap nhat noi dung document

    void softDeleteDocumentById(Long documentId); // xoa tam document

    InputStream download(Long documentId);

    void softDeleteDocumentsByFolderIds(List<Long> folderIds); // xóa document theo danh sach folder id nhưng chưa xóa vĩnh viễn

    void hardDeleteDocumentByFolderIds(List<Long> folderIds); // xoa vinh vien document theo danh sach folder id

    ItemResponse copyDocumentById(Long documentId); //tao ban sao document

    DocumentResponse updateDocumentById(Long documentId, DocumentRequest documentRequest); //cap nhat document

    void restoreDocumentsByFolderIds(List<Long> folderIds);

    List<ItemSearchResponse> searchMetadata(String query, Pageable pageable);

    DocumentDataResponse openDocumentById(Long documentId);

    OnlyOfficeConfig getOnlyOfficeConfig(Long documentId); // lay thong tin config onlyoffice

    void hardDeleteItemById(Long documentId);

    File downloadAsPdf(String blobName); // tải file pdf từ blob storage
}
