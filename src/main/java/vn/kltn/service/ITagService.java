package vn.kltn.service;

import vn.kltn.dto.request.TagRequest;
import vn.kltn.dto.response.TagResponse;

public interface ITagService {
    TagResponse createTag(TagRequest tagRequest);
}
