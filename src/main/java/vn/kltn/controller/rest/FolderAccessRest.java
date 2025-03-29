package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.*;
import vn.kltn.service.IFolderAccessService;
import vn.kltn.validation.Create;
import vn.kltn.validation.Update;

import java.util.List;

@RestController
@RequestMapping("/api/v1/folders-access")
@RequiredArgsConstructor
@Validated
public class FolderAccessRest {
    private final IFolderAccessService folderAccessService;

    @PostMapping("/folder/{folderId}")
    public ResponseData<AccessResourceResponse> copy(@PathVariable Long folderId, @Validated(Create.class) @RequestBody AccessRequest accessRequest) {
        return new ResponseData<>(201, "Thành công", folderAccessService.createAccess(folderId, accessRequest));
    }

    @DeleteMapping("/{accessId}")
    public ResponseData<AccessResourceResponse> delete(@PathVariable Long accessId) {
        folderAccessService.deleteAccess(accessId);
        return new ResponseData<>(204, "Thành công");
    }

    @PutMapping("/{accessId}")
    public ResponseData<AccessResourceResponse> update(@PathVariable Long accessId, @Validated(Update.class) @RequestBody AccessRequest accessRequest) {
        return new ResponseData<>(200, "Thành công", folderAccessService.updateAccess(accessId, accessRequest.getPermission()));
    }

    @GetMapping
    public ResponseData<PageResponse<List<AccessResourceResponse>>> getAccessByResource(Pageable pageable, @RequestParam(required = false) String[] resources) {
        return new ResponseData<>(200, "Thành công", folderAccessService.getAccessByResource(pageable, resources));
    }

    @GetMapping("/folder-shared")
    public ResponseData<PageResponse<List<FolderResponse>>> getDocumentSharedByCurrentUser(Pageable pageable, @RequestParam(required = false) String[] folders) {
        return new ResponseData<>(200, "Thành công", folderAccessService.getPageFolderSharedForMe(pageable, folders));
    }
}
