package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
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
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.specification.SpecificationUtil;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.*;
import vn.kltn.util.ItemValidator;

import java.util.List;

@Service
@Transactional
public abstract class AbstractItemCommonService<T extends Item, R extends ItemResponse> implements IItemCommonService<T, R> {
    protected final IDocumentPermissionService documentPermissionService;
    protected final IFolderPermissionService folderPermissionService;
    protected final IAuthenticationService authenticationService;
    protected final AbstractPermissionService abstractPermissionService;
    protected final FolderCommonService folderCommonService;
    protected final ItemValidator itemValidator;
    protected final IPermissionInheritanceService permissionInheritanceService;
    protected final IPermissionValidatorService permissionValidatorService;

    protected AbstractItemCommonService(IDocumentPermissionService documentPermissionService, IFolderPermissionService folderPermissionService, IAuthenticationService authenticationService, @Qualifier("documentPermissionServiceImpl") AbstractPermissionService abstractPermissionService, FolderCommonService folderCommonService, ItemValidator itemValidator, IPermissionInheritanceService permissionInheritanceService, IPermissionValidatorService permissionValidatorService) {
        this.documentPermissionService = documentPermissionService;
        this.folderPermissionService = folderPermissionService;
        this.authenticationService = authenticationService;
        this.abstractPermissionService = abstractPermissionService;
        this.folderCommonService = folderCommonService;
        this.itemValidator = itemValidator;
        this.permissionInheritanceService = permissionInheritanceService;
        this.permissionValidatorService = permissionValidatorService;
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
    public void hardDeleteItemById(Long itemId) {
        T resource = getItemByIdOrThrow(itemId);
        itemValidator.validateItemDeleted(resource);
        deletePermissionResourceById(resource.getId());
        hardDeleteResource(resource);
    }

    @Override
    public R getItemById(Long itemId) {
        return mapToR(getItemByIdOrThrow(itemId));
    }

    @Override
    public void deleteItemById(Long itemId) {
        T item = getItemByIdOrThrow(itemId);
        // resource chua bi xoa
        itemValidator.validateItemNotDeleted(item);
        // validate chu so huu hoac editor o resource cha
        if (item.getParent() != null) {
            itemValidator.validateCurrentUserIsOwnerOrEditorItem(item.getParent());
        }
        User currentUser = getCurrentUser();
        User owner = item.getOwner();
        if (currentUser.getId().equals(owner.getId())) {
            // neu la chu so huu thi chuyen vao thung rac
            softDeleteResource(item);
        } else {
            // nguoi thuc hien co quyen editor
            permissionValidatorService.validatePermissionManager(item, currentUser);
            // set parent = null la se dua resource nay vao drive cua toi
            item.setParent(null);
        }
    }

    @Override
    public R moveItemToFolder(Long itemId, Long folderId) {
        T resourceToMove = getItemByIdOrThrow(itemId);
        //kiem tra xem nguoi dung hien tai co quyen di chuyen folder hay khong ( kiem tra quyen o folder cha)
        itemValidator.validateCurrentUserIsOwnerOrEditorItem(resourceToMove.getParent());
        // folder can di chuyen chua bi xoa
        itemValidator.validateItemNotDeleted(resourceToMove);
        Folder folderDestination = getFolderByIdOrThrow(folderId);
        // folder dich chua bi xoa
        itemValidator.validateItemNotDeleted(folderDestination);
        resourceToMove.setParent(folderDestination);
        resourceToMove = saveResource(resourceToMove);
        // xoa cac permission cu
        folderPermissionService.deletePermissionByItemId(itemId);
        // them cac permission moi cua folder cha moi
        permissionInheritanceService.inheritPermissionsFromParentFolder(resourceToMove);
        return mapToR(resourceToMove);
    }

    protected Folder getFolderByIdOrThrow(Long folderId) {
        return folderCommonService.getFolderByIdOrThrow(folderId);
    }

    protected abstract T saveResource(T resource);


    protected abstract void softDeleteResource(T resource);

    protected void deletePermissionResourceById(Long resourceId) {
        abstractPermissionService.deletePermissionByItemId(resourceId);
    }

    protected abstract void hardDeleteResource(T resource);

    protected abstract Page<T> getPageResourceBySpec(Specification<T> spec, Pageable pageable);

    protected abstract R mapToR(T resource);

    protected User getCurrentUser() {
        return authenticationService.getCurrentUser();
    }
}
