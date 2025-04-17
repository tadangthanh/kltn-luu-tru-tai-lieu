package vn.kltn.service;

import vn.kltn.dto.request.FolderRequest;
import vn.kltn.entity.Folder;

public interface IFolderValidation {
    void validateConditionsToCreateFolder(FolderRequest folderRequest);
    void validateFolderDeleted(Folder folder);
}
