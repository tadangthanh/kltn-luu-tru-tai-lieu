package vn.kltn.service;

import vn.kltn.common.CancellationToken;
import vn.kltn.dto.FileBuffer;
import vn.kltn.dto.FolderContent;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.entity.Folder;

import java.util.List;

public interface IFolderService extends IItemCommonService<Folder, FolderResponse> {
    FolderResponse createFolder(FolderRequest folderRequest);

    Folder getFolderByIdOrThrow(Long folderId);

    void softDeleteFolderById(Long folderId);

    FolderResponse updateFolderById(Long folderId, FolderRequest folderRequest);

    List<FolderContent> getAllContents(Long folderId, String currentPath);

    void hardDeleteFolderById(Long folderId);

    void uploadFolderNullParent(List<FileBuffer> fileBufferList, CancellationToken token);
}
