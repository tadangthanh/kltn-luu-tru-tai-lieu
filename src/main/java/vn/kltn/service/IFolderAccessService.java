package vn.kltn.service;

import vn.kltn.dto.request.AccessRequest;
import vn.kltn.dto.response.FolderAccessResponse;

public interface IFolderAccessService {
    FolderAccessResponse createFolderAccess(Long folderId, AccessRequest accessRequest);

    void deleteFolderAccess(Long folderId, Long recipientId);

}
