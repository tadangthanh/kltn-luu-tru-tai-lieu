package vn.kltn.repository.elasticsearch.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import vn.kltn.exception.CustomIOException;
import vn.kltn.index.DocumentSegmentEntity;
import vn.kltn.repository.elasticsearch.CustomDocumentSegmentRepo;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Slf4j(topic = "CUSTOM_DOCUMENT_SEGMENT_REPO")
public class CustomDocumentSegmentRepoImpl implements CustomDocumentSegmentRepo {
    private final ElasticsearchClient elasticsearchClient;

    @Override
    public void markDeletedByDocumentId(Long documentId, boolean value) {
        log.info("mark deleted documentId: {}", documentId);
        try {
            elasticsearchClient.updateByQuery(UpdateByQueryRequest.of(b -> b
                    .index("document_segments")
                    .query(q -> q
                            .term(t -> t
                                    .field("documentId")
                                    .value(documentId)
                            )
                    ).script(s -> s
                            .source("ctx._source.isDeleted = " + value)
                            .lang("painless")
                    )
            ));
        } catch (IOException e) {
            log.error("Error marking documents as deleted: {}", e.getMessage(), e);
            throw new CustomIOException("Failed to mark documents as deleted");
        }
    }

    @Override
    public void updateDocument(DocumentSegmentEntity documentUpdated) {
        try {
            elasticsearchClient.updateByQuery(UpdateByQueryRequest.of(b -> b
                    .index("document_segments")
                    .query(q -> q
                            .term(t -> t
                                    .field("documentId")
                                    .value(documentUpdated.getDocumentId())  // cast đúng kiểu String là keyword
                            )
                    )
                    .script(s -> s
                            .source("ctx._source.name = params.name; " +
                                    "ctx._source.description = params.description; " +
                                    "ctx._source.updatedAt = params.updatedAt;" +
                                    " ctx._source.updatedBy = params.updatedBy;") // add các field cần update
                            .lang("painless")
                            .params(documentUpdated.toParamMap())
                    )
            ));
        } catch (IOException e) {
            log.error("Error updating document: {}", e.getMessage(), e);
            throw new CustomIOException("Failed to update document");
        }
    }

    @Override
    public void markDeletedByDocumentIds(List<Long> documentIds, boolean value) {
        log.info("Marking deleted documentIds: {},value {}", documentIds, value);
        try {
            elasticsearchClient.updateByQuery(UpdateByQueryRequest.of(b -> b
                    .index("document_segments")
                    .query(q -> q
                            .terms(t -> t
                                    .field("documentId")
                                    .terms(v -> v
                                            .value(documentIds.stream()
                                                    .map(FieldValue::of)
                                                    .toList())
                                    )
                            )
                    )
                    .script(s -> s
                            .source("ctx._source.isDeleted = " + value)
                            .lang("painless")
                    )
            ));
        } catch (IOException e) {
            log.error("Error marking documents as deleted: {}", e.getMessage(), e);
            throw new CustomIOException("Failed to mark documents as deleted");
        }
    }

    @Override
    public List<DocumentSegmentEntity> getDocumentByMe(Set<Long> listDocumentSharedWith, String query, int page, int size) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            SearchResponse<DocumentSegmentEntity> response = elasticsearchClient.search(s -> s
                            .index("document_segments")
                            .from(page * size)
                            .size(size)
                            .query(q -> q
                                    .bool(b -> b
                                            .must(m -> m
                                                    .matchPhrase(match -> match
                                                            .field("content")
                                                            .query(query)
                                                    )
                                            )
                                            .filter(f -> f
                                                    .term(t -> t
                                                            .field("isDeleted")
                                                            .value(false)
                                                    )
                                            )
                                            .should(sh1 -> sh1
                                                    .term(t -> t
                                                            .field("createdBy")
                                                            .value(currentEmail)
                                                    )
                                            )
                                            .should(sh2 -> sh2
                                                    .terms(t -> t
                                                            .field("sharedWith")
                                                            .terms(tq -> tq
                                                                    .value(listDocumentSharedWith.stream()
                                                                            .map(String::valueOf)
                                                                            .map(FieldValue::of)
                                                                            .toList())
                                                            )
                                                    )
                                            )
                                            .minimumShouldMatch("1") // ít nhất 1 điều kiện should phải đúng
                                    )
                            ),
                    DocumentSegmentEntity.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (IOException e) {
            log.error("Error searching document segments: {}", e.getMessage(), e);
            throw new CustomIOException("Failed to search documents");
        }
    }


}
