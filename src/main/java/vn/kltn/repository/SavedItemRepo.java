package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.SavedItem;

@Repository
public interface SavedItemRepo extends JpaRepository<SavedItem, Long> {

    @Query("select si from SavedItem si where si.user.id = ?1")
    Page<SavedItem> getPageItemSaved(Long userId,Pageable pageable);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    void deleteByItemIdAndUserId(Long itemId, Long userId);
}
