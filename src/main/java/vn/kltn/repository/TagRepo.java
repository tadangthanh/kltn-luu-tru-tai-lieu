package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.kltn.entity.Tag;

public interface TagRepo extends JpaRepository<Tag,Long> {
}
