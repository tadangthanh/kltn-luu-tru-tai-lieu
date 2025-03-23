package vn.kltn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.Document;

@Repository
public interface DocumentRepo extends JpaRepository<Document, Long> , JpaSpecificationExecutor<Document> {
    @Query("select d from Document d where d.owner.email=?1")
    Page<Document> findDocumentByCreatedBy(String createdEmail, Pageable pageable);
}
