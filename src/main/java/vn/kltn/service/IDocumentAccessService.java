package vn.kltn.service;

import vn.kltn.dto.response.AccessResourceResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.DocumentAccess;

public interface IDocumentAccessService extends IAccessService<DocumentAccess, AccessResourceResponse> {
    void inheritAccess(Document document);
}
