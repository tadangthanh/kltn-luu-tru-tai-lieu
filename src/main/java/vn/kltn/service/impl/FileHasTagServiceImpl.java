package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.TagRequest;
import vn.kltn.entity.File;
import vn.kltn.entity.FileHasTag;
import vn.kltn.entity.Tag;
import vn.kltn.repository.FileHasTagRepo;
import vn.kltn.service.IFileHasTagService;
import vn.kltn.service.ITagService;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "FILE_HAS_TAG_SERVICE")
public class FileHasTagServiceImpl implements IFileHasTagService {
    private final FileHasTagRepo fileHasTagRepo;
    private final ITagService tagService;

    @Override
    public void addFileToTag(File fileEntity, TagRequest[] tags) {
        for (TagRequest tagRequest : tags) {
            Tag tagExist= tagService.getByNameOrNull(tagRequest.getName());
            if (tagExist != null) {
                saveFileHasTag(fileEntity, tagExist);
            }
            tagExist= tagService.requestToEntity(tagRequest);
            tagExist = tagService.saveTag(tagExist);
            saveFileHasTag(fileEntity, tagExist);
        }
    }
    private void saveFileHasTag(File file, Tag tag) {
        FileHasTag fileHasTag = new FileHasTag();
        fileHasTag.setFile(file);
        fileHasTag.setTag(tag);
        fileHasTagRepo.save(fileHasTag);
    }

}
