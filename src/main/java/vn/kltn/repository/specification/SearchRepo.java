package vn.kltn.repository.specification;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.kltn.dto.response.DocumentResponse;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.entity.Document;
import vn.kltn.entity.DocumentAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
@Slf4j(topic = "SEARCH_REPO")
public class SearchRepo {
    @PersistenceContext
    private EntityManager entityManager;

    public PageResponse<List<DocumentResponse>> searchDocumentByCriteriaWithJoin(Pageable pageable, String[] documents, String[] accesses) {
        log.info("searchDocumentByCriteriaWithJoin");
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DocumentAccess> query = builder.createQuery(DocumentAccess.class);
        Root<DocumentAccess> accessRoot = query.from(DocumentAccess.class);
        Join<Document, DocumentAccess> documentRoot = accessRoot.join("resource");
        List<Predicate> accessPreList = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(\\p{Punct}?)");
        for (String u : accesses) {
            Matcher matcher = pattern.matcher(u);
            if (matcher.find()) {
                SpecSearchCriteria searchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                accessPreList.add(toPredicate(accessRoot, builder, searchCriteria));
            }
        }
        List<Predicate> documentPreList = new ArrayList<>();
        for (String a : documents) {
            Matcher matcher = pattern.matcher(a);
            if (matcher.find()) {
                SpecSearchCriteria searchCriteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                documentPreList.add(toPredicate(documentRoot, builder, searchCriteria));
            }
        }

        Predicate accessPre = builder.or(accessPreList.toArray(new Predicate[0]));
        Predicate documentPre = builder.or(documentPreList.toArray(new Predicate[0]));
        Predicate finalPre = builder.and(accessPre, documentPre);
        query.where(finalPre);

        List<DocumentAccess> documentAccesses = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
        List<DocumentResponse> documentResponses = new ArrayList<>();
        for (DocumentAccess documentAccess : documentAccesses) {
            DocumentResponse documentResponse = new DocumentResponse();
            documentResponse.setId(documentAccess.getResource().getId());
            documentResponse.setName(documentAccess.getResource().getName());
            documentResponse.setDescription(documentAccess.getResource().getDescription());
            documentResponse.setCreatedAt(documentAccess.getResource().getCreatedAt());
            documentResponse.setUpdatedAt(documentAccess.getResource().getUpdatedAt());
            documentResponses.add(documentResponse);
        }
        long total = entityManager.createQuery(builder.createQuery(Long.class).select(builder.count(query.from(DocumentAccess.class)))).getSingleResult();
        return PageResponse.<List<DocumentResponse>>builder()
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalItems(total)
                .items(documentResponses)
                .build();
    }

    private Predicate toPredicate(Root<DocumentAccess> root, CriteriaBuilder builder, SpecSearchCriteria criteria) {
        log.info("-------------- toDocumentAccessPredicate --------------");
        return switch (criteria.getOperation()) {
            case EQUALITY -> builder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN -> builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case STARTS_WITH -> builder.like(root.get(criteria.getKey()), criteria.getValue() + "%");
            case ENDS_WITH -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue());
            case CONTAINS -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        };
    }

    private Predicate toPredicate(Join<Document, DocumentAccess> root, CriteriaBuilder builder, SpecSearchCriteria criteria) {
        log.info("-------------- toDocumentAccessPredicate --------------");
        return switch (criteria.getOperation()) {
            case EQUALITY -> builder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN -> builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case STARTS_WITH -> builder.like(root.get(criteria.getKey()), criteria.getValue() + "%");
            case ENDS_WITH -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue());
            case CONTAINS -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        };
    }
}
