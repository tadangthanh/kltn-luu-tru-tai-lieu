package vn.kltn.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.kltn.common.Permission;
import vn.kltn.dto.BaseDto;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.AccessResource;
import vn.kltn.entity.User;
import vn.kltn.exception.InvalidDataException;
import vn.kltn.repository.specification.EntitySpecificationsBuilder;
import vn.kltn.repository.specification.SpecificationUtil;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IAccessService;

import java.util.List;

@Service
public abstract class AbstractAccessService<T extends AccessResource, R extends BaseDto> implements IAccessService<T, R> {

    // Phương thức tạo mới access - dùng chung cho DocumentAccess và FolderAccess
    @Override
    public R createAccess(Long resourceId, AccessRequest accessRequest) {
        T access = createEmptyAccess();
        setResource(access, resourceId);
        access.setRecipient(getUserByEmail(accessRequest.getRecipientEmail()));
        access.setPermission(accessRequest.getPermission());
        sendEmailInviteAccess(access, accessRequest);
        return mapToR(saveAccess(access));
    }

    @Override
    public R updateAccess(Long accessId, Permission newPermission) {
        T access = findAccessById(accessId);
        access.setPermission(newPermission);
        return mapToR(saveAccess(access));
    }

    @Override
    public void deleteAccess(Long accessId) {
        T access = findAccessById(accessId);
        deleteAccessEntity(access);
    }

    @Override
    public PageResponse<List<R>> getAccessByResource(Pageable pageable, String[] resources) {
        if (resources != null && resources.length > 0) {
            EntitySpecificationsBuilder<T> builder = new EntitySpecificationsBuilder<>();
            Specification<T> spec = SpecificationUtil.buildSpecificationFromFilters(resources, builder);
            Page<T> pageAccessByResource = getPageAccessByResourceBySpec(spec, pageable);
            return PaginationUtils.convertToPageResponse(pageAccessByResource, pageable, this::mapToR);
        }
        return PaginationUtils.convertToPageResponse(getPageAccessByResource(pageable), pageable, this::mapToR);
    }

    @Override
    public void deleteAccessByResourceIdRecipient(Long resourceId, Long recipientId) {
        deleteAccessByResourceIdAndRecipient(resourceId, recipientId);
    }

    @Override
    public void validateUserIsEditor(Long resourceId, Long userId) {
        T access = getAccessByResourceAndRecipient(resourceId, userId);
        if (access.getPermission() != Permission.EDITOR) {
            throw new InvalidDataException("Bạn không có quyền thực hiện hành động này!");
        }
    }


    protected abstract T getAccessByResourceAndRecipient(Long resourceId, Long recipientId);

    protected abstract Page<T> getPageAccessByResource(Pageable pageable);

    protected abstract Page<T> getPageAccessByResourceBySpec(Specification<T> spec, Pageable pageable);

    protected abstract R mapToR(T access);

    protected abstract void sendEmailInviteAccess(T access, AccessRequest accessRequest);

    protected abstract T createEmptyAccess();

    protected abstract void setResource(T access, Long resourceId);

    protected abstract T findAccessById(Long accessId);

    protected abstract T saveAccess(T access);

    protected abstract void deleteAccessEntity(T access);

    protected abstract User getUserByEmail(String email);

    protected abstract User getCurrentUser();

    protected abstract void deleteAccessByResourceIdAndRecipient(Long resourceId, Long recipientId);


}
