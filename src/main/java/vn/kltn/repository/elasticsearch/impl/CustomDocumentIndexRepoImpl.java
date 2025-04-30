package vn.kltn.repository.elasticsearch.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import vn.kltn.dto.response.DocumentIndexResponse;
import vn.kltn.exception.CustomIOException;
import vn.kltn.index.DocumentIndex;
import vn.kltn.repository.elasticsearch.CustomDocumentIndexRepo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j(topic = "CUSTOM_DOCUMENT_INDEX_REPO")
public class CustomDocumentIndexRepoImpl implements CustomDocumentIndexRepo {
    private final ElasticsearchClient elasticsearchClient;

    @Override
    public void markDeletedByIndexId(String indexId, boolean value) {
        log.info("Mark deleted for indexId: {}", indexId);
        try {
            elasticsearchClient.update(UpdateRequest.of(u -> u.index("documents_index").id(indexId).doc(Map.of("isDeleted", value))), DocumentIndex.class);
        } catch (ElasticsearchException e) {
            if (e.getMessage() != null && e.getMessage().contains("document_missing_exception")) {
                log.warn("Document with indexId {} not found in Elasticsearch. Skipping update.", indexId);
            } else {
                log.error("Elasticsearch error when marking document as deleted: {}", e.getMessage(), e);
                throw new CustomIOException("Failed to mark document as deleted due to Elasticsearch error");
            }
        } catch (IOException e) {
            log.error("Error marking document as deleted by indexId: {}", e.getMessage(), e);
            throw new CustomIOException("Failed to mark document as deleted");
        }
    }


    // ko update content
    @Override
    public void updateDocument(DocumentIndex documentUpdated) {
        try {
            elasticsearchClient.update(u -> u
                            .index("documents_index")
                            .id(documentUpdated.getId())
                            .doc(documentUpdated), // chỉ định object thay thế
                    DocumentIndex.class);
        } catch (IOException| ElasticsearchException e) {
            log.error("Error updating document: {}", e.getMessage(), e);
            throw new CustomIOException("Failed to update document");
        }
    }

    @Override
    public void deleteIndexByIdList(List<Long> indexIds) {
        try {
            // Xây dựng BulkRequest để xóa nhiều document
            BulkRequest.Builder bulkRequest = new BulkRequest.Builder();

            // Lặp qua từng indexId và tạo delete operation
            for (Long indexId : indexIds) {
                bulkRequest.operations(op -> op.delete(d -> d.index("documents_index")  // Tên index
                        .id(indexId.toString())  // ID của document trong ES
                ));
            }

            // Thực hiện bulk delete
            BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());

            if (response.errors()) {
                log.error("Errors occurred during bulk delete operation");
                throw new CustomIOException("Bulk delete operation failed");
            }

        } catch (IOException e) {
            log.error("Error during bulk delete: {}", e.getMessage(), e);
            throw new CustomIOException("Failed to delete documents from Elasticsearch");
        }
    }

    @Override
    public void markDeleteDocumentsIndex(List<String> indexIds, boolean value) {
        try {
            // Xây dựng UpdateByQuery request
            UpdateByQueryRequest.Builder updateRequestBuilder = new UpdateByQueryRequest.Builder()
                    .index("documents_index")  // Tên của index
                    .query(q -> q
                            .terms(t -> t
                                    .field("id")  // Trường id hoặc có thể là "documentId"
                                    .terms(tq -> tq
                                            .value(indexIds.stream()
                                                    .map(FieldValue::of)
                                                    .collect(Collectors.toList()))  // Duyệt qua indexIds và chuyển thành FieldValue
                                    )
                            )
                    )
                    .script(s -> s
                            .source("ctx._source.isDeleted = params.isDeleted")  // Đánh dấu xóa (soft delete)
                            .lang("painless")
                            .params(Map.of("isDeleted", JsonData.of(value)))  // Sử dụng JsonData.of để chuyển đổi value thành kiểu JsonData
                    );

            // Thực hiện update
            elasticsearchClient.updateByQuery(updateRequestBuilder.build());

            log.info("Successfully marked documents as deleted or restored");

        } catch (IOException e) {
            log.error("Error marking documents as deleted: {}", e.getMessage(), e);
            throw new CustomIOException("Failed to mark documents as deleted");
        }
    }


    @Override
    public List<DocumentIndexResponse> getDocumentShared(Set<Long> documentIds, String query, int page, int size) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            SearchResponse<DocumentIndex> response = elasticsearchClient.search(s ->
                            s.index("documents_index")
                                    .from(page * size)
                                    .size(size)
                                    .query(q -> q
                                            .bool(b -> b
                                                    .must(m -> m
                                                            .multiMatch(mm -> mm
                                                                    .query(query)
                                                                    .type(TextQueryType.Phrase)
                                                                    .fields("name^3",
                                                                            "description^2",
                                                                            "content",
                                                                            "updatedBy",
                                                                            "createdBy")))

                                                    .filter(f -> f
                                                            .term(t -> t
                                                                    .field("isDeleted")
                                                                    .value(false)))

                                                    // Đây mới là phần bạn cần: id ∈ documentIds hoặc createdBy == currentEmail
                                                    .should(sh1 -> sh1.terms(t -> t
                                                            .field("id")
                                                            .terms(tq -> tq.value(documentIds.stream()
                                                                    .map(String::valueOf)
                                                                    .map(FieldValue::of)
                                                                    .toList()))
                                                    ))
                                                    .should(sh2 -> sh2.term(t -> t
                                                            .field("createdBy")
                                                            .value(currentEmail)
                                                    ))
                                                    .minimumShouldMatch("1") // 👈 bắt buộc ít nhất 1 cái đúng
                                            )
                                    )
                                    .highlight(h -> h
                                            .preTags("<mark>")
                                            .postTags("</mark>")
                                            .fields("content",
                                                    f -> f.fragmentSize(150)
                                                            .numberOfFragments(3))
                                            .fields("name", f -> f)
                                            .fields("description", f -> f)
                                            .fields("updatedBy", f -> f)),
                    DocumentIndex.class);

            return response.hits().hits().stream()
                    .filter(hit -> hit.source() != null)
                    .map(hit -> {
                        DocumentIndexResponse dto = new DocumentIndexResponse();
                        dto.setDocument(hit.source());
                        dto.setHighlights(hit.highlight());
                        return dto;
                    })
                    .toList();

        } catch (IOException e) {
            log.error("Error searching documents: {}", e.getMessage(), e);
            throw new CustomIOException("Failed to search documents");
        }
    }


    @Override
    public void bulkUpdate(List<DocumentIndex> indices) {
        try {
            BulkRequest.Builder builder = new BulkRequest.Builder();

            for (DocumentIndex doc : indices) {
                builder.operations(op -> op
                        .update(u -> u
                                .index("documents_index")
                                .id(String.valueOf(doc.getId()))
                                .action(a -> a
                                        .doc(doc.toPartialUpdateMap())
                                        .docAsUpsert(true)
                                )
                        )
                );
            }

            BulkResponse response = elasticsearchClient.bulk(builder.build());

            if (response.errors()) {
                response.items().forEach(item -> {
                    if (item.error() != null) {
                        log.error("Bulk sync failed. Operation: {}, docId: {}, error: {}",
                                item.operationType(), item.id(), item.error().reason());
                    }
                });
                throw new CustomIOException("Elasticsearch bulk sync error");
            }

            log.info("Bulk update successful for {} documents", indices.size());

        } catch (IOException e) {
            log.error("Bulk update failed: {}", e.getMessage(), e);
            throw new CustomIOException("Bulk update failed");
        }
    }


}
