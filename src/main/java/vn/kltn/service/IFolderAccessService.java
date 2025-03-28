package vn.kltn.service;

import vn.kltn.dto.response.AccessResourceResponse;
import vn.kltn.entity.Folder;
import vn.kltn.entity.FolderAccess;

public interface IFolderAccessService extends IAccessService<FolderAccess, AccessResourceResponse> {
    void inheritAccess(Folder newFolder);
}
