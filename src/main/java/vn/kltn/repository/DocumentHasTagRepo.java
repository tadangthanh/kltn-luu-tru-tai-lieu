package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.DocumentHasTag;

import java.util.List;

@Repository
public interface DocumentHasTagRepo extends JpaRepository<DocumentHasTag, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM DocumentHasTag dht WHERE dht.document.id = ?1")
    void deleteByDocumentId(Long documentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DocumentHasTag dht WHERE dht.document.id IN ?1")
    void deleteAllByDocumentIds(List<Long> documentIds);

    @Modifying
    @Transactional
    @Query("delete from DocumentHasTag dht where dht.document.parent.id in ?1 ")
    void deleteTagDocumentByListParentId(List<Long> parentIds);

    @Query("select case when count(dht) > 0 then true else false end from DocumentHasTag dht where dht.document.id = ?1 and dht.tag.id = ?2")
    boolean existsByDocumentIdAndTagId(Long documentId, Long tagId);

    void deleteAllByDocumentId(Long id);
}
