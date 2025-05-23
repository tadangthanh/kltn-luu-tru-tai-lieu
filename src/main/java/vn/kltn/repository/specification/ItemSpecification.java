package vn.kltn.repository.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import vn.kltn.common.ItemType;
import vn.kltn.entity.Item;
import vn.kltn.entity.Permission;

public class ItemSpecification {
    public static Specification<Item> hasNameLike(String keyword) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
    }



    public static Specification<Item> ownedBy(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), userId);
    }

    public static Specification<Item> hasPermissionForUser(Long userId) {
        return (root, query, cb) -> {
            Join<Item, Permission> permissionJoin = root.join("permissions", JoinType.LEFT);
            return cb.equal(permissionJoin.get("recipient").get("id"), userId);
        };
    }
    public  static Specification<Item> hasPermissionItemNotHiddenForUser(Long userId) {
        return (root, query, cb) -> {
            Join<Item, Permission> permissionJoin = root.join("permissions", JoinType.LEFT);
            return cb.and(
                    cb.equal(permissionJoin.get("recipient").get("id"), userId),
                    cb.equal(permissionJoin.get("isHidden"), false)
            );
        };
    }
    public static Specification<Item> notHiddenShared() {
        return (root, query, cb) -> {
            Join<Item, Permission> permissionJoin = root.join("permissions", JoinType.LEFT);
            return cb.equal(permissionJoin.get("isHidden"), false);
        };
    }

    public static Specification<Item> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }


    public static Specification<Item> markDeleted() {
        return (root, query, cb) -> cb.isNotNull(root.get("deletedAt"));
    }

    public static Specification<Item> ofType(ItemType type) {
        return (root, query, cb) -> cb.equal(root.get("itemType"), type);
    }

    public static Specification<Item> hasParentId() {
        return (root, query, cb) -> cb.isNotNull(root.get("parent"));
    }

    public static Specification<Item> nullParent() {
        return (root, query, cb) -> cb.isNull(root.get("parent"));
    }
    public static Specification<Item> parentNotMarkDeleted() {
        return (root, query, cb) -> {
            Join<Item, Item> parentJoin = root.join("parent", JoinType.LEFT);
            return cb.isNull(parentJoin.get("deletedAt"));
        };
    }

}
