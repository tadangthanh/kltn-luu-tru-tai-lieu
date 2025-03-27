package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.FolderAccess;

@Repository
public interface FolderAccessRepo extends JpaRepository<FolderAccess, Long>, JpaSpecificationExecutor<FolderAccess> {

}
