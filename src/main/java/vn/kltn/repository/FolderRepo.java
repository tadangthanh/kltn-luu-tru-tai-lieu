package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.Folder;

@Repository
public interface FolderRepo extends JpaRepository<Folder, Long>, JpaSpecificationExecutor<Folder> {
}
