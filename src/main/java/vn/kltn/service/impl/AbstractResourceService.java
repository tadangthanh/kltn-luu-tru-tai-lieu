package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IResourceService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
public abstract class AbstractResourceService<T extends Resource, R extends ResourceResponse> implements IResourceService<T, R> {

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
            Pattern pattern = Pattern.compile("([a-zA-Z0-9_.]+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
            for (String s : resources) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                }
            }
            Specification<T> spec = builder.build();
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
        deleteAccessResourceById(resource.getId());
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
            validateAccessEditor(resourceId, currentUser.getId());
            //xoa access
            deleteAccessByResourceAndRecipientId(resourceId, currentUser.getId());
            resource.setParent(null);
        }
    }

    protected abstract void deleteAccessByResourceAndRecipientId(Long resourceId, Long recipientId);

    protected abstract void validateAccessEditor(Long resourceId, Long userId);

    protected abstract void softDeleteResource(T resource);

    protected abstract void deleteAccessResourceById(Long id);

    protected abstract void hardDeleteResource(T resource);

    protected abstract Page<T> getPageResource(Pageable pageable);

    protected abstract Page<T> getPageResourceBySpec(Specification<T> spec, Pageable pageable);

    protected abstract R mapToR(T resource);

    protected abstract User getCurrentUser();
}
