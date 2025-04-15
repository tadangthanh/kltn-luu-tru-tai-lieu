package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.FileBuffer;
import vn.kltn.exception.CustomIOException;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IUploadProcessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j(topic = "UPLOAD_PROCESSOR_SERVICE")
@RequiredArgsConstructor
public class UploadProcessorImpl implements IUploadProcessor {
    private final IAzureStorageService azureStorageService;

    @Override
    public List<String> process(List<FileBuffer> bufferedFiles) {
        return uploadBufferedFilesToCloud(bufferedFiles);
    }

    private List<String> uploadBufferedFilesToCloud(List<FileBuffer> files) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (FileBuffer file : files) {
            try (InputStream inputStream = new ByteArrayInputStream(file.getData())) {
                CompletableFuture<String> future = azureStorageService.uploadChunkedWithContainerDefaultAsync(inputStream,
                        file.getFileName(), file.getSize(), 10 * 1024 * 1024)
                        .handle((blobName, ex) -> {
                            if (ex != null) {
                                log.error("Upload failed for file {}: {}", file.getFileName(), ex.getMessage());
                                return null; // hoặc return "" nếu bạn muốn tránh null
                            }
                            log.info("Upload thành công file: {}", file.getFileName());
                            return blobName;
                        });;
                futures.add(future);
            } catch (IOException e) {
                log.error("IOException khi đọc file {}: {}", file.getFileName(), e.getMessage());
                throw new CustomIOException("Có lỗi xảy ra khi đọc file");
            }
        }
        // Gộp tất cả futures lại
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // Collect kết quả
        CompletableFuture<List<String>> resultsFuture = allDoneFuture.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull) // bỏ những cái bị null do lỗi upload
                        .toList()
        );
        return resultsFuture.join(); // chỉ block 1 lần ở đây khi đã xong hết
    }
}
