package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.FolderAccess;

import java.util.Set;

@Repository
public interface FolderAccessRepo extends JpaRepository<FolderAccess, Long>, JpaSpecificationExecutor<FolderAccess> {

    @Query("SELECT fa FROM FolderAccess fa WHERE fa.resource.id = :resourceId")
    Set<FolderAccess> findAllByResourceId(Long resourceId);

    @Modifying
    @Transactional
    @Query("DELETE FROM FolderAccess fa WHERE fa.resource.id = :resourceId")
    void deleteAllByResourceId(Long resourceId);
}
