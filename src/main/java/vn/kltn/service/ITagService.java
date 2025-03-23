package vn.kltn.service;

import vn.kltn.dto.request.TagRequest;
import vn.kltn.entity.Tag;

import java.util.Set;

public interface ITagService {
    Tag getByNameOrNull(String name);

    Tag requestToEntity(TagRequest tagRequest);

    Set<Tag> getTagsByDocumentId(Long documentId);
    Tag saveTag(Tag tag);
}
