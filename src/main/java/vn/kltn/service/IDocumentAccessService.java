package vn.kltn.service;

import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.DocumentAccessResponse;

public interface IDocumentAccessService {
    DocumentAccessResponse createDocumentAccess(Long documentId, AccessRequest accessRequest);

    void deleteDocumentAccess(Long documentId, Long recipientId);

}
