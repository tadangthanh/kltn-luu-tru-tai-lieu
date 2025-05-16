package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.CreateSharedLinkRequest;
import vn.kltn.dto.request.UpdateSharedLinkRequest;
import vn.kltn.dto.response.OnlyOfficeConfig;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.dto.response.SharedLinkResponse;
import vn.kltn.service.ISharedLinkService;

import java.util.List;

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
    @PutMapping("/{id}/disable")
    public ResponseData<SharedLinkResponse> disableSharedLink(@PathVariable Long id) {
        SharedLinkResponse response = sharedLinkService.disableSharedLink(id);
        return new ResponseData<>(200, "Vô hiệu hóa liên kết chia sẻ thành công", response);
    }
    @PutMapping("/{id}/enable")
    public ResponseData<SharedLinkResponse> enableSharedLink(@PathVariable Long id) {
        SharedLinkResponse response = sharedLinkService.enableSharedLink(id);
        return new ResponseData<>(200, "Kích hoạt liên kết chia sẻ thành công", response);
    }
    @DeleteMapping("/{id}")
    public ResponseData<SharedLinkResponse> deleteSharedLink(@PathVariable Long id) {
        SharedLinkResponse response = sharedLinkService.deleteSharedLink(id);
        return new ResponseData<>(200, "Xóa liên kết chia sẻ thành công", response);
    }

    @PutMapping("/{id}")
    public ResponseData<SharedLinkResponse> updateSharedLink(@PathVariable Long id, @RequestBody UpdateSharedLinkRequest request) {
        SharedLinkResponse response = sharedLinkService.updateSharedLink(id, request);
        return new ResponseData<>(200, "Cập nhật liên kết chia sẻ thành công", response);
    }
    @GetMapping("/items/{itemId}")
    public ResponseData<PageResponse<List<SharedLinkResponse>>> getAllSharedLinks(@PathVariable Long itemId, Pageable pageable) {
        return new ResponseData<>(200, "Lấy danh sách liên kết chia sẻ thành công", sharedLinkService.getAllSharedLinks(itemId,pageable));
    }
}
