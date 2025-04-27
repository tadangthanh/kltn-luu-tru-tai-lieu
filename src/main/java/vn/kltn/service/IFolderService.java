package vn.kltn.service;

import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.entity.Folder;

public interface IFolderService extends IItemCommonService<Folder, FolderResponse> {
    FolderResponse createFolder(FolderRequest folderRequest);

    Folder getFolderByIdOrThrow(Long folderId);

    FolderResponse updateFolderById(Long folderId, FolderRequest folderRequest);

}
