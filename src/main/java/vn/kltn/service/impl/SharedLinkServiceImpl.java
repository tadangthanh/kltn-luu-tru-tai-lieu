package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.kltn.dto.request.CreateSharedLinkRequest;
import vn.kltn.dto.request.UpdateSharedLinkRequest;
import vn.kltn.dto.response.OnlyOfficeConfig;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.SharedLinkResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.Item;
import vn.kltn.entity.SharedLink;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.SharedLinkMapper;
import vn.kltn.repository.SharedLinkRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentService;
import vn.kltn.service.IItemService;
import vn.kltn.service.ISharedLinkService;
import vn.kltn.util.DocumentTypeUtil;
import vn.kltn.util.ItemValidator;
import vn.kltn.util.PaginationUtils;

import java.time.LocalDateTime;
import java.util.List;
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
    public SharedLink getSharedLinkByIdOrThrow(Long id) {
        log.info("Get shared link by id: {}", id);
        return sharedLinkRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Link không tồn tại"));
    }

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

    @Override
    public SharedLinkResponse disableSharedLink(Long id) {
        log.info("Disable shared link: id={}", id);
        SharedLink link = getSharedLinkByIdOrThrow(id);
        if (!link.getIsActive()) {
            throw new AccessDeniedException("Link đã bị vô hiệu hóa");
        }
        Item item = link.getItem();
        itemValidator.validateCurrentUserHasAccessToItem(item);
        link.setIsActive(false);
        sharedLinkRepo.save(link);
        return sharedLinkMapper.toResponse(link);
    }

    @Override
    public SharedLinkResponse enableSharedLink(Long id) {
        log.info("Enable shared link: id={}", id);
        SharedLink link = getSharedLinkByIdOrThrow(id);
        if (link.getIsActive()) {
            throw new AccessDeniedException("Link đã được kích hoạt");
        }
        Item item = link.getItem();
        itemValidator.validateCurrentUserHasAccessToItem(item);
        link.setIsActive(true);
        sharedLinkRepo.save(link);
        return sharedLinkMapper.toResponse(link);
    }

    @Override
    public SharedLinkResponse deleteSharedLink(Long id) {
        log.info("Delete shared link: id={}", id);
        SharedLink link = getSharedLinkByIdOrThrow(id);
        Item item = link.getItem();
        itemValidator.validateCurrentUserHasAccessToItem(item);
        sharedLinkRepo.delete(link);
        return sharedLinkMapper.toResponse(link);
    }

    @Override
    public SharedLinkResponse getSharedLink(Long id) {
        log.info("Get shared link: id={}", id);
        SharedLink link = getSharedLinkByIdOrThrow(id);
        if (!link.getIsActive()) {
            throw new AccessDeniedException("Link đã bị vô hiệu hóa");
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
        return sharedLinkMapper.toResponse(link);
    }

    @Override
    public SharedLinkResponse updateSharedLink(Long id, UpdateSharedLinkRequest request) {
        log.info("Update shared link: id={}, request={}", id, request);
        SharedLink link = getSharedLinkByIdOrThrow(id);
        if (!link.getIsActive()) {
            throw new AccessDeniedException("Link đã bị vô hiệu hóa");
        }
        Item item = link.getItem();
        itemValidator.validateCurrentUserHasAccessToItem(item);
        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AccessDeniedException("Thời gian hết hạn không hợp lệ");
        }
        if (request.getMaxViews() != null && request.getMaxViews() <= 0) {
            throw new AccessDeniedException("Số lượt truy cập không hợp lệ");
        }
        // Cập nhật thông tin link
        if(request.getExpiresAt() != null) {
            link.setExpiresAt(request.getExpiresAt());
        }
        if(request.getMaxViews() != null) {
            link.setMaxViews(request.getMaxViews());
        }
        sharedLinkRepo.save(link);
        // Trả về thông tin link đã cập nhật
        return sharedLinkMapper.toResponse(link);
    }

    @Override
    public PageResponse<List<SharedLinkResponse>> getAllSharedLinks(Long itemId,Pageable pageable) {
        log.info("Get all shared links:itemId:{}, page no:{}, page size:{}",itemId, pageable.getPageNumber(), pageable.getPageSize());
        User currentUser = authenticationService.getCurrentUser();
        Page<SharedLink> page = sharedLinkRepo.findAllBySharedByItemId(itemId,currentUser.getId(), pageable);
        return PaginationUtils.convertToPageResponse(page, pageable, sharedLinkMapper::toResponse);
    }
}
