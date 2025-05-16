package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.SharedLink;

import java.util.Optional;

@Repository
public interface SharedLinkRepo extends JpaRepository<SharedLink, Long> {
    Optional<SharedLink> findByAccessToken(String accessToken);
}
