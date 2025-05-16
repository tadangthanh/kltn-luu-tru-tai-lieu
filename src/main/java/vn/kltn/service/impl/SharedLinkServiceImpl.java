package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kltn.dto.request.CreateSharedLinkRequest;
import vn.kltn.dto.response.OnlyOfficeConfig;
import vn.kltn.dto.response.SharedLinkResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.Item;
import vn.kltn.entity.SharedLink;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.SharedLinkMapper;
import vn.kltn.repository.SharedLinkRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IItemService;
import vn.kltn.service.ISharedLinkService;
import vn.kltn.util.DocumentTypeUtil;
import vn.kltn.util.ItemValidator;

import java.time.LocalDateTime;
import java.util.UUID;

import static vn.kltn.repository.util.FileUtil.getFileExtension;

@Service
@Transactional
@Slf4j(topic = "SHARED_LINK_SERVICE")
@RequiredArgsConstructor
public class SharedLinkServiceImpl implements ISharedLinkService {
    private final SharedLinkRepo sharedLinkRepo;
    private final SharedLinkMapper sharedLinkMapper;
    private final IItemService itemService;
    private final IAuthenticationService authenticationService;
    private final ItemValidator itemValidator;
    private final IDocumentService documentService;

    @Override
    public SharedLinkResponse createSharedLink(CreateSharedLinkRequest request) {
        log.info("Create shared link: itemService: {},expiresAt: {}, maxViews: {}", request.getItemId(),
                request.getExpiresAt(), request.getMaxViews());
        Item item = itemService.getItemByIdOrThrow(request.getItemId());
        itemValidator.validateCurrentUserHasAccessToItem(item);
        SharedLink link = sharedLinkMapper.toEntity(request);
        link.setItem(item);
        link.setAccessToken(UUID.randomUUID().toString());
        link.setCurrentViews(0);
        link.setSharedBy(authenticationService.getCurrentUser());
        link.setIsActive(true);
        sharedLinkRepo.save(link);
        return sharedLinkMapper.toResponse(link);
    }

    @Override
    public OnlyOfficeConfig accessSharedLink(String accessToken) {
        log.info("Access shared link: accessToken={}", accessToken);
        SharedLink link = sharedLinkRepo.findByAccessToken(accessToken)
                .orElseThrow(() -> new ResourceNotFoundException("Link không tồn tại"));
        if (!link.getIsActive()) {
            throw new RuntimeException("Link đã bị vô hiệu hóa");
        }

        if (link.getExpiresAt() != null && link.getExpiresAt().isBefore(LocalDateTime.now())) {
            link.setIsActive(false);
            sharedLinkRepo.save(link);
            throw new AccessDeniedException("Link đã hết hạn");
        }

        if (link.getMaxViews() != null && link.getCurrentViews() >= link.getMaxViews()) {
            link.setIsActive(false);
            sharedLinkRepo.save(link);
            throw new AccessDeniedException("Link đã đạt lượt truy cập tối đa");
        }
        // Tăng lượt truy cập
        link.setCurrentViews(link.getCurrentViews() + 1);
        sharedLinkRepo.save(link);

        // Trả về thông tin cấu hình OnlyOffice
        // Tạo cấu hình cho OnlyOffice
        OnlyOfficeConfig config = new OnlyOfficeConfig();
        Item item = link.getItem();
        Document document = documentService.getItemByIdOrThrow(item.getId());
        // Lấy phần mở rộng file từ tên file
        String fileExtension = getFileExtension(document.getName());
        // Sử dụng hàm util để lấy fileType và documentType
        DocumentTypeUtil.DocumentTypeInfo documentTypeInfo = DocumentTypeUtil.getDocumentTypeInfo(fileExtension);
        config.setDocumentId(document.getId());
        String documentKey = document.getId() + "-" + document.getUpdatedAt().getTime() + "-" + document.getCurrentVersion().getBlobName();
        config.setDocumentKey(documentKey);
        config.setDocumentTitle(document.getName());
        config.setFileType(documentTypeInfo.getFileType());
        config.setDocumentType(documentTypeInfo.getDocumentType());
        // Thông tin quyền truy cập người dùng
        OnlyOfficeConfig.Permissions permissions = new OnlyOfficeConfig.Permissions();
        permissions.setEdit(false); // Quyền chỉnh sửa (có thể tùy chỉnh)
        permissions.setComment(false); // Quyền bình luận
        permissions.setDownload(true); // Quyền tải xuống
        config.setPermissions(permissions);
        // Thông tin người dùng
        String userRandomId = UUID.randomUUID().toString();
        OnlyOfficeConfig.User user = new OnlyOfficeConfig.User();
        user.setId(userRandomId); // Lấy từ context hoặc JWT của người dùng
        user.setName(userRandomId); // Lấy từ context hoặc JWT của người dùng
        config.setUser(user);
        return config;
    }
}
