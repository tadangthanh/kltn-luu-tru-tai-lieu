package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.request.FolderRequest;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.dto.response.PageResponse;

import java.util.List;

public interface IFolderService {
    FolderResponse createFolder(FolderRequest folderRequest);

    void deleteFolderById(Long folderId);

    PageResponse<List<FolderResponse>> searchByCurrentUser(Pageable pageable, String[] folders);

    FolderResponse updateFolderById(Long folderId, FolderRequest folderRequest);

}
