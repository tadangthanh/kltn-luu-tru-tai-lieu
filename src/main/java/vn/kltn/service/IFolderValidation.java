package vn.kltn.service;

import vn.kltn.dto.request.FolderRequest;
import vn.kltn.entity.Folder;
import vn.kltn.entity.Resource;

public interface IFolderValidation {
    void validateConditionsToCreateFolder(FolderRequest folderRequest);

    void validateFolderDeleted(Folder folder);

    void validateResourceNotDeleted(Resource resource);
}
