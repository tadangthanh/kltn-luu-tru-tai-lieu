package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.UserHasRole;
@Repository
public interface UserHasRoleRepo extends JpaRepository<UserHasRole,Long> {
}
