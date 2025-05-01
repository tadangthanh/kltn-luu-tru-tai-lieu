package vn.kltn.service;

import vn.kltn.entity.Document;
import vn.kltn.entity.DocumentVersion;

import java.util.List;

public interface IDocumentVersionService {
    DocumentVersion increaseVersion(Document document);

    void increaseVersions(List<Document> documents);

    DocumentVersion createNewVersion(Document document, String blobName, long size);

    List<DocumentVersion> getVersionsByDocumentId(Long documentId);
}
