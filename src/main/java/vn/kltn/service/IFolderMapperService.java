package vn.kltn.service;

import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.entity.Folder;

public interface IFolderMapperService {
    Folder mapToFolder(FolderRequest folderRequest);
    FolderResponse mapToResponse(Folder folder);
    void updateFolder(Folder folder, FolderRequest folderRequest);
}
