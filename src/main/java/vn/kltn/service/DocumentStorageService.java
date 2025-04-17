package vn.kltn.service;

import java.io.InputStream;
import java.util.List;

public interface DocumentStorageService {
    void deleteBlobsFromCloud(List<String> blobNames);

    void deleteBlob(String blobName);

    InputStream downloadBlobInputStream(String blobName);

    String copyBlob(String blobName);
}
