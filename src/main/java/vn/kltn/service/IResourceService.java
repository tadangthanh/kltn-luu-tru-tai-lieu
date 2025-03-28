package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResourceResponse;
import vn.kltn.entity.Resource;

import java.util.List;

// T là kiểu dữ liệu của entity resource(document or folder), R là kiểu dữ liệu của response (DocumentResponse or FolderResponse)
public interface IResourceService<T extends Resource, R extends ResourceResponse> {
    void validateResourceNotDeleted(Resource resource);

    void validateResourceDeleted(Resource resource);

    void validateCurrentUserIsOwnerResource(T resource);

    void deleteResourceById(Long resourceId);

    R restoreResourceById(Long resourceId);

    R getResourceById(Long resourceId);

    T getResourceByIdOrThrow(Long resourceId);

    void hardDeleteResourceById(Long resourceId);

    R moveResourceToFolder(Long resourceId, Long folderId);

    PageResponse<List<R>> searchByCurrentUser(Pageable pageable, String[] resources);
}
