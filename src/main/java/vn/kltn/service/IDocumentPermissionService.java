package vn.kltn.service;

import java.util.Set;

public interface IDocumentPermissionService extends IPermissionService {
    Set<Long> getUserIdsByDocumentShared(Long documentId);

    Set<Long> getDocumentIdsByUser(Long userId);
}
