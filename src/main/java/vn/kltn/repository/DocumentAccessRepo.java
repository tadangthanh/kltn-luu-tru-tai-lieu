package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.DocumentAccess;

import java.util.Set;

@Repository
public interface DocumentAccessRepo extends JpaRepository<DocumentAccess, Long>, JpaSpecificationExecutor<DocumentAccess> {

    @Query("SELECT da FROM DocumentAccess da WHERE da.resource.id = :resourceId")
    Set<DocumentAccess> findAllByResourceId(Long resourceId);
}
