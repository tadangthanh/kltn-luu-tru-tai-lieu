package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.response.AccessResourceResponse;
import vn.kltn.dto.response.FolderResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Folder;
import vn.kltn.entity.FolderAccess;

import java.util.List;

public interface IFolderAccessService extends IAccessService<FolderAccess, AccessResourceResponse> {
    void inheritAccess(Folder newFolder);
    PageResponse<List<FolderResponse>> getPageFolderSharedForMe(Pageable pageable, String[] folders);

}
