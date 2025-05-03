package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.SavedItem;
@Repository
public interface SavedItemRepo extends JpaRepository<SavedItem,Long> {
}
