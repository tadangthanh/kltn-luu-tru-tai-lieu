package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.PreviewImage;

@Repository
public interface PreviewImageRepo extends JpaRepository<PreviewImage, Long> {
}
