package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.FolderAccess;

import java.util.Set;

@Repository
public interface FolderAccessRepo extends JpaRepository<FolderAccess, Long>, JpaSpecificationExecutor<FolderAccess> {

    @Query("SELECT fa FROM FolderAccess fa WHERE fa.resource.id = :resourceId")
    Set<FolderAccess> findAllByResourceId(Long resourceId);
}
