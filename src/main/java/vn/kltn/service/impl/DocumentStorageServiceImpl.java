package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.DocumentStorageService;

import java.io.InputStream;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_STORAGE_SERVICE")
public class DocumentStorageServiceImpl implements DocumentStorageService {
    private final IAzureStorageService azureStorageService;

    @Override
    public void deleteBlobsFromCloud(List<String> blobNames) {
        log.info("delete blobs from cloud: {}", blobNames);
        if (!blobNames.isEmpty()) {
            azureStorageService.deleteBLobs(blobNames);
        }
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
}
