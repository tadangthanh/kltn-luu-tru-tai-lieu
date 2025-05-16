package vn.kltn.service;

import vn.kltn.dto.request.CreateSharedLinkRequest;
import vn.kltn.dto.response.OnlyOfficeConfig;
import vn.kltn.dto.response.SharedLinkResponse;

public interface ISharedLinkService {
    SharedLinkResponse createSharedLink(CreateSharedLinkRequest request);

    OnlyOfficeConfig accessSharedLink(String accessToken);
}
