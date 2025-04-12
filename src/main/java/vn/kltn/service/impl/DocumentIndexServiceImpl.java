package vn.kltn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.kltn.entity.Document;
import vn.kltn.entity.Tag;
import vn.kltn.index.DocumentIndex;
import vn.kltn.map.DocumentIndexMapper;
import vn.kltn.repository.elasticsearch.CustomDocumentIndexRepo;
import vn.kltn.repository.elasticsearch.DocumentIndexRepo;
import vn.kltn.repository.util.FileUtil;
import vn.kltn.service.IDocumentHasTagService;
import vn.kltn.service.IDocumentIndexService;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DOCUMENT_INDEX_SERVICE")
public class DocumentIndexServiceImpl implements IDocumentIndexService {
    private final DocumentIndexRepo documentIndexRepo;
    private final DocumentIndexMapper documentIndexMapper;
    private final IDocumentHasTagService documentHasTagService;
    private final DocumentPermissionCommonService documentPermissionCommonService;
    private final CustomDocumentIndexRepo customDocumentIndexRepo;

    @Override
    @Async
    public void insertDoc(Document document, InputStream inputStream) {
        log.info("insert document Id: {}", document.getId());
        String content = FileUtil.extractTextByType(document.getType(), inputStream);
        DocumentIndex documentIndex = mapDocumentIndex(document);
        documentIndex.setContent(content);
        documentIndexRepo.save(documentIndex);
    }

    @Override
    @Async
    public void deleteIndex(String indexId) {
        log.info("delete index Id: {}", indexId);
        documentIndexRepo.deleteById(indexId);
    }

    /**
     * đánh dấu field isDeleted = value
     *
     * @param indexId: id của document trong elasticsearch
     * @param value    : true/false
     */
    @Override
    @Async
    public void markDeleteDocument(String indexId, boolean value) {
        log.info("mark deleted indexId: {}", indexId);
        customDocumentIndexRepo.markDeletedByIndexId(indexId, value);
    }

    @Override
    public List<DocumentIndex> getDocumentByMe(Set<Long> listDocumentSharedWith, String query, int page, int size) {
        return customDocumentIndexRepo.getDocumentByMe(listDocumentSharedWith, query, page, size);
    }

    @Override
    @Async
    public void updateDocument(Document document) {
        log.info("update documentId: {}", document.getId());
        customDocumentIndexRepo.updateDocument(mapDocumentIndex(document));
    }

    @Override
    public void deleteIndexByIdList(List<Long> indexIds) {
        customDocumentIndexRepo.deleteIndexByIdList(indexIds);
    }

    @Override
    public void markDeleteDocumentsIndex(List<String> indexIds, boolean value) {
        customDocumentIndexRepo.markDeleteDocumentsIndex(indexIds, value);
    }

    private List<String> getTagsByDocumentId(Long documentId) {
        return documentHasTagService.getTagsByDocumentId(documentId).stream().map(Tag::getName).toList();
    }

    private DocumentIndex mapDocumentIndex(Document document) {
        DocumentIndex documentIndex = documentIndexMapper.toIndex(document);
        List<String> tagsList = getTagsByDocumentId(document.getId());
        List<Long> sharedWith = getUserIdsByDocumentShared(document.getId());
        documentIndex.setTags(tagsList);
        documentIndex.setSharedWith(sharedWith);
        return documentIndex;
    }

    private List<Long> getUserIdsByDocumentShared(Long documentId) {
        return documentPermissionCommonService.getUserIdsByDocumentShared(documentId).stream().toList();
    }

}
