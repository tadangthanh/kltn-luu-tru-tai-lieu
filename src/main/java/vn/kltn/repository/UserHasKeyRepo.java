package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.UserHasKey;

@Repository
public interface UserHasKeyRepo extends JpaRepository<UserHasKey, Long> {

}
