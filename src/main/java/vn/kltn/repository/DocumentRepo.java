package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.Document;

import java.util.List;

@Repository
public interface DocumentRepo extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    @Query("select d from Document d where d.owner.email=?1")
    Page<Document> findDocumentByCreatedBy(String createdEmail, Pageable pageable);

    @Modifying
    @Transactional
    @Query("update Document d set d.deletedAt=now() where d.id=?1")
    void setDeletedDocumentByFolderId(Long folderId);
    @Modifying
    @Transactional
    @Query("update Document d set d.deletedAt=now() where d.folder.id in ?1")
    void setDeletedDocumentByListFolderId(List<Long> folderIds);
}
