package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.PermissionRequest;
import vn.kltn.dto.response.PermissionResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.FileSystemEntity;
import vn.kltn.map.PermissionMapper;
import vn.kltn.repository.PermissionRepo;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.IDocumentPermissionService;
import vn.kltn.service.IUserService;

@Service
@Transactional
public class DocumentPermissionServiceImpl extends AbstractPermissionService<Document> implements IDocumentPermissionService {
    private final DocumentCommonService documentCommonService;

    protected DocumentPermissionServiceImpl(
            PermissionRepo permissionRepo,
            IAuthenticationService authenticationService,
            IUserService userService,
            PermissionMapper permissionMapper,
            ResourceCommonService resourceCommonService, DocumentCommonService documentCommonService) {
        super(permissionRepo, userService, permissionMapper, resourceCommonService,authenticationService);
        this.documentCommonService = documentCommonService;
    }

//    @Override
//    public PermissionResponse setPermissionResource(Long resourceId, PermissionRequest permissionRequest) {
//        return null;
//    }

    @Override
    protected FileSystemEntity getResourceById(Long resourceId) {
        return documentCommonService.getDocumentByIdOrThrow(resourceId);
    }

}
