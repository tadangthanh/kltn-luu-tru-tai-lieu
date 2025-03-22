package vn.kltn.service;

import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;

public interface IFolderService {
    FolderResponse createFolder(FolderRequest folderRequest);

    void deleteFolderById(Long folderId);

    FolderResponse updateFolderById(Long folderId, FolderRequest folderRequest);

}
