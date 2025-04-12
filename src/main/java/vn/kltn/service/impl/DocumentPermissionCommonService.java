package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.kltn.repository.PermissionRepo;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DocumentPermissionCommonService {
    private final PermissionRepo permissionRepo;

    public Set<Long> getUserIdsByDocumentShared(Long documentId) {
        return permissionRepo.findIdsUserSharedWithByResourceId(documentId);
    }
}
