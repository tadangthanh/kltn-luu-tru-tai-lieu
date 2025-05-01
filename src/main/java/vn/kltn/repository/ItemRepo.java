package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.DocumentVersion;
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

    @Modifying
    @Query("DELETE FROM Item i WHERE i.permanentDeleteAt <= :now")
    void deleteExpiredItems(@Param("now") LocalDateTime now);

    List<Item> findAllByPermanentDeleteAtBefore(LocalDateTime time);
}
