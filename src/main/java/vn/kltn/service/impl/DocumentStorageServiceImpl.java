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
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentMapperService;
import vn.kltn.service.IDocumentStorageService;
import vn.kltn.service.IUploadProcessor;

import java.io.InputStream;
import java.util.List;

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

    @Override
    public void deleteBlobsFromCloud(List<String> blobNames) {
        log.info("delete blobs from cloud: {}", blobNames);
        if (!blobNames.isEmpty()) {
            azureStorageService.deleteBLobs(blobNames);
        }
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
