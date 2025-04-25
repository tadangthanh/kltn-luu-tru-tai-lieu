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
import vn.kltn.exception.InvalidDataException;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.specification.SpecificationUtil;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentPermissionService;
import vn.kltn.service.IFolderPermissionService;
import vn.kltn.service.IItemService;

import java.util.List;

@Service
@Transactional
public abstract class AbstractItemService<T extends Item, R extends ItemResponse> implements IItemService<T, R> {
    protected final IDocumentPermissionService documentPermissionService;
    protected final IFolderPermissionService folderPermissionService;
    protected final IAuthenticationService authenticationService;
    protected final AbstractPermissionService abstractPermissionService;
    protected final FolderCommonService folderCommonService;

    protected AbstractItemService(IDocumentPermissionService documentPermissionService, IFolderPermissionService folderPermissionService, IAuthenticationService authenticationService, @Qualifier("documentPermissionServiceImpl") AbstractPermissionService abstractPermissionService, FolderCommonService folderCommonService) {
        this.documentPermissionService = documentPermissionService;
        this.folderPermissionService = folderPermissionService;
        this.authenticationService = authenticationService;
        this.abstractPermissionService = abstractPermissionService;
        this.folderCommonService = folderCommonService;
    }

    @Override
    public void validateItemNotDeleted(Item item) {
        if (item.getDeletedAt() != null) {
            throw new InvalidDataException("Resource đã bị xóa");
        }
    }

    @Override
    public void validateItemDeleted(Item item) {
        if (item.getDeletedAt() == null) {
            throw new InvalidDataException("Resource chưa bị xóa");
        }
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
        return PaginationUtils.convertToPageResponse(getPageResourceBySpec(spec,pageable), pageable, this::mapToR);
    }

    @Override
    public void hardDeleteItemById(Long itemId) {
        T resource = getItemByIdOrThrow(itemId);
        validateItemDeleted(resource);
        deletePermissionResourceById(resource.getId());
        hardDeleteResource(resource);
    }

    @Override
    public R getItemById(Long itemId) {
        return mapToR(getItemByIdOrThrow(itemId));
    }

    @Override
    public void validateCurrentUserIsOwnerItem(Item item) {
        User currentUser = getCurrentUser();
        if (!item.getOwner().getId().equals(currentUser.getId())) {
            throw new InvalidDataException("Bạn không có quyền thực hiện thao tác này");
        }
    }

    /**
     * Kiểm tra xem người dùng hiện tại có quyền sở hữu hoặc chỉnh sửa tài nguyên hay không
     *
     * @param item Tài nguyên cần kiểm tra
     */
    @Override
    public void validateCurrentUserIsOwnerOrEditorItem(Item item) {
        User currentUser = getCurrentUser();
        // neu la chu so huu thi ko can kiem tra quyen editor
        if (item.getOwner().getId().equals(currentUser.getId())) {
            return;
        }
        // nếu khong là chu so huu thi kiem tra quyen editor
        validateUserIsEditor(item.getId(), currentUser.getId());
    }

    @Override
    public void deleteItemById(Long itemId) {
        T resource = getItemByIdOrThrow(itemId);
        // resource chua bi xoa
        validateItemNotDeleted(resource);
        // validate chu so huu hoac editor o resource cha
        if (resource.getParent() != null) {
            validateCurrentUserIsOwnerOrEditorItem(resource.getParent());
        }
        User currentUser = getCurrentUser();
        User owner = resource.getOwner();
        if (currentUser.getId().equals(owner.getId())) {
            // neu la chu so huu thi chuyen vao thung rac
            softDeleteResource(resource);
        } else {
            // nguoi thuc hien co quyen editor
            validateUserIsEditor(itemId, currentUser.getId());
            // set parent = null la se dua resource nay vao drive cua toi
            resource.setParent(null);
        }
    }

    @Override
    public R moveItemToFolder(Long itemId, Long folderId) {
        T resourceToMove = getItemByIdOrThrow(itemId);
        //kiem tra xem nguoi dung hien tai co quyen di chuyen folder hay khong ( kiem tra quyen o folder cha)
        validateCurrentUserIsOwnerOrEditorItem(resourceToMove.getParent());
        // folder can di chuyen chua bi xoa
        validateItemNotDeleted(resourceToMove);
        Folder folderDestination = getFolderByIdOrThrow(folderId);
        // folder dich chua bi xoa
        validateItemNotDeleted(folderDestination);
        resourceToMove.setParent(folderDestination);
        resourceToMove = saveResource(resourceToMove);
        // xoa cac permission cu
        folderPermissionService.deletePermissionByResourceId(itemId);
        // them cac permission moi cua folder cha moi
        folderPermissionService.inheritPermissions(resourceToMove);
        return mapToR(resourceToMove);
    }

    protected Folder getFolderByIdOrThrow(Long folderId) {
        return folderCommonService.getFolderByIdOrThrow(folderId);
    }

    protected abstract T saveResource(T resource);


    protected void validateUserIsEditor(Long resourceId, Long userId) {
        abstractPermissionService.validateUserIsEditor(resourceId, userId);
    }

    protected abstract void softDeleteResource(T resource);

    protected void deletePermissionResourceById(Long resourceId) {
        abstractPermissionService.deletePermissionByResourceId(resourceId);
    }

    protected abstract void hardDeleteResource(T resource);

    protected abstract Page<T> getPageResource(Pageable pageable);

    protected abstract Page<T> getPageResourceBySpec(Specification<T> spec, Pageable pageable);

    protected abstract R mapToR(T resource);

    protected User getCurrentUser() {
        return authenticationService.getCurrentUser();
    }
}
