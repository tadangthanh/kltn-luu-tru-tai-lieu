package vn.kltn.service;

import vn.kltn.dto.request.TagRequest;
import vn.kltn.entity.File;

public interface IFileHasTagService {
    void addFileToTag(File fileEntity, TagRequest[] tags);
    void deleteByFileId(Long fileId);
}
