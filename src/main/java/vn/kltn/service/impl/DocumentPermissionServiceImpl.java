package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vn.kltn.entity.FileSystemEntity;
import vn.kltn.map.PermissionMapper;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentPermissionService;
import vn.kltn.service.IUserService;

import java.util.Set;

@Service
@Transactional
public class DocumentPermissionServiceImpl extends AbstractPermissionService implements IDocumentPermissionService {
    private final DocumentCommonService documentCommonService;

    protected DocumentPermissionServiceImpl(
            PermissionRepo permissionRepo,
            IAuthenticationService authenticationService,
            IUserService userService,
            PermissionMapper permissionMapper,
            ResourceCommonService resourceCommonService,
            DocumentCommonService documentCommonService) {
        super(permissionRepo, userService, permissionMapper, resourceCommonService, authenticationService);
        this.documentCommonService = documentCommonService;
    }

    @Override
    protected FileSystemEntity getResourceById(Long resourceId) {
        return documentCommonService.getDocumentByIdOrThrow(resourceId);
    }

    /***
     *  lay danh sach id user ma document nay chia se
     * @param documentId id document
     * @return danh sach id user mà document này chia sẻ
     */
    @Override
    public Set<Long> getUserIdsByDocumentShared(Long documentId) {
        return permissionRepo.findIdsUserSharedWithByResourceId(documentId);
    }

    /***
     *  lay danh sach document duoc chia se cho user nay
     * @param userId : id user
     * @return lay danh sach document duoc chia se cho user nay
     */
    @Override
    public Set<Long> getDocumentIdsByUser(Long userId) {
        return permissionRepo.findIdsDocumentByUserId(userId);
    }
}
