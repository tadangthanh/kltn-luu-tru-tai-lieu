package vn.kltn.repository.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import vn.kltn.entity.Folder;
import vn.kltn.entity.FolderAccess;

public class FolderSpecification {
    public static Specification<Folder> hasAccessByRecipient(Long recipientId) {
        return (root, query, criteriaBuilder) -> {
            // JOIN với bảng DocumentAccess
            Join<Folder, FolderAccess> folderFolderAccessJoin = root.join("folderAccessList", JoinType.INNER);

            // Điều kiện recipient_id
            return criteriaBuilder.equal(folderFolderAccessJoin.get("recipient").get("id"), recipientId);
        };
    }
}
