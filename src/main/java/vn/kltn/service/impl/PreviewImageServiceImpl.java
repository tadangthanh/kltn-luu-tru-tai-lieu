package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.PreviewPageSelectionRequest;
import vn.kltn.entity.Document;
import vn.kltn.entity.PreviewImage;
import vn.kltn.exception.InternalServerErrorException;
import vn.kltn.repository.PreviewImageRepo;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IDocumentConversionService;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IPreviewImageService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "PREVIEW_IMAGE_SERVICE")
public class PreviewImageServiceImpl implements IPreviewImageService {
    private final PreviewImageRepo previewImageRepo;
    private final IDocumentService documentService;
    private final IDocumentConversionService documentConversionService;
    private final IAzureStorageService azureStorageService;

    @Override
    public String createPreviewImages(PreviewPageSelectionRequest request) {
        log.info("Yêu cầu tạo preview async cho docId: {}", request.getDocumentId());

        List<Integer> existingPages = previewImageRepo.findAllPageNumbersByDocumentId(request.getDocumentId());
        Set<Integer> existingPageSet = new HashSet<>(existingPages);
        // Lọc ra các trang chưa tồn tại
        List<Integer> pagesToGenerate = request.getPageNumbers()
                .stream()
                .filter(page -> !existingPageSet.contains(page))
                .distinct()
                .collect(Collectors.toList());

        if (pagesToGenerate.isEmpty()) {
            return "Tất cả các trang đã được tạo preview.";
        }
        generatePreviewImagesAsync(request.getDocumentId(), pagesToGenerate);
        return "Yêu cầu đang được xử lý, preview tài liệu sẽ sớm được tạo!";
    }

    @Async
    public void generatePreviewImagesAsync(Long documentId, List<Integer> pages) {
        List<String> imageBlobNames = new ArrayList<>();
        try {
            Document document = documentService.getResourceByIdOrThrow(documentId);
            imageBlobNames.addAll(documentConversionService.convertPdfToImagesAndUpload(document.getBlobName(), pages));
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
            previewImageRepo.saveAll(previewImages);
        } catch (Exception e) {
            azureStorageService.deleteBLobs(imageBlobNames);
            log.error("Lỗi khi tạo preview async", e);
        }
    }


}
