package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.common.CancellationToken;
import vn.kltn.dto.FileBuffer;
import vn.kltn.exception.CustomIOException;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IUploadProcessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j(topic = "UPLOAD_PROCESSOR_SERVICE")
@RequiredArgsConstructor
public class UploadProcessorImpl implements IUploadProcessor {
    private final IAzureStorageService azureStorageService;
    private final UploadFinalizerService uploadFinalizerService;

    @Override
    public List<String> process(CancellationToken token, List<FileBuffer> bufferedFiles) {
        return uploadBufferedFilesToCloud(bufferedFiles, token);
    }

    private List<String> uploadBufferedFilesToCloud(List<FileBuffer> files, CancellationToken token) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (FileBuffer file : files) {
            try (InputStream inputStream = new ByteArrayInputStream(file.getData())) {
                CompletableFuture<String> future = azureStorageService.uploadChunkedWithContainerDefaultAsync(inputStream,
                        file.getFileName(), file.getSize(), 10 * 1024 * 1024,token);
                futures.add(future);
            } catch (IOException e) {
                throw new CustomIOException("Có lỗi xảy ra khi đọc file");
            }
        }
        // Gộp tất cả future lại
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // Đợi tất cả hoàn tất rồi collect kết quả
        CompletableFuture<List<String>> resultsFuture = allDoneFuture
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList())
                .whenComplete((blobNames, ex) -> {
                    if (token.isCancelled()) {
                        log.info("Upload was cancelled");
                        uploadFinalizerService.finalizeUpload(token, null, null, blobNames);
                    }

                    if (ex != null) {
                        log.error("Error occurred during upload: {}", ex.getMessage());
                        uploadFinalizerService.finalizeUpload(token, null, null, blobNames);
                        log.info("Token đã được remove khỏi registry: {}", token.getUploadId());
                    }
                });

        return resultsFuture.join(); // chỉ block 1 lần ở đây khi đã xong hết
    }
}
