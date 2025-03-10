package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import vn.kltn.entity.File;

import java.util.List;

public interface FileRepo extends JpaRepository<File, Long>, JpaSpecificationExecutor<File> {
    @Query("select f from File f join f.tags t where lower(t.tag.name) = lower(?1) ")
    List<File> findByTagName(String tagName);

}
