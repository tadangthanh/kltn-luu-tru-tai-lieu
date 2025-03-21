package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.UserSubscription;

@Repository
public interface UserSubscriptionRepo extends JpaRepository<UserSubscription, Long> {
}
