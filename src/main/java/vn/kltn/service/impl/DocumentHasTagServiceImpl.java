package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.TagRequest;
import vn.kltn.entity.*;
import vn.kltn.repository.DocumentHasTagRepo;
import vn.kltn.service.IDocumentHasTagService;
import vn.kltn.service.ITagService;

import java.util.Set;

@Service
@Transactional
@Slf4j(topic = "DOCUMENT_HAS_TAG_SERVICE")
@RequiredArgsConstructor
public class DocumentHasTagServiceImpl implements IDocumentHasTagService {
    private final DocumentHasTagRepo documentHasTagRepo;
    private final ITagService tagService;

    @Override
    public void addDocumentToTag(Document document, TagRequest[] tags) {
        for (TagRequest tagRequest : tags) {
            Tag tagExist = tagService.getByNameOrNull(tagRequest.getName());
            if (tagExist != null) {
                saveDocumentTag(document, tagExist);
            } else {
                tagExist = tagService.requestToEntity(tagRequest);
                tagExist = tagService.saveTag(tagExist);
                saveDocumentTag(document, tagExist);
            }
        }
    }

    @Override
    public void addDocumentToTag(Document document, Set<Tag> tags) {
        for (Tag tag : tags) {
            saveDocumentTag(document, tag);
        }
    }

    private void saveDocumentTag(Document document, Tag tag) {
        DocumentHasTag documentHasTag = new DocumentHasTag();
        documentHasTag.setDocument(document);
        documentHasTag.setTag(tag);
        documentHasTagRepo.save(documentHasTag);
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        documentHasTagRepo.deleteByDocumentId(documentId);
    }

    @Override
    public Set<Tag> getTagsByDocumentId(Long id) {
        log.info("getTagsByDocumentId: " + id);
        return tagService.getTagsByDocumentId(id);
    }
}
