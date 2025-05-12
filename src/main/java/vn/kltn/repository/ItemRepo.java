package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.Item;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemRepo extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    @Query("""
                select distinct i.owner.email
                from Item i
                inner join Permission p on p.item.id = i.id
                where p.recipient.id = ?1
                and i.deletedAt is null
            """)
    Page<String> findOwnerEmailsSharedItemForMe(Long userId, Pageable pageable);

    @Query("""
                select distinct i.owner.email
                from Item i
                inner join Permission p on p.item.id = i.id
                where p.recipient.id = ?1
                and i.deletedAt is null
                and lower(i.owner.email) like lower(concat('%', ?2, '%'))
            """)
    Page<String> findOwnerEmailsSharedItemForMeFiltered(Long userId, String keyword, Pageable pageable);


    @Query("select i from Item i inner join SavedItem si on i.id = si.item.id where si.user.id = ?1 and i.deletedAt is null")
    Page<Item> getPageItemSaved(Long userId, Pageable pageable);


    @Query("select count(i) from Item i where i.owner.id = ?1 and i.deletedAt is null and i.itemType = 'DOCUMENT'")
    int countByOwnerIdAndDeletedAtIsNullAndTypeDocument(Long ownerId);

    @Query(value = """
                WITH RECURSIVE item_tree AS (
                                        -- Bắt đầu từ parent
                                        SELECT id FROM item WHERE id = :parentId
                                        UNION ALL
                                        SELECT i.id
                                        FROM item i
                                        INNER JOIN item_tree it ON i.parent_id = it.id
                                    )
                                    -- Lấy tất cả ID bao gồm cả parent
                                    SELECT id FROM item_tree;
            """, nativeQuery = true)
    List<Long> findAllItemIdsRecursively(@Param("parentId") Long parentId);

    @Modifying
    @Query("UPDATE Item i SET i.deletedAt = :deletedAt, i.permanentDeleteAt = :permanentDeleteAt WHERE i.id IN :itemIds")
    void updateDeletedAtAndPermanentDeleteAt(@Param("itemIds") List<Long> itemIds,
                                             @Param("deletedAt") LocalDateTime deletedAt,
                                             @Param("permanentDeleteAt") LocalDateTime permanentDeleteAt);
}
