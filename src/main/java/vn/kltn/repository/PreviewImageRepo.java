package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.PreviewImage;

import java.util.List;

@Repository
public interface PreviewImageRepo extends JpaRepository<PreviewImage, Long> {
    @Query("SELECT pi.pageNumber FROM PreviewImage pi WHERE pi.document.id = :documentId")
    List<Integer> findAllPageNumbersByDocumentId(Long documentId);
}
