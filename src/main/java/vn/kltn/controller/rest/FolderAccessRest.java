package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.AccessResourceResponse;
import vn.kltn.dto.response.FolderAccessResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IFolderAccessService;
import vn.kltn.validation.Create;
import vn.kltn.validation.Update;

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
}
