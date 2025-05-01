package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Folder;
import vn.kltn.entity.Item;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.specification.SpecificationUtil;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.*;
import vn.kltn.util.ItemValidator;

import java.util.List;

@Service
@Transactional
public abstract class AbstractItemCommonService<T extends Item, R extends ItemResponse> implements IItemCommonService<T, R> {
//    protected final IDocumentPermissionService documentPermissionService;
//    protected final IFolderPermissionService folderPermissionService;
    protected final IAuthenticationService authenticationService;
//    protected final AbstractPermissionService abstractPermissionService;
    protected final FolderCommonService folderCommonService;
    protected final ItemValidator itemValidator;
    protected final IPermissionInheritanceService permissionInheritanceService;
    protected final IPermissionValidatorService permissionValidatorService;
    protected final IPermissionService permissionService;

//    protected AbstractItemCommonService(IDocumentPermissionService documentPermissionService, IFolderPermissionService folderPermissionService, IAuthenticationService authenticationService, @Qualifier("documentPermissionServiceImpl") AbstractPermissionService abstractPermissionService, FolderCommonService folderCommonService, ItemValidator itemValidator, IPermissionInheritanceService permissionInheritanceService, IPermissionValidatorService permissionValidatorService) {
//        this.documentPermissionService = documentPermissionService;
//        this.folderPermissionService = folderPermissionService;
//        this.authenticationService = authenticationService;
//        this.abstractPermissionService = abstractPermissionService;
//        this.folderCommonService = folderCommonService;
//        this.itemValidator = itemValidator;
//        this.permissionInheritanceService = permissionInheritanceService;
//        this.permissionValidatorService = permissionValidatorService;
//    }
protected AbstractItemCommonService(IAuthenticationService authenticationService, FolderCommonService folderCommonService, ItemValidator itemValidator, IPermissionInheritanceService permissionInheritanceService, IPermissionValidatorService permissionValidatorService, IPermissionService permissionService) {
    this.authenticationService = authenticationService;
    this.folderCommonService = folderCommonService;
    this.itemValidator = itemValidator;
    this.permissionInheritanceService = permissionInheritanceService;
    this.permissionValidatorService = permissionValidatorService;
    this.permissionService = permissionService;
}


    @Override
    public PageResponse<List<R>> searchByCurrentUser(Pageable pageable, String[] items) {
        EntitySpecificationsBuilder<T> builder = new EntitySpecificationsBuilder<>();
        Specification<T> spec;
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (items != null && items.length > 0) {
            spec = SpecificationUtil.buildSpecificationFromFilters(items, builder);
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdBy"), email));
            Page<T> pageAccessByResource = getPageResourceBySpec(spec, pageable);
            return PaginationUtils.convertToPageResponse(pageAccessByResource, pageable, this::mapToR);
        }
        spec = (root, query, criteriaBuilder) -> root.get("deletedAt").isNull();
        spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdBy"), email));
        return PaginationUtils.convertToPageResponse(getPageResourceBySpec(spec, pageable), pageable, this::mapToR);
    }


    @Override
    public R getItemById(Long itemId) {
        return mapToR(getItemByIdOrThrow(itemId));
    }

    @Override
    public R moveItemToFolder(Long itemId, Long folderId) {
        T resourceToMove = getItemByIdOrThrow(itemId);
        //kiem tra xem nguoi dung hien tai co quyen di chuyen folder hay khong ( kiem tra quyen o folder cha)
        itemValidator.validateCurrentUserHasAccessToItem(resourceToMove);
        // folder can di chuyen chua bi xoa
        itemValidator.validateItemNotDeleted(resourceToMove);
        Folder folderDestination = getFolderByIdOrThrow(folderId);
        // folder dich chua bi xoa
        itemValidator.validateItemNotDeleted(folderDestination);
        resourceToMove.setParent(folderDestination);
        resourceToMove = saveResource(resourceToMove);
        // xoa cac permission cu
        permissionService.deletePermissionByItemId(itemId);
        // them cac permission moi cua folder cha moi
        permissionInheritanceService.inheritPermissionsFromParentFolder(resourceToMove);
        return mapToR(resourceToMove);
    }

    protected Folder getFolderByIdOrThrow(Long folderId) {
        return folderCommonService.getFolderByIdOrThrow(folderId);
    }

    protected abstract T saveResource(T resource);


    protected abstract Page<T> getPageResourceBySpec(Specification<T> spec, Pageable pageable);

    protected abstract R mapToR(T resource);

    protected User getCurrentUser() {
        return authenticationService.getCurrentUser();
    }
}
