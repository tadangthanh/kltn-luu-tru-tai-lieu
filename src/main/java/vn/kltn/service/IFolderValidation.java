package vn.kltn.service;

import vn.kltn.dto.request.FolderRequest;

public interface IFolderValidation {
    void validateConditionsToCreateFolder(FolderRequest folderRequest);
}
