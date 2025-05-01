package vn.kltn.controller.rest;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.FolderContent;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.entity.Folder;
import vn.kltn.service.IAzureStorageService;
import vn.kltn.service.IFolderService;
import vn.kltn.validation.Create;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequestMapping("/api/v1/folders")
@RestController
@RequiredArgsConstructor
@Validated
public class FolderRest {
    private final IFolderService folderService;
    private final IAzureStorageService azureStorageService;

    @PostMapping
    public ResponseData<FolderResponse> createFolder(@RequestBody @Validated(Create.class) FolderRequest folderRequest) {
        return new ResponseData<>(201, "Thành công", folderService.createFolder(folderRequest));
    }

    @DeleteMapping("/{folderId}/hard")
    public ResponseData<Void> hardDeleteFolder(@PathVariable Long folderId) {
        folderService.hardDeleteFolderById(folderId);
        return new ResponseData<>(204, "Xóa thành công", null);
    }

    @PostMapping("/{folderId}/restore")
    public ResponseData<FolderResponse> restoreFolder(@PathVariable Long folderId) {
        return new ResponseData<>(200, "Thành công", folderService.restoreItemById(folderId));
    }

    @PutMapping("/{folderId}")
    public ResponseData<FolderResponse> updateFolder(@PathVariable Long folderId, @RequestBody @Validated(Create.class) FolderRequest folderRequest) {
        return new ResponseData<>(200, "Thành công", folderService.updateFolderById(folderId, folderRequest));
    }

    @PutMapping("/{folderId}/move/{folderParentId}")
    public ResponseData<FolderResponse> moveFolder(@PathVariable Long folderId, @PathVariable Long folderParentId) {
        return new ResponseData<>(200, "Thành công", folderService.moveItemToFolder(folderId, folderParentId));
    }

    @GetMapping
    public ResponseData<PageResponse<List<FolderResponse>>> search(Pageable pageable, @RequestParam(required = false, value = "folders") String[] folders) {
        return new ResponseData<>(200, "Thành công", folderService.searchByCurrentUser(pageable, folders));
    }

    @GetMapping("/{folderId}")
    public ResponseData<FolderResponse> getFolder(@PathVariable Long folderId) {
        return new ResponseData<>(200, "Thành công", folderService.getItemById(folderId));
    }


    @GetMapping("/{folderId}/download")
    public void downloadFolder(@PathVariable Long folderId, HttpServletResponse response) throws IOException {
        Folder folder = folderService.getFolderByIdOrThrow(folderId);
        List<FolderContent> contents = folderService.getAllContents(folderId, folder.getName());

        response.setContentType("application/zip");
        String rawFileName = folder.getName() + ".zip";
        String encodedFileName = URLEncoder.encode(rawFileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        response.setHeader("Content-Disposition", "attachment; filename=\"" + rawFileName + "\"; filename*=UTF-8''" + encodedFileName);

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            Set<String> addedPaths = new HashSet<>();

            for (FolderContent content : contents) {
                String path = content.getPath();

                // Bỏ qua nếu path đã được thêm vào trước đó
                if (!addedPaths.add(path)) continue;

                ZipEntry entry = new ZipEntry(path);
                zipOut.putNextEntry(entry);

                if (!content.isFolder()) {
                    try (InputStream fileInputStream = azureStorageService.downloadBlobInputStream(content.getBlobName())) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            zipOut.write(buffer, 0, bytesRead);
                        }
                    }
                }

                zipOut.closeEntry();
            }

            zipOut.finish();
        }

    }


}
