package vn.kltn.repository.elasticsearch.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.UserIndexResponse;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.index.UserIndex;
import vn.kltn.repository.elasticsearch.CustomUserIndexRepo;
import vn.kltn.util.PaginationIndexUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j(topic = "CUSTOM_USER_INDEX_REPO")
public class CustomUserIndexRepoImpl implements CustomUserIndexRepo {
    private final ElasticsearchClient elasticsearchClient;


    @Override
    public PageResponse<List<UserIndexResponse>> search(String query, Pageable pageable) {
        String currentEmail= SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            SearchResponse<UserIndex> response = elasticsearchClient.search(s -> s
                            .index("users_index")
                            .query(q -> q
                                    .bool(b -> b
                                            .must(m -> m
                                                    .multiMatch(mm -> mm
                                                            .query(query)
                                                            .fields("email^3", "fullName^2")
                                                    )
                                            )
                                            .filter(f -> f
                                                    .term(t -> t
                                                            .field("status.keyword")
                                                            .value("ACTIVE")
                                                    )
                                            )
                                            .mustNot(mn -> mn
                                                    .term(t -> t
                                                            .field("email.keyword")
                                                            .value(currentEmail)  // Điều kiện email khác currentEmail
                                                    )
                                            )
                                    )
                            )
                            .highlight(h -> h
                                    .preTags("<mark>")
                                    .postTags("</mark>")
                                    .fields("email", f -> f.fragmentSize(150).numberOfFragments(3))
                                    .fields("fullName", f -> f)
                            )
                            .from(pageable.getPageNumber() * pageable.getPageSize())
                            .size(pageable.getPageSize()),
                    UserIndex.class);

            long totalItems = response.hits().total().value();
            int totalPage = (int) Math.ceil((double) totalItems / pageable.getPageSize());

            List<UserIndexResponse> userIndices = response.hits().hits().stream()
                    .filter(hit -> hit.source() != null)
                    .map(hit -> {
                        UserIndexResponse dto = new UserIndexResponse();
                        dto.setUser(hit.source());
                        dto.setHighlights(hit.highlight());
                        return dto;
                    })
                    .toList();

            boolean hasNext = pageable.getPageNumber() + 1 < totalPage;

            return PaginationIndexUtils.convertToPageResponse(userIndices, totalItems, totalPage, pageable, hasNext);

        } catch (Exception e) {
            log.error("Error searching user index: {}", e.getMessage(), e);
            throw new ResourceNotFoundException("Error searching user index");
        }
    }
}


