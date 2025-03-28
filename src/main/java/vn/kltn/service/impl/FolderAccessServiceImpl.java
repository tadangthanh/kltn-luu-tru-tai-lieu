package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.AccessResourceResponse;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Folder;
import vn.kltn.entity.FolderAccess;
import vn.kltn.entity.User;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.AccessResourceMapper;
import vn.kltn.repository.FolderAccessRepo;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.specification.FolderSpecification;
import vn.kltn.repository.specification.SpecificationUtil;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IFolderAccessService;
import vn.kltn.service.IMailService;
import vn.kltn.service.IUserService;

import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FOLDER_ACCESS_SERVICE")
public class FolderAccessServiceImpl extends AbstractAccessService<FolderAccess, AccessResourceResponse> implements IFolderAccessService {
    private final FolderAccessRepo folderAccessRepo;
    private final AccessResourceMapper accessResourceMapper;
    private final IUserService userService;
    private final IMailService mailService;
    private final ResourceCommonService resourceCommonService;
    private final IAuthenticationService authenticationService;


    private void validateFolderConditionsAccess(Folder folder) {
        resourceCommonService.validateResourceNotDeleted(folder);
        resourceCommonService.validateCurrentUserIsOwnerResource(folder);
    }

    @Override
    protected FolderAccess getAccessByResourceAndRecipient(Long resourceId, Long recipientId) {
        return folderAccessRepo.findByResourceAndRecipientId(resourceId, recipientId).orElseThrow(() -> {
            ;
            log.warn("Folder access not found by resource id: {} and recipient id: {}", resourceId, recipientId);
            return new ResourceNotFoundException("Bạn không có quyền thực hiện hành động này!");
        });
    }

    @Override
    protected Page<FolderAccess> getPageAccessByResource(Pageable pageable) {
        return folderAccessRepo.findAll(pageable);
    }

    @Override
    protected Page<FolderAccess> getPageAccessByResourceBySpec(Specification<FolderAccess> spec, Pageable pageable) {
        return folderAccessRepo.findAll(spec, pageable);
    }

    @Override
    protected AccessResourceResponse mapToR(FolderAccess access) {
        return accessResourceMapper.toAccessResourceResponse(access);
    }

    @Override
    protected void sendEmailInviteAccess(FolderAccess access, AccessRequest accessRequest) {
        mailService.sendEmailInviteFolderAccess(accessRequest.getRecipientEmail(), access, accessRequest.getMessage());
    }

    @Override
    protected FolderAccess createEmptyAccess() {
        return new FolderAccess();
    }

    @Override
    protected void setResource(FolderAccess access, Long resourceId) {
        Folder folder = resourceCommonService.getFolderByIdOrThrow(resourceId);
        validateFolderConditionsAccess(folder);
        access.setResource(folder);
    }

    @Override
    protected FolderAccess findAccessById(Long accessId) {
        return folderAccessRepo.findById(accessId).orElseThrow(() -> {
            log.warn("Folder access not found by id: {}", accessId);
            return new ResourceNotFoundException("Folder access not found");
        });
    }

    @Override
    protected FolderAccess saveAccess(FolderAccess access) {
        return folderAccessRepo.save(access);
    }

    @Override
    protected void deleteAccessEntity(FolderAccess access) {
        folderAccessRepo.delete(access);
    }

    @Override
    protected User getUserByEmail(String email) {
        return userService.getUserByEmail(email);
    }

    @Override
    protected User getCurrentUser() {
        return authenticationService.getCurrentUser();
    }

    @Override
    protected void deleteAccessByResourceIdAndRecipient(Long resourceId, Long recipientId) {
        folderAccessRepo.deleteByResourceAndRecipientId(resourceId, recipientId);
    }

    @Override
    public Set<FolderAccess> getAllByResourceId(Long resourceId) {
        return folderAccessRepo.findAllByResourceId(resourceId);
    }

    @Override
    public void deleteAccessByResource(Long resourceId) {
        folderAccessRepo.deleteAllByResourceId(resourceId);
    }

    @Override
    public void inheritAccess(Folder newFolder) {
        Folder parent = newFolder.getParent();
        if (parent != null) {
            Set<FolderAccess> folderAccesses = getAllByResourceId(parent.getId());
            folderAccesses.forEach(folderAccess -> {
                FolderAccess folderNewAccess = createEmptyAccess();
                folderNewAccess.setRecipient(folderAccess.getRecipient());
                folderNewAccess.setPermission(folderAccess.getPermission());
                folderNewAccess.setResource(newFolder);
                folderAccessRepo.save(folderNewAccess);
            });
        }
    }

    @Override
    public PageResponse<List<FolderResponse>> getPageFolderSharedForMe(Pageable pageable, String[] folders) {
        log.info("get page folder shared me");
        User currentUser = authenticationService.getCurrentUser();
        if (folders != null && folders.length > 0) {
            EntitySpecificationsBuilder<Folder> builder = new EntitySpecificationsBuilder<>();
            Specification<Folder> spec = SpecificationUtil.buildSpecificationFromFilters(folders, builder);
            // nó trả trả về 1 spec mới
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt")));
            spec = spec.and(FolderSpecification.hasAccessByRecipient(currentUser.getId()));
            Page<Folder> folderPage = resourceCommonService.getPageFolderBySpec(spec, pageable);
            return PaginationUtils.convertToPageResponse(folderPage, pageable, resourceCommonService::mapToFolderResponse);
        }
        Specification<Folder> spec = FolderSpecification.hasAccessByRecipient(currentUser.getId());
        return PaginationUtils.convertToPageResponse(resourceCommonService.getPageFolderBySpec(spec, pageable),
                pageable, resourceCommonService::mapToFolderResponse);
    }
}
