package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.Item;

@Repository
public interface ItemRepo extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    @Query("select distinct  i.owner.email from Item i inner join Permission p on p.item.id=i.id where p.recipient.id =?1")
    Page<String> findOwnerEmailsSharedItemForMe(Long userId, Pageable pageable);
}
