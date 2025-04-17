package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.common.CancellationToken;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.ProcessUploadResult;
import vn.kltn.dto.UploadContext;
import vn.kltn.entity.Document;
import vn.kltn.entity.Folder;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.service.*;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_STORAGE_SERVICE")
public class DocumentStorageServiceImpl implements IDocumentStorageService {
    private final IAzureStorageService azureStorageService;
    private final IDocumentMapperService documentMapperService;
    private final DocumentRepo documentRepo;
    private final FolderCommonService folderCommonService;
    private final IUploadProcessor uploadProcessor;
    private final IDocumentIndexService documentIndexService;
    private final IDocumentHasTagService documentHasTagService;

    @Override
    public void deleteBlobsFromCloud(List<String> blobNames) {
        log.info("delete blobs from cloud: {}", blobNames);
        if (!blobNames.isEmpty()) {
            azureStorageService.deleteBLobs(blobNames);
        }
    }

    @Override
    public void deleteDocuments(List<Document> documents) {
        log.info("delete documents");
        //  xoa tren cloud
        deleteBlobsFromStorage(documents);
        //  xoa cac du lieu lien quan
        deleteAssociatedData(documents);
        // xoa trong database
        deleteFromDatabase(documents);
    }

    private void deleteFromDatabase(List<Document> documents) {
        if (!documents.isEmpty()) {
            documentRepo.deleteAll(documents);
        }
    }

    private void deleteAssociatedData(List<Document> documents) {
        List<Long> documentIds = extractDocumentIds(documents);
        if (!documentIds.isEmpty()) {
            // Delete tags
            documentHasTagService.deleteAllByDocumentIds(documentIds);
            // Delete from search index
            documentIndexService.deleteIndexByIdList(documentIds);
        }
    }

    private List<Long> extractDocumentIds(List<Document> documents) {
        return documents.stream().map(Document::getId).toList();
    }

    private void deleteBlobsFromStorage(List<Document> documents) {
        List<String> blobNames = extractBlobNames(documents);
        if (!blobNames.isEmpty()) {
            deleteBlobsFromCloud(blobNames);
        }
    }

    private List<String> extractBlobNames(List<Document> documents) {
        return documents.stream().map(Document::getBlobName).filter(Objects::nonNull).toList();
    }

    @Override
    public void store(CancellationToken token, List<FileBuffer> bufferedFiles, List<Document> documents) {
        UploadContext context = new UploadContext(token, documents);
        ProcessUploadResult result = uploadProcessor.processUpload(context, bufferedFiles);

        if (result.isCancelled()) {
            log.info("Upload was cancelled");
        }
    }

    @Override
    public List<Document> saveDocumentsWithFolder(List<FileBuffer> fileBuffers, Long folderId) {
        // Lưu tài liệu vào cơ sở dữ liệu
        List<Document> documents = documentMapperService.mapFilesBufferToListDocument(fileBuffers);
        Folder folder = folderCommonService.getFolderByIdOrThrow(folderId);
        documents.forEach(document -> document.setParent(folder));
        documents = documentRepo.saveAll(documents);
        return documents;
    }

    @Override
    public void deleteBlob(String blobName) {
        log.info("delete blob from cloud: {}", blobName);
        azureStorageService.deleteBlob(blobName);
    }

    @Override
    public InputStream downloadBlobInputStream(String blobName) {
        return azureStorageService.downloadBlobInputStream(blobName);
    }

    @Override
    public String copyBlob(String blobName) {
        log.info("copy blob from cloud: {}", blobName);
        return azureStorageService.copyBlob(blobName);
    }

    @Override
    public List<Document> saveDocuments(List<FileBuffer> bufferedFiles) {
        List<Document> documents = documentMapperService.mapFilesBufferToListDocument(bufferedFiles);
        documents = documentRepo.saveAll(documents);
        return documents;
    }

}
