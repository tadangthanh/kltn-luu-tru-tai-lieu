package vn.kltn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.kltn.entity.SubscriptionPlan;

import java.math.BigDecimal;

@Repository
public interface SubscriptionPlanRepo extends JpaRepository<SubscriptionPlan, Long> {
    boolean existsByName(String name);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM SubscriptionPlan s WHERE" +
            " s.maxReposPerMember = ?1 AND s.maxMembersPerRepo = ?2 AND s.maxStorage = ?3 AND s.price = ?4")
    boolean existsPlanLimit(int maxReposPerMember, int maxMembersPerRepo, Long maxStorage, BigDecimal price);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM SubscriptionPlan s WHERE" +
            " lower(s.name) = lower(?1)")
    boolean existsPlanByName(String name);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM SubscriptionPlan s WHERE" +
            " lower(s.name) = lower(?1) and s.id <> ?2")
    boolean existsPlanByNameExceptId(String name, Long id);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM SubscriptionPlan s WHERE" +
            " s.maxReposPerMember = ?1 AND s.maxMembersPerRepo = ?2 AND s.maxStorage = ?3 AND s.price = ?4 and s.id <> ?5")
    boolean existPlanLimitExceptId(int maxReposPerMember, int maxMembersPerRepo, Long maxStorage, BigDecimal price, Long id);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END FROM SubscriptionPlan s WHERE s.id = ?1 and s.isDeleted = true")
    boolean existsByIdAndDeletedTrue(Long planId);
}
