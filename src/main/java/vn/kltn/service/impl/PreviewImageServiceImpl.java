package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.PreviewPageSelectionRequest;
import vn.kltn.exception.InternalServerErrorException;
import vn.kltn.repository.PreviewImageRepo;
import vn.kltn.service.IPreviewImageService;

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
    private final AsyncPreviewImageService asyncPreviewImageService;

    @Override
    public String createPreviewImages(PreviewPageSelectionRequest request) {
        log.info("Yêu cầu tạo preview async cho docId: {}", request.getDocumentId());

        // Lấy các trang đã có preview
        List<Integer> existingPages = previewImageRepo.findAllPageNumbersByDocumentId(request.getDocumentId());
        Set<Integer> existingPageSet = new HashSet<>(existingPages);

        // Lọc ra các trang chưa được tạo preview
        List<Integer> pagesToGenerate = request.getPageNumbers()
                .stream()
                .filter(page -> !existingPageSet.contains(page))
                .distinct()
                .collect(Collectors.toList());

        if (pagesToGenerate.isEmpty()) {
            return "Tất cả các trang đã được tạo preview.";
        }

//        // Gọi task async để chuyển đổi preview (không chặn API)
        asyncPreviewImageService.generatePreviewImagesAsync(request.getDocumentId(), pagesToGenerate)
                .thenAccept(previewImages -> {
                    log.info("Tạo preview thành công cho {} trang", previewImages.size());
                    // Lưu các preview vào DB
                    previewImageRepo.saveAll(previewImages);
                    // thong bao
                })
                .exceptionally(ex -> {
                    log.error("Lỗi tạo preview: {}", ex.getMessage());
                    throw new InternalServerErrorException("Có lỗi xảy ra khi tạo preview tài liệu");
                });
        // Trả về response ngay lập tức
        return "Yêu cầu đang được xử lý, preview tài liệu sẽ sớm được tạo!";
    }


}
