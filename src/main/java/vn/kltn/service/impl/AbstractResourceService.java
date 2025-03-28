package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public abstract class AbstractResourceService<T extends Resource, R extends ResourceResponse> implements IResourceService<T, R> {
    @Value("${app.delete.document-retention-days}")
    private int documentRetentionDays;

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

//    @Override
//    public R restoreResourceById(Long resourceId) {
//        T resource = getResourceByIdOrThrow(resourceId);
//        validateCurrentUserIsOwnerResource(resource);
//        validateResourceDeleted(resource);
//        resource.setDeletedAt(null);
//        resource.setPermanentDeleteAt(null);
//        return mapToR(resource);
//    }

    @Override
    public void softDeleteResourceById(Long resourceId) {
        T resource = getResourceByIdOrThrow(resourceId);
        validateCurrentUserIsOwnerResource(resource);
        validateResourceNotDeleted(resource);
        resource.setDeletedAt(LocalDateTime.now());
        resource.setPermanentDeleteAt(LocalDateTime.now().plusDays(documentRetentionDays));
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
        deleteResource(resource);
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


    protected abstract void deleteResource(T resource);

    protected abstract Page<T> getPageResource(Pageable pageable);

    protected abstract Page<T> getPageResourceBySpec(Specification<T> spec, Pageable pageable);

    protected abstract R mapToR(T resource);

    protected abstract User getCurrentUser();
}
