package vn.kltn.service.impl;

import org.springframework.stereotype.Service;
import vn.kltn.common.Permission;
import vn.kltn.dto.BaseDto;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.entity.AccessResource;
import vn.kltn.entity.User;
import vn.kltn.service.IAccessService;

@Service
public abstract class AbstractAccessService<T extends AccessResource, R extends BaseDto> implements IAccessService<R> {

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

    protected abstract R mapToR(T access);

    protected abstract void sendEmailInviteAccess(T access, AccessRequest accessRequest);

    protected abstract T createEmptyAccess();

    protected abstract void setResource(T access, Long resourceId);

    protected abstract T findAccessById(Long accessId);

    protected abstract T saveAccess(T access);

    protected abstract void deleteAccessEntity(T access);

    protected abstract User getUserByEmail(String email);

}
