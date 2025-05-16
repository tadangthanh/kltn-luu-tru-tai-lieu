package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.SharedLink;

import java.util.Optional;

@Repository
public interface SharedLinkRepo extends JpaRepository<SharedLink, Long> {
    Optional<SharedLink> findByAccessToken(String accessToken);


    @Query("SELECT sl FROM SharedLink sl WHERE sl.item.id = :itemId AND sl.sharedBy.id = :sharedById")
    Page<SharedLink> findAllBySharedByItemId(Long itemId,Long sharedById, Pageable pageable);
}
