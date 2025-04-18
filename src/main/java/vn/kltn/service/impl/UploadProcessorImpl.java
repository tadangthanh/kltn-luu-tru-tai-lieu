package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.ProcessUploadResult;
import vn.kltn.dto.UploadContext;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.exception.CustomIOException;
import vn.kltn.index.DocumentIndex;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentIndexService;
import vn.kltn.service.IDocumentMapperService;
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
    private final IDocumentMapperService documentMapperService;
    private final IDocumentIndexService documentIndexService;
    private final UploadFinalizerService uploadFinalizerService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public List<String> process(List<FileBuffer> bufferedFiles) {
        return uploadBufferedFilesToCloud(bufferedFiles);
    }

    @Override
    public ProcessUploadResult processUpload(UploadContext context, List<FileBuffer> files) {
        try {
            // Upload files
            if (checkCancellation(context)) {
                return createCancelledResult();
            }
            List<String> blobNames = uploadBufferedFilesToCloud(files);
            context.setBlobNames(blobNames);

            // Map blobs to documents
            if (checkCancellation(context)) {
                return createCancelledResult();
            }
            uploadFinalizerService.finalizeUpload(context.getToken());
            documentMapperService.mapBlobNamesToDocuments(context.getDocuments(), blobNames);
            List<DocumentResponse> documentResponses = documentMapperService.mapToDocumentResponseList(context.getDocuments());
            messagingTemplate.convertAndSendToUser(
                    SecurityContextHolder.getContext().getAuthentication().getName(),
                    "/topic/upload-documents-completed",
                    new ProcessUploadResult(false, documentResponses));
            List<DocumentIndex> indices = documentIndexService.insertAllDoc(context.getDocuments()).join();
            context.setDocumentIndices(indices);

            return new ProcessUploadResult(false, documentResponses);
        } finally {
            uploadFinalizerService.finalizeUpload(context.getToken());

        }
    }

    private boolean checkCancellation(UploadContext context) {
        return uploadFinalizerService.checkCancelledAndFinalize(
                context.getToken(),
                context.getDocuments(),
                context.getDocumentIndices(),
                context.getBlobNames()
        );
    }

    private ProcessUploadResult createCancelledResult() {
        return new ProcessUploadResult(true, null);
    }


    private List<String> uploadBufferedFilesToCloud(List<FileBuffer> files) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (FileBuffer file : files) {
            try (InputStream inputStream = new ByteArrayInputStream(file.getData())) {
                CompletableFuture<String> future = azureStorageService.uploadChunkedWithContainerDefaultAsync(inputStream,
                                file.getFileName(), file.getSize(), 1024 * 1024)
                        .handle((blobName, ex) -> {
                            if (ex != null) {
                                log.error("Upload failed for file {}: {}", file.getFileName(), ex.getMessage());
                                return null; // hoặc return "" nếu bạn muốn tránh null
                            }
                            log.info("Upload thành công file: {}", file.getFileName());
                            return blobName;
                        });
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
