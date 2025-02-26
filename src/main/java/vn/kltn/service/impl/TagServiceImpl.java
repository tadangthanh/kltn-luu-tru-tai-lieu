package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.TagRequest;
import vn.kltn.dto.response.TagResponse;
import vn.kltn.entity.Tag;
import vn.kltn.map.TagMapper;
import vn.kltn.repository.TagRepo;
import vn.kltn.service.ITagService;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "TAG_SERVICE")
public class TagServiceImpl implements ITagService {
    private final TagRepo tagRepo;
    private final TagMapper tagMapper;

    @Override
    public TagResponse createTag(TagRequest tagRequest) {
        Optional<Tag> tagExist = tagRepo.findByName(tagRequest.getName());
        if (tagExist.isPresent()) {
            return tagMapper.entityToResponse(tagExist.get());
        }
        Tag tag = tagMapper.requestToEntity(tagRequest);
        Tag savedTag = tagRepo.save(tag);
        return tagMapper.entityToResponse(savedTag);
    }
}
