package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.DocumentAccess;
@Repository
public interface DocumentAccessRepo extends JpaRepository<DocumentAccess, Long> {
}
