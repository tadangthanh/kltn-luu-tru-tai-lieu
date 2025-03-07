package vn.kltn.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.UserHasKey;

@Repository
public interface UserHasKeyRepo extends JpaRepository<UserHasKey, Long> {
    @Transactional
    @Modifying
    @Query("update UserHasKey u set u.isActive = false where u.user.id = ?1")
    void disableAllKeyByUserId(Long userId);
}
