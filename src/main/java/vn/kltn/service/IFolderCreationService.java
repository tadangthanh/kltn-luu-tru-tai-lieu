package vn.kltn.service;

import vn.kltn.dto.request.FolderRequest;
import vn.kltn.entity.Folder;

public interface IFolderCreationService {
    Folder createFolder(FolderRequest folderRequest);

}
