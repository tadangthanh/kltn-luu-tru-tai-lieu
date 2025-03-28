package vn.kltn.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.service.IFolderService;
import vn.kltn.validation.Create;

import java.util.List;

@RequestMapping("/api/v1/folders")
@RestController
@RequiredArgsConstructor
@Validated
public class FolderRest {
    private final IFolderService folderService;

    @PostMapping
    public ResponseData<FolderResponse> createFolder(@RequestBody @Validated(Create.class) FolderRequest folderRequest) {
        return new ResponseData<>(201, "Thành công", folderService.createFolder(folderRequest));
    }

    @DeleteMapping("/{folderId}/soft")
    public ResponseData<Void> softDeleteFolder(@PathVariable Long folderId) {
        folderService.softDeleteFolderById(folderId);
        return new ResponseData<>(204, "Xóa thành công", null);
    }

    @DeleteMapping("/{folderId}/hard")
    public ResponseData<Void> hardDeleteFolder(@PathVariable Long folderId) {
        folderService.hardDeleteResourceById(folderId);
        return new ResponseData<>(204, "Xóa thành công", null);
    }

    @PostMapping("/{folderId}/restore")
    public ResponseData<FolderResponse> restoreFolder(@PathVariable Long folderId) {
        return new ResponseData<>(200, "Thành công", folderService.restoreResourceById(folderId));
    }

    @PutMapping("/{folderId}")
    public ResponseData<FolderResponse> updateFolder(@PathVariable Long folderId, @RequestBody @Validated(Create.class) FolderRequest folderRequest) {
        return new ResponseData<>(200, "Thành công", folderService.updateFolderById(folderId, folderRequest));
    }

    @PutMapping("/{folderId}/move/{folderParentId}")
    public ResponseData<FolderResponse> moveFolder(@PathVariable Long folderId, @PathVariable Long folderParentId) {
        return new ResponseData<>(200, "Thành công", folderService.moveResourceToFolder(folderId, folderParentId));
    }

    @GetMapping
    public ResponseData<PageResponse<List<FolderResponse>>> search(Pageable pageable, @RequestParam(required = false, value = "folders") String[] folders) {
        return new ResponseData<>(200, "Thành công", folderService.searchByCurrentUser(pageable, folders));
    }

    @GetMapping("/{folderId}")
    public ResponseData<FolderResponse> getFolder(@PathVariable Long folderId) {
        return new ResponseData<>(200, "Thành công", folderService.getResourceById(folderId));
    }
}
