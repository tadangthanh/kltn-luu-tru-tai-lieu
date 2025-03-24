package vn.kltn.service;

import vn.kltn.dto.request.DocumentAccessRequest;
import vn.kltn.dto.response.DocumentAccessResponse;

public interface IDocumentAccessService {
    DocumentAccessResponse createDocumentAccess(Long documentId, DocumentAccessRequest accessRequest);

    void deleteDocumentAccess(Long documentId, Long recipientId);

}
