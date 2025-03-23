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

    @DeleteMapping("/{folderId}")
    public ResponseData<Void> deleteFolder(@PathVariable Long folderId) {
        folderService.softDeleteFolderById(folderId);
        return new ResponseData<>(204, "Xóa thành công", null);
    }

    @PutMapping("/{folderId}")
    public ResponseData<FolderResponse> updateFolder(@PathVariable Long folderId, @RequestBody @Validated(Create.class) FolderRequest folderRequest) {
        return new ResponseData<>(200, "Thành công", folderService.updateFolderById(folderId, folderRequest));
    }

    @GetMapping
    public ResponseData<PageResponse<List<FolderResponse>>> search(Pageable pageable, @RequestParam(required = false, value = "folders") String[] folders) {
        return new ResponseData<>(200, "Thành công", folderService.searchByCurrentUser(pageable, folders));
    }
}
