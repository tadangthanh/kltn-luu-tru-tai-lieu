package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.FolderAccessResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IFolderAccessService;

@RestController
@RequestMapping("/api/v1/folders-access")
@RequiredArgsConstructor
@Validated
public class FolderAccessRest {
    private final IFolderAccessService folderAccessService;

    @PostMapping("/folder/{folderId}")
    public ResponseData<FolderAccessResponse> copy(@PathVariable Long folderId, @Valid @RequestBody AccessRequest accessRequest) {
        return new ResponseData<>(201, "Thành công", folderAccessService.createFolderAccess(folderId, accessRequest));
    }

    @DeleteMapping("/folder/{folderId}/recipient/{recipientId}")
    public ResponseData<FolderAccessResponse> delete(@PathVariable Long folderId, @PathVariable Long recipientId) {
        folderAccessService.deleteFolderAccess(folderId, recipientId);
        return new ResponseData<>(204, "Thành công", null);
    }
}
