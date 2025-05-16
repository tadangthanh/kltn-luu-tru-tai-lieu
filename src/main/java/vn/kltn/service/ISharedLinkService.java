package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.request.CreateSharedLinkRequest;
import vn.kltn.dto.request.UpdateSharedLinkRequest;
import vn.kltn.dto.response.OnlyOfficeConfig;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.SharedLinkResponse;
import vn.kltn.entity.SharedLink;

import java.util.List;

public interface ISharedLinkService {
    SharedLink getSharedLinkByIdOrThrow(Long id);

    SharedLinkResponse createSharedLink(CreateSharedLinkRequest request);

    OnlyOfficeConfig accessSharedLink(String accessToken);

    SharedLinkResponse disableSharedLink(Long id);

    SharedLinkResponse enableSharedLink(Long id);

    SharedLinkResponse deleteSharedLink(Long id);

    SharedLinkResponse getSharedLink(Long id);

    SharedLinkResponse updateSharedLink(Long id, UpdateSharedLinkRequest request);

    PageResponse<List<SharedLinkResponse>> getAllSharedLinks(Long itemId,Pageable pageable);

}
