package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Document;
import vn.kltn.entity.PreviewImage;
import vn.kltn.exception.InternalServerErrorException;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentConversionService;
import vn.kltn.service.IDocumentService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j(topic = "ASYNC_PREVIEW_IMAGE_SERVICE")
@RequiredArgsConstructor
public class AsyncPreviewImageService {
    private final IDocumentService documentService;
    private final IDocumentConversionService documentConversionService;
    private final IAzureStorageService azureStorageService;


    @Async
    public CompletableFuture<List<PreviewImage>> generatePreviewImagesAsync(Long documentId, List<Integer> pages) {
        log.info("Thread: {}", Thread.currentThread().getName());
        List<String> imageBlobNames = new ArrayList<>();
        try {
            Document document = documentService.getItemByIdOrThrow(documentId);
            imageBlobNames.addAll(documentConversionService.convertPdfToImagesAndUpload(document, pages));
            if (imageBlobNames.size() != pages.size()) {
                log.error("Số lượng ảnh tạo ra không khớp với số trang yêu cầu");
                throw new InternalServerErrorException("Có lỗi xảy ra khi tạo preview tài liệu");
            }
            List<PreviewImage> previewImages = new ArrayList<>();
            for (int i = 0; i < imageBlobNames.size(); i++) {
                int page = pages.get(i);
                PreviewImage previewImage = new PreviewImage();
                previewImage.setDocument(document);
                previewImage.setPageNumber(page);
                previewImage.setImageBlobName(imageBlobNames.get(i));
                previewImages.add(previewImage);
            }
            return CompletableFuture.completedFuture(previewImages);
        } catch (Exception e) {
            azureStorageService.deleteBLobs(imageBlobNames);
            log.error("Lỗi khi tạo preview async", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
