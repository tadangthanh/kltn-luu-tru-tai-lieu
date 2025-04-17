package vn.kltn.service;

import vn.kltn.entity.Folder;

public interface IFolderRestorationService {
    Folder restore(Long folderId);
}
