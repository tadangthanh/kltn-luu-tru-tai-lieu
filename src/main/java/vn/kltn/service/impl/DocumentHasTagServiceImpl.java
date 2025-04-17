package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.TagRequest;
import vn.kltn.entity.Document;
import vn.kltn.entity.DocumentHasTag;
import vn.kltn.entity.Tag;
import vn.kltn.repository.DocumentHasTagRepo;
import vn.kltn.service.IDocumentHasTagService;
import vn.kltn.service.ITagService;

import java.util.List;
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
            if (documentHasTagRepo.existsByDocumentIdAndTagId(document.getId(), tag.getId())) {
                continue;
            }
            saveDocumentTag(document, tag);
        }
    }

    @Override
    public void updateTagDocument(Document document, TagRequest[] tags) {
        log.info("update tag document {}", document.getId());
        documentHasTagRepo.deleteAllByDocumentId(document.getId());
        addDocumentToTag(document, tags);
    }

    @Override
    public void deleteAllByDocumentIds(List<Long> documentIds) {
        log.info("delete tags by document ids: " + documentIds);
        if(documentIds==null || documentIds.isEmpty()){
            return;
        }
        documentHasTagRepo.deleteAllByDocumentIds(documentIds);
    }

    @Override
    public void deleteAllByFolderIds(List<Long> folderIds) {
        log.warn("deleteAllByFolderIds: " + folderIds);
        documentHasTagRepo.deleteTagDocumentByListParentId(folderIds);
    }

    private void saveDocumentTag(Document document, Tag tag) {
        log.info("saveDocumentTag: " + document.getId() + " - " + tag.getId());
        DocumentHasTag documentHasTag = new DocumentHasTag();
        documentHasTag.setDocument(document);
        documentHasTag.setTag(tag);
        documentHasTagRepo.save(documentHasTag);
    }

    @Override
    public void deleteAllByDocumentId(Long documentId) {
        documentHasTagRepo.deleteByDocumentId(documentId);
    }

    @Override
    public Set<Tag> getTagsByDocumentId(Long id) {
        log.info("getTagsByDocumentId: " + id);
        return tagService.getTagsByDocumentId(id);
    }

}
