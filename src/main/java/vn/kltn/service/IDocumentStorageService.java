package vn.kltn.service;

import vn.kltn.common.CancellationToken;
import vn.kltn.dto.FileBuffer;
import vn.kltn.entity.Document;

import java.io.InputStream;
import java.util.List;

public interface IDocumentStorageService {
    void deleteBlobsFromCloud(List<String> blobNames);

    void store(CancellationToken token, List<FileBuffer> bufferedFiles, List<Document> documents);

    void deleteBlob(String blobName);

    InputStream downloadBlobInputStream(String blobName);

    List<Document> saveDocumentsWithFolder(List<FileBuffer> fileBuffers, Long folderId);
    String copyBlob(String blobName);
}
