package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResourceResponse;
import vn.kltn.entity.Resource;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.specification.SpecificationUtil;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentPermissionService;
import vn.kltn.service.IResourceService;

import java.util.List;

@Service
@Transactional
public abstract class AbstractResourceService<T extends Resource, R extends ResourceResponse> implements IResourceService<T, R> {
    protected final IDocumentPermissionService documentPermissionService;
    protected final IFolderPermissionService folderPermissionService;
    protected final IAuthenticationService authenticationService;
    protected final AbstractPermissionService abstractPermissionService;

    protected AbstractResourceService(IDocumentPermissionService documentPermissionService,
                                      IFolderPermissionService folderPermissionService,
                                      IAuthenticationService authenticationService,
                                      @Qualifier("documentPermissionServiceImpl") AbstractPermissionService abstractPermissionService) {
        this.documentPermissionService = documentPermissionService;
        this.folderPermissionService = folderPermissionService;
        this.authenticationService = authenticationService;
        this.abstractPermissionService = abstractPermissionService;
    }

    @Override
    public void validateResourceNotDeleted(Resource resource) {
        if (resource.getDeletedAt() != null) {
            throw new InvalidDataException("Resource đã bị xóa");
        }
    }

    @Override
    public void validateResourceDeleted(Resource resource) {
        if (resource.getDeletedAt() == null) {
            throw new InvalidDataException("Resource chưa bị xóa");
        }
    }

    @Override
    public PageResponse<List<R>> searchByCurrentUser(Pageable pageable, String[] resources) {
        if (resources != null && resources.length > 0) {
            EntitySpecificationsBuilder<T> builder = new EntitySpecificationsBuilder<>();
            Specification<T> spec = SpecificationUtil.buildSpecificationFromFilters(resources, builder);
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt")));
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("createdBy"), email));
            Page<T> pageAccessByResource = getPageResourceBySpec(spec, pageable);
            return PaginationUtils.convertToPageResponse(pageAccessByResource, pageable, this::mapToR);
        }
        return PaginationUtils.convertToPageResponse(getPageResource(pageable), pageable, this::mapToR);
    }

    @Override
    public void hardDeleteResourceById(Long resourceId) {
        T resource = getResourceByIdOrThrow(resourceId);
        validateResourceDeleted(resource);
        deletePermissionResourceById(resource.getId());
        hardDeleteResource(resource);
    }

    @Override
    public R getResourceById(Long resourceId) {
        return mapToR(getResourceByIdOrThrow(resourceId));
    }

    @Override
    public void validateCurrentUserIsOwnerResource(T resource) {
        User currentUser = getCurrentUser();
        if (!resource.getOwner().getId().equals(currentUser.getId())) {
            throw new InvalidDataException("Bạn không có quyền thực hiện thao tác này");
        }
    }

    @Override
    public void deleteResourceById(Long resourceId) {
        T resource = getResourceByIdOrThrow(resourceId);
        // resource chua bi xoa
        validateResourceNotDeleted(resource);
        User currentUser = getCurrentUser();
        User owner = resource.getOwner();
        if (currentUser.getId().equals(owner.getId())) {
            // neu la chu so huu thi chuyen vao thung rac
            softDeleteResource(resource);
        } else {
            // nguoi thuc hien co quyen editor
            validateUserIsEditor(resourceId, currentUser.getId());
            if (resource.getParent() != null) {
                //xoa access
                deletePermissionByResourceAndRecipientId(resourceId, currentUser.getId());
            }
            resource.setParent(null);
        }
    }

    protected void deletePermissionByResourceAndRecipientId(Long resourceId, Long recipientId) {
        abstractPermissionService.deleteByResourceAndRecipientId(resourceId, recipientId);
    }

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
