package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.response.ItemSearchResponse;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.dto.response.PageResponse;

import java.util.List;

public interface IDocumentSearchService {
    PageResponse<List<DocumentResponse>> searchByCurrentUser(Pageable pageable, String[] documents);
    List<ItemSearchResponse> getMyDocument(String query, int page, int size);
}
