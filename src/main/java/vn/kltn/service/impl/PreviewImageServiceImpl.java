package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.PreviewPageSelectionRequest;
import vn.kltn.entity.Document;
import vn.kltn.entity.PreviewImage;
import vn.kltn.map.PreviewImageMapper;
import vn.kltn.repository.PreviewImageRepo;
import vn.kltn.service.IDocumentConversionService;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IPreviewImageService;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "PREVIEW_IMAGE_SERVICE")
public class PreviewImageServiceImpl implements IPreviewImageService {
    private final PreviewImageMapper previewImageMapper;
    private final PreviewImageRepo previewImageRepo;
    private final IDocumentService documentService;
    private final IDocumentConversionService documentConversionService;

    @Override
    public String createPreviewImages(PreviewPageSelectionRequest request) {
        log.info("Yêu cầu tạo preview async cho docId: {}", request.getDocumentId());
        generatePreviewImagesAsync(request.getDocumentId(), request.getPageNumbers());
        return "Yêu cầu đang được xử lý, ảnh preview sẽ sớm được tạo!";
    }

    @Async
    public void generatePreviewImagesAsync(Long documentId, List<Integer> pages) {
        try {
            Document document = documentService.getResourceByIdOrThrow(documentId);
            List<String> imageBlobNames = documentConversionService.convertPdfToImagesAndUpload(document.getBlobName(), pages);

            List<PreviewImage> previewImages = new ArrayList<>();
            for (int i = 0; i < imageBlobNames.size(); i++) {
                PreviewImage previewImage = new PreviewImage();
                previewImage.setDocument(document);
                previewImage.setPageNumber(pages.get(i));
                previewImage.setImageBlobName(imageBlobNames.get(i));
                previewImages.add(previewImage);
            }

            previewImageRepo.saveAll(previewImages);
            // Optional: push thông báo, log, update trạng thái tài liệu,...
        } catch (Exception e) {
            // Log hoặc retry hoặc lưu trạng thái lỗi tùy chiến lược
            log.error("Lỗi khi tạo preview async", e);
        }
    }

}
