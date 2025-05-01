package vn.kltn.service;

import vn.kltn.common.CancellationToken;
import vn.kltn.dto.FileBuffer;
import vn.kltn.entity.Document;

import java.io.InputStream;
import java.util.List;

public interface IDocumentStorageService {
    void deleteBlobsFromCloud(List<String> blobNames);

    void markDeleteDocumentsByFolders(List<Long> folderIds);

    void restoreDocumentsByFolders(List<Long> folderIds);

    Document copyDocument(Document document);

    void deleteDocumentsByFolders(List<Long> folderIds);

    void deleteDocuments(List<Document> documents);

    List<String> store(CancellationToken token, List<FileBuffer> bufferedFiles, List<Document> documents);

    void deleteBlob(String blobName);

    List<Document> saveDocumentsWithFolder(List<FileBuffer> fileBuffers, Long folderId);

    String copyBlob(String blobName);

    List<Document> saveDocuments(List<FileBuffer> bufferedFiles);

    InputStream download(String blobName);
}
