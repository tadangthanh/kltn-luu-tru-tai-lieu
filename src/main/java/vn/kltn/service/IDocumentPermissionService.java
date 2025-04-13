package vn.kltn.service;

import vn.kltn.entity.Document;

import java.util.List;
import java.util.Set;

public interface IDocumentPermissionService extends IPermissionService {
    Set<Long> getUserIdsByDocumentShared(Long documentId);

    void inheritPermissions(List<Document> documents);
    Set<Long> getDocumentIdsByUser(Long userId);
}
