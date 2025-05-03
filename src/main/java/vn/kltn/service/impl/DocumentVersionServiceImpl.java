package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.LatestVersion;
import vn.kltn.dto.response.DocumentVersionResponse;
import vn.kltn.dto.response.OnlyOfficeConfig;
import vn.kltn.entity.Document;
import vn.kltn.entity.DocumentVersion;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.DocumentVersionMapper;
import vn.kltn.repository.DocumentRepo;
import vn.kltn.repository.DocumentVersionRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentVersionService;
import vn.kltn.service.IRedisTokenService;
import vn.kltn.service.event.publisher.DocumentEventPublisher;
import vn.kltn.util.DocumentTypeUtil;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static vn.kltn.repository.util.FileUtil.getFileExtension;

@Service
@Transactional
@Slf4j(topic = "DOCUMENT_VERSION_SERVICE")
@RequiredArgsConstructor
public class DocumentVersionServiceImpl implements IDocumentVersionService {
    private final DocumentVersionRepo documentVersionRepo;
    private final DocumentVersionMapper documentVersionMapper;
    private final IAzureStorageService azureStorageService;
    private final DocumentRepo documentRepo;
    private final DocumentEventPublisher documentEventPublisher;
    private final IAuthenticationService authenticationService;


    @Override
    public DocumentVersion increaseVersion(Document document) {
        DocumentVersion newVersion;
        if (document.getCurrentVersion() == null) {
            log.info("Create new version for document: documentId={}", document.getId());
            newVersion = documentVersionMapper.toDocumentVersion(document);
            newVersion.setVersion(1); // version đầu tiên
        } else {
            log.info("Create next version for document: documentId={}", document);
            DocumentVersion latestVersion = documentVersionRepo.findLatestVersion(document.getId());
            newVersion = new DocumentVersion();
            newVersion.setVersion(latestVersion.getVersion() + 1);
            newVersion.setBlobName(document.getCurrentVersion().getBlobName());
        }
        // Bước 3: Xoá các version cũ ngoài top 10
        List<DocumentVersion> oldVersions = documentVersionRepo
                .findOldVersions(document.getId(), 10);
        if (!oldVersions.isEmpty()) {
            documentVersionRepo.deleteAll(oldVersions);
        }
        newVersion.setDocument(document);
        document.setCurrentVersion(newVersion);
        newVersion.setExpiredAt(LocalDateTime.now().plusDays(7));
        return documentVersionRepo.save(newVersion);
    }


    @Override
    public List<DocumentVersion> increaseVersions(List<Document> documents) {
        if (documents.isEmpty()) return List.of();

        // 1) Lấy map documentId → latest version
        List<Long> ids = documents.stream().map(Document::getId).toList();
        Map<Long, Integer> latestMap = documentVersionRepo
                .findLatestVersionNumbers(ids)
                .stream()
                .collect(Collectors.toMap(LatestVersion::getDocumentId, LatestVersion::getVersion));

        // 2) Tạo batch các version mới
        List<DocumentVersion> toSave = new ArrayList<>(documents.size());
        LocalDateTime now = LocalDateTime.now();
        for (Document doc : documents) {
            int newVer = latestMap.getOrDefault(doc.getId(), 0) + 1;
            DocumentVersion dv = documentVersionMapper.toDocumentVersion(doc);
            dv.setDocument(doc);
            dv.setVersion(newVer);
            dv.setExpiredAt(now.plusDays(7));
            doc.setCurrentVersion(dv);
            toSave.add(dv);
        }
        List<DocumentVersion> result = documentVersionRepo.saveAll(toSave);

        // 3) Xóa các version cũ để chỉ giữ 10 bản mỗi document
        //    (dùng window-function native query)
        documentVersionRepo.deleteOldVersionsBeyondLimit(10);
        return result;
    }

    @Override
    public List<DocumentVersion> increaseVersions(List<Document> documents, Map<Long, FileBuffer> bufferMap) {
        if (documents.isEmpty()) return List.of();

        // 1) Lấy map documentId → latest version
        List<Long> ids = documents.stream().map(Document::getId).toList();
        Map<Long, Integer> latestMap = documentVersionRepo
                .findLatestVersionNumbers(ids)
                .stream()
                .collect(Collectors.toMap(LatestVersion::getDocumentId, LatestVersion::getVersion));

        // 2) Tạo batch các version mới
        List<DocumentVersion> toSave = new ArrayList<>(documents.size());
        LocalDateTime now = LocalDateTime.now();
        for (Document doc : documents) {
            int newVer = latestMap.getOrDefault(doc.getId(), 0) + 1;
            DocumentVersion dv = documentVersionMapper.toDocumentVersion(doc);
            dv.setDocument(doc);
            dv.setVersion(newVer);
            dv.setSize(bufferMap.get(doc.getId()).getSize());
            dv.setExpiredAt(now.plusDays(7));
            doc.setCurrentVersion(dv);
            toSave.add(dv);
        }
        List<DocumentVersion> result = documentVersionRepo.saveAll(toSave);

        // 3) Xóa các version cũ để chỉ giữ 10 bản mỗi document
        //    (dùng window-function native query)
        documentVersionRepo.deleteOldVersionsBeyondLimit(10);
        return result;
    }

    @Override
    public DocumentVersion createNewVersion(Document document, String blobName, long size) {
        int latestVersion = documentVersionRepo
                .findLatestVersionNumber(document.getId())
                .orElse(0);

        DocumentVersion newVersion = documentVersionMapper.toDocumentVersion(document);
        newVersion.setVersion(latestVersion + 1);
        newVersion.setExpiredAt(LocalDateTime.now().plusDays(7));
        newVersion.setBlobName(blobName);
        newVersion.setSize(size);
        newVersion.setDocument(document);

        return documentVersionRepo.save(newVersion);
    }


    @Override
    public List<DocumentVersionResponse> getVersionsByDocumentId(Long documentId) {
        log.info("get versions for documentId: {}", documentId);
        return documentVersionMapper.toDocumentVersionResponse(documentVersionRepo.findAllByDocumentId(documentId));
    }

    @Override
    public void deleteVersionsByDocumentId(Long documentId) {
        List<DocumentVersion> versions = documentVersionRepo.findAllByDocumentId(documentId);
        if (versions.isEmpty()) {
            log.info("No versions found for documentId: {}", documentId);
            return;
        }
        log.info("Deleting versions for documentId: {}", documentId);
        List<String> blobNames = versions.stream()
                .map(DocumentVersion::getBlobName)
                .collect(Collectors.toList());
        documentVersionRepo.deleteAll(versions);
        azureStorageService.deleteBLobs(blobNames);
        log.info("Deleted {} versions for documentId: {}", versions.size(), documentId);
    }

    @Override
    public void deleteAllByDocuments(List<Long> documentIds) {
        List<DocumentVersion> versions = documentVersionRepo.findAllByDocumentIds(documentIds);
        if (versions.isEmpty()) {
            log.info("No versions found for documentIds: {}", documentIds);
            return;
        }
        log.info("Deleting versions for documentIds: {}", documentIds);
        List<String> blobNames = versions.stream()
                .map(DocumentVersion::getBlobName)
                .collect(Collectors.toList());
        documentVersionRepo.deleteAll(versions);
        azureStorageService.deleteBLobs(blobNames);
        log.info("Deleted {} versions for documentIds: {}", versions.size(), documentIds);

    }

    @Override
    public DocumentVersionResponse restoreVersion(Long documentId, Long targetVersionId) {
        DocumentVersion targetVersion = documentVersionRepo
                .findById(targetVersionId)
                .orElseThrow(() -> {
                    log.warn("DocumentVersion with id {} not found", targetVersionId);
                    return new ResourceNotFoundException("Version not found");
                });

        Document document = targetVersion.getDocument();

        User currentUser = authenticationService.getCurrentUser();
        if (!currentUser.getId().equals(document.getOwner().getId())) {
            throw new AccessDeniedException("Chức năng này chỉ dành cho người sở hữu tài liệu");
        }

        if (!document.getId().equals(documentId)) {
            throw new InvalidDataException("Document ID does not match with the version's document ID");
        }

        // Backup current version before restore
        DocumentVersion currentVersion = document.getCurrentVersion();
        if (currentVersion != null) {
            DocumentVersion backupVersion = new DocumentVersion();
            backupVersion.setDocument(document);
            backupVersion.setVersion(documentVersionRepo.findLatestVersionNumber(documentId).orElse(0) + 1);
            backupVersion.setBlobName(currentVersion.getBlobName());
            backupVersion.setSize(currentVersion.getSize());
            backupVersion.setExpiredAt(LocalDateTime.now().plusDays(7));
            documentVersionRepo.save(backupVersion);
        }

        // Create new version from targetVersion (don't reuse old one)
        DocumentVersion restored = new DocumentVersion();
        restored.setDocument(document);
        restored.setBlobName(targetVersion.getBlobName());
        restored.setSize(targetVersion.getSize());
        restored.setExpiredAt(LocalDateTime.now().plusDays(7));
        restored.setVersion(documentVersionRepo.findLatestVersionNumber(documentId).orElse(0) + 1);
        documentVersionRepo.save(restored);

        // Update document to point to restored version
        document.setCurrentVersion(restored);
        documentRepo.save(document);

        log.info("Restored documentId={} to versionId={} (new version created)", documentId, targetVersionId);
        // cái này sai, vì nó update document sau khi đã chuyển sang version cũ tức là
        documentEventPublisher.publishDocumentUpdateContent(documentId);
        return documentVersionMapper.toDocumentVersionResponse(restored);
    }

    @Override
    public InputStream downloadVersion(Long versionId) {
        DocumentVersion documentVersion = documentVersionRepo.findById(versionId).orElseThrow(() -> {
            log.warn("DocumentVersion with id {} not found", versionId);
            return new ResourceNotFoundException("Version not found");
        });
        if (documentVersion.getBlobName() == null) {
            log.warn("DocumentVersion with id {} has no blob name", versionId);
            throw new ResourceNotFoundException("Version not found");
        }
        String blobName = documentVersion.getBlobName();
        log.info("Downloading versionId={} with blobName={}", versionId, blobName);
        return azureStorageService.downloadBlobInputStream(blobName);
    }

    @Override
    public DocumentVersion getVersionByIdOrThrow(Long versionId) {
        return documentVersionRepo.findById(versionId).orElseThrow(() -> {
            log.warn("DocumentVersion with id {} not found", versionId);
            return new ResourceNotFoundException("Version not found");
        });
    }

}
