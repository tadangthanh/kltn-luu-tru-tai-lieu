package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.ProcessDocUploadResult;
import vn.kltn.dto.UploadContext;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.UploadProgressDTO;
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
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j(topic = "UPLOAD_PROCESSOR_SERVICE")
@RequiredArgsConstructor
public class UploadProcessorImpl implements IUploadProcessor {
    private final IAzureStorageService azureStorageService;
    private final UploadFinalizerService uploadFinalizerService;
    private final WebSocketService webSocketService;
    private final ItemMapperService itemMapperService;
    private final UploadProgressThrottler uploadProgressThrottler;


    @Override
    public List<String> processUpload(UploadContext context, List<FileBuffer> files) {
        try {
            // Upload files
            if (checkCancellation(context)) {
                return List.of();
            }
            List<String> blobNames = uploadBufferedFilesToCloud(files);
            context.setBlobNames(blobNames);

            // Map blobs to documents
            if (checkCancellation(context)) {
                return blobNames;
            }
            uploadFinalizerService.finalizeUpload(context.getToken());

            List<ItemResponse> itemResponses = itemMapperService.toResponse(context.getItems());
            webSocketService.sendDocUploadSuccess(SecurityContextHolder.getContext().getAuthentication().getName(), new ProcessDocUploadResult(false, itemResponses));

            return blobNames;
        } finally {
            uploadFinalizerService.finalizeUpload(context.getToken());
        }
    }


    private boolean checkCancellation(UploadContext context) {
        return uploadFinalizerService.checkCancelledAndFinalize(context.getToken(), context.getItems(), context.getDocumentIndices(), context.getBlobNames());
    }



    private List<String> uploadBufferedFilesToCloud(List<FileBuffer> files) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("email = " + email);
        int totalFile = files.size();
        AtomicInteger totalFileUploaded = new AtomicInteger();
        for (FileBuffer file : files) {
            try (InputStream inputStream = new ByteArrayInputStream(file.getData())) {
                uploadProgressThrottler.sendProgress(email, new UploadProgressDTO(file.getFileName(), totalFileUploaded.get(), totalFile));
                CompletableFuture<String> future = azureStorageService.uploadChunk(inputStream, file.getFileName(), file.getSize(), 1024 * 1024).handle((blobName, ex) -> {
                    // clear progress
                    if (ex != null) {
                        uploadProgressThrottler.clear(email, file.getFileName());
                        log.error("Upload failed for file {}: {}", file.getFileName(), ex.getMessage());
                        return null; // hoặc return "" nếu bạn muốn tránh null
                    }
                    log.info("Upload thành công file: {}", file.getFileName());
                    totalFileUploaded.getAndIncrement();
                    uploadProgressThrottler.sendProgress(email, new UploadProgressDTO(file.getFileName(), totalFileUploaded.get(), totalFile));
                    uploadProgressThrottler.clear(email, file.getFileName());
                    return blobName;
                });
                futures.add(future);
            } catch (IOException e) {
                uploadProgressThrottler.clear(email, file.getFileName());
                log.error("IOException khi đọc file {}: {}", file.getFileName(), e.getMessage());
                throw new CustomIOException("Có lỗi xảy ra khi đọc file");
            }
        }
        // Gộp tất cả futures lại
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // Collect kết quả
        CompletableFuture<List<String>> resultsFuture = allDoneFuture.thenApply(v -> futures.stream().map(CompletableFuture::join).filter(Objects::nonNull) // bỏ những cái bị null do lỗi upload
                .toList());
        return resultsFuture.join(); // chỉ block 1 lần ở đây khi đã xong hết
    }
}
