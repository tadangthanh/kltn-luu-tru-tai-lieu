package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.Document;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepo extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    @Modifying
    @Transactional
    @Query("update Document d set d.deletedAt=?2, d.permanentDeleteAt=?3  where d.parent.id in ?1")
    void setDeleteDocument(List<Long> parentIds, LocalDateTime deletedAt, LocalDateTime permanentDeletedAt);

    @Modifying
    @Transactional
    @Query("delete from Document d where d.parent.id in ?1 ")
    void deleteDocumentByListParentId(List<Long> parentIds);


    @Query("select d.blobName from Document d where d.parent.id in ?1")
    List<String> getBlobNameDocumentsByParentIds(List<Long> parentIds);
}
