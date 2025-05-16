package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.CreateSharedLinkRequest;
import vn.kltn.dto.response.OnlyOfficeConfig;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.dto.response.SharedLinkResponse;
import vn.kltn.service.ISharedLinkService;

@RequiredArgsConstructor
@RequestMapping("/api/v1/shared-link")
@RestController
@Validated
public class SharedLinkRest {
    private final ISharedLinkService sharedLinkService;

    @PostMapping
    public ResponseData<SharedLinkResponse> createSharedLink(@Valid @RequestBody CreateSharedLinkRequest request) {
        SharedLinkResponse response = sharedLinkService.createSharedLink(request);
        return new ResponseData<>(201, "Tạo liên kết chia sẻ thành công", response);
    }

    @GetMapping("/{token}")
    public ResponseData<OnlyOfficeConfig> accessSharedLink(@PathVariable String token) {
        OnlyOfficeConfig config = sharedLinkService.accessSharedLink(token);
        return new ResponseData<>(200, "Truy cập liên kết chia sẻ thành công", config);
    }
}
