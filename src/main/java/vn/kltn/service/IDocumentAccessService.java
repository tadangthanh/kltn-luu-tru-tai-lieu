package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.response.AccessResourceResponse;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.DocumentAccess;

import java.util.List;

public interface IDocumentAccessService extends IAccessService<DocumentAccess, AccessResourceResponse> {
    void inheritAccess(Document document);

    PageResponse<List<DocumentResponse>> getPageDocumentSharedByCurrentUser(Pageable pageable,String[] documents);
}
