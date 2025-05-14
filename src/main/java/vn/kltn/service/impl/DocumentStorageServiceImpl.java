package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.kltn.common.CancellationToken;
import vn.kltn.common.ItemType;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.UploadContext;
import vn.kltn.entity.*;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.service.*;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

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
    private final IItemIndexService itemIndexService;
    private final IDocumentHasTagService documentHasTagService;
    @Value("${app.delete.document-retention-days}")
    private int documentRetentionDays;
    private final IDocumentVersionService documentVersionService;


    @Override
    public void deleteBlobsFromCloud(List<String> blobNames) {
        log.info("delete blobs from cloud: {}", blobNames);
        if (!blobNames.isEmpty()) {
            azureStorageService.deleteBLobs(blobNames);
        }
    }

    @Override
    public void markDeleteDocumentsByFolders(List<Long> folderIds) {
        log.info("mark delete documents by folders: {}", folderIds);
        documentRepo.setDeleteDocument(folderIds, LocalDateTime.now(), LocalDateTime.now().plusDays(documentRetentionDays));
        List<Long> documentIdsToMarkDelete = documentRepo.findDocumentIdsWithParentIds(folderIds);
        // danh dau isDeleted o elasticsearch
        itemIndexService.markDeleteItems(documentIdsToMarkDelete, true);
    }

    @Override
    public void restoreDocumentsByFolders(List<Long> folderIds) {
        log.info("restore documents by folders");
        documentRepo.setDeleteDocument(folderIds, null, null);
        List<Long> documentIdsToMarkDelete = documentRepo.findDocumentIdsWithParentIds(folderIds);
        // danh dau isDeleted o elasticsearch
        itemIndexService.markDeleteItems(documentIdsToMarkDelete, false);
    }


    @Override
    public Document copyDocument(Document document) {
        Document copied = documentMapperService.copyDocument(document);

        String newName = generateCopyName(document.getName());
        copied.setName(newName);

        copied = documentRepo.save(copied);

        copied.setDeletedAt(null);
        copied.setPermanentDeleteAt(null);

        //  Copy related data
        copyRelatedData(document, copied);

        return documentRepo.save(copied);
    }

    private void copyRelatedData(Document source, Document target) {
        // Copy tags
        copyDocumentTags(source, target);

        // Copy blob
        String newBlobName = copyBlob(source.getCurrentVersion().getBlobName());
        target.getCurrentVersion().setBlobName(newBlobName);

        // Index document
        itemIndexService.insertItem(target);
    }

    private String generateCopyName(String originalName) {
        int lastDotIndex = originalName.lastIndexOf(".");
        return originalName.substring(0, lastDotIndex - 1) +
               "_copy" +
               originalName.substring(lastDotIndex);
    }

    private void copyDocumentTags(Document source, Document target) {
        Set<Tag> tags = documentHasTagService.getTagsByDocumentId(source.getId());
        documentHasTagService.addDocumentToTag(target, tags);
    }

    @Override
    public void deleteDocumentsByFolders(List<Long> folderIds) {
        log.info("delete documents by folders");
        // Lấy danh sách documents theo folderIds
        List<Document> documentsToDelete = documentRepo.findDocumentsByParentIds(folderIds);
        if (documentsToDelete.isEmpty()) {
            return; // Không có document nào để xóa, thoát sớm
        }
        deleteDocuments(documentsToDelete);
    }

    private void markDeleteDocumentIndexByFolders(List<Long> folderIds, boolean value) {
        List<Long> documentsMarkDeleted = documentRepo.findDocumentIdsWithParentIds(folderIds);
        // danh dau isDeleted o elasticsearch
        itemIndexService.markDeleteItems(documentsMarkDeleted, value);
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
            itemIndexService.deleteIndexByIdList(documentIds);
            // delete versions
            documentVersionService.deleteAllByDocuments(documentIds);
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
        return documents.stream().map(
                document -> {
                    if (document.getCurrentVersion() != null) {
                        return document.getCurrentVersion().getBlobName();
                    }
                    return null;
                }
        ).filter(Objects::nonNull).toList();
    }

    @Override
    public List<String> store(CancellationToken token, List<FileBuffer> bufferedFiles, List<Document> documents) {
        List<Item> items = new ArrayList<>(documents);
        UploadContext context = new UploadContext(token, items);
        return uploadProcessor.processUpload(context, bufferedFiles);
    }
    @Override
    public List<Document> saveDocumentsWithFolder(List<FileBuffer> fileBuffers, Long folderId) {
        // Lưu tài liệu vào cơ sở dữ liệu
        List<Document> documents = documentMapperService.mapFilesBufferToListDocument(fileBuffers);
        documents = documentRepo.saveAll(documents);
        Folder folder = folderCommonService.getFolderByIdOrThrow(folderId);
        // Map từ documentId sang FileBuffer để truyền size
        Map<Long, FileBuffer> bufferMap = new HashMap<>();
        for (int i = 0; i < documents.size(); i++) {
            bufferMap.put(documents.get(i).getId(), fileBuffers.get(i));
        }

        // Tạo versions
        List<DocumentVersion> documentVersions = documentVersionService.increaseVersions(documents, bufferMap);

        for (Document document : documents) {
            document.setParent(folder);
            document.setItemType(ItemType.DOCUMENT);
            document.setCurrentVersion(documentVersions.get(documents.indexOf(document)));
        }
        documents = documentRepo.saveAll(documents);
        return documents;
    }

    @Override
    public void deleteBlob(String blobName) {
        log.info("delete blob from cloud: {}", blobName);
        azureStorageService.deleteBlob(blobName);
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

        // Map từ documentId sang FileBuffer để truyền size
        Map<Long, FileBuffer> bufferMap = new HashMap<>();
        for (int i = 0; i < documents.size(); i++) {
            bufferMap.put(documents.get(i).getId(), bufferedFiles.get(i));
        }

        // Tạo versions
        List<DocumentVersion> documentVersions = documentVersionService.increaseVersions(documents, bufferMap);

        // Set lại currentVersion
        for (int i = 0; i < documents.size(); i++) {
            documents.get(i).setCurrentVersion(documentVersions.get(i));
            documents.get(i).setItemType(ItemType.DOCUMENT);
        }

        return documents;
    }

    @Override
    public InputStream download(String blobName) {
        log.info("download blob from cloud: {}", blobName);
        return azureStorageService.downloadBlobInputStream(blobName);
    }

}
