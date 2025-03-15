package vn.kltn.service;

import vn.kltn.dto.request.TagRequest;
import vn.kltn.entity.Tag;

public interface ITagService {
    Tag getByNameOrNull(String name);

    Tag requestToEntity(TagRequest tagRequest);

    Tag saveTag(Tag tag);
}
