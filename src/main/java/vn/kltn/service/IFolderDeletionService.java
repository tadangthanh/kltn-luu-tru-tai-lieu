package vn.kltn.service;

import vn.kltn.entity.Folder;

public interface IFolderDeletionService {
    void softDelete(Folder folder);
    void hardDelete(Folder folder);
}
