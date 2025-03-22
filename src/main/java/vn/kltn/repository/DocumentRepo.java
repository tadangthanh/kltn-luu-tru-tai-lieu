package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.Document;

@Repository
public interface DocumentRepo extends JpaRepository<Document, Long> {
}
