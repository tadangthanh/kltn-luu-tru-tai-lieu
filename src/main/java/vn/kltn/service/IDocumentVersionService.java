package vn.kltn.service;

import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.response.DocumentVersionResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.DocumentVersion;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface IDocumentVersionService {
    DocumentVersion increaseVersion(Document document);

    List<DocumentVersion> increaseVersions(List<Document> documents);

    List<DocumentVersion> increaseVersions(List<Document> documents, Map<Long, FileBuffer> bufferMap);

    DocumentVersion createNewVersion(Document document, String blobName, long size);

    List<DocumentVersionResponse> getVersionsByDocumentId(Long documentId);

    void deleteVersionsByDocumentId(Long documentId);

    void deleteAllByDocuments(List<Long> documentIds);

    DocumentVersionResponse restoreVersion(Long documentId, Long targetVersionId);

    InputStream downloadVersion(Long versionId);

    DocumentVersion getVersionByIdOrThrow(Long versionId);


}
