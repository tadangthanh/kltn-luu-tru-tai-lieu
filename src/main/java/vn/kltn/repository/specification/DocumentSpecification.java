package vn.kltn.repository.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import vn.kltn.entity.Document;

public class DocumentSpecification {
//    public static Specification<Document> hasAccessByRecipient(Long recipientId) {
//        return (root, query, criteriaBuilder) -> {
//            // JOIN với bảng DocumentAccess
//            Join<Document, DocumentAccess> documentAccessJoin = root.join("documentAccessList", JoinType.INNER);
//
//            // Điều kiện recipient_id
//            return criteriaBuilder.equal(documentAccessJoin.get("recipient").get("id"), recipientId);
//        };
//    }
}
