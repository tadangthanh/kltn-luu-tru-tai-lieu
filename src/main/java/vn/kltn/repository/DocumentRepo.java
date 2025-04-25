package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = """
            select d.id from document d
                            inner join file_system_entity fse on d.id=fse.id
                                    where fse.parent_id in (:parentResourceIds)
                        and d.deleted_at is null
                        and not exists(select 1 from permission p
                                        where p.recipient_id = :recipientId
                                        and p.resource_id = fse.id)
            """, nativeQuery = true)
    List<Long> findDocumentChildIdsEmptyPermission(@Param("parentResourceIds") List<Long> parentResourceIds, @Param("recipientId") Long recipientId);

    @Query("select d from Document d where d.parent.id in ?1")
    List<Document> findDocumentsByParentIds(List<Long> folderIds);

    @Modifying
    @Query("select d.id from Document d where d.parent.id in ?1")
    List<Long> findDocumentIdsWithParentIds(List<Long> folderIds);

    @Query("select d from Document d where d.name in ?1")
    List<Document> findAllByListName(List<String> listFileName);
    @Query(value = """
            select d from document d
                            inner join file_system_entity fse on d.id=fse.id
                                    where fse.parent_id in (:parentResourceIds)
                        and d.deleted_at is null
                        and not exists(select 1 from permission p
                                        where p.recipient_id = :recipientId
                                        and p.resource_id = fse.id)
            """, nativeQuery = true)
    List<Document> findDocumentChildEmptyPermission(@Param("parentResourceIds") List<Long> parentResourceIds, @Param("recipientId") Long recipientId);

    @Query("select d from Document d where d.id in (select p.item.id from Permission p where p.recipient.id = ?1)")
    List<Document> findAllDocumentByResourceAndRecipient(Long resourceId, Long recipientId);

}
