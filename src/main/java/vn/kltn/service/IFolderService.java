package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Folder;

import java.util.List;

public interface IFolderService {
    FolderResponse createFolder(FolderRequest folderRequest);

    void softDeleteFolderById(Long folderId);

    Folder getFolderByIdOrThrow(Long folderId);

    FolderResponse getFolderById(Long folderId);

    void hardDeleteFolderById(Long folderId);

    FolderResponse restoreFolderById(Long folderId);

    PageResponse<List<FolderResponse>> searchByCurrentUser(Pageable pageable, String[] folders);

    FolderResponse updateFolderById(Long folderId, FolderRequest folderRequest);

    FolderResponse moveFolderToFolder(Long folderId, Long folderParentId);



}
