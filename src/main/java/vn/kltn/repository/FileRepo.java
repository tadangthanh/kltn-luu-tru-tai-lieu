package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.File;

import java.util.Set;

public interface FileRepo extends JpaRepository<File,Long>, JpaSpecificationExecutor<File> {
    Set<File> findAllByRepoId(Long repoId);
    @Modifying
    @Transactional
    @Query("delete from File f where f.repo.id = ?1")
    void deleteAllByRepoId(Long repoId);


}
