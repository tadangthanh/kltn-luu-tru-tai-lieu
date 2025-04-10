package vn.kltn.service;

import vn.kltn.entity.Document;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface IDocumentPermissionService extends IPermissionService {
    Set<Long> getSharedWithByDocumentId(Long documentId);
}
