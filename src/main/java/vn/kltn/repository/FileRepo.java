package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.kltn.entity.File;

public interface FileRepo extends JpaRepository<File,Long> {
}
