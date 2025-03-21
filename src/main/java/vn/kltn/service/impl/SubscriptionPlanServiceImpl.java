package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.SubscriptionPlanRequest;
import vn.kltn.dto.response.SubscriptionPlanResponse;
import vn.kltn.entity.SubscriptionPlan;
import vn.kltn.exception.DuplicateResourceException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.SubscriptionPlanMapper;
import vn.kltn.repository.SubscriptionPlanRepo;
import vn.kltn.service.ISubscriptionPlanService;

@Service
@Transactional
@Slf4j(topic = "SUBSCRIPTION_PLAN_SERVICE")
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements ISubscriptionPlanService {
    private final SubscriptionPlanMapper subscriptionPlanMapper;
    private final SubscriptionPlanRepo planRepo;

    @Override
    public SubscriptionPlanResponse createSubscriptionPlan(SubscriptionPlanRequest request) {
        validatePlanNotExist(request);
        SubscriptionPlan subscriptionPlan = mapToEntity(request);
        SubscriptionPlan savedPlan = planRepo.save(subscriptionPlan);
        return mapToResponse(savedPlan);
    }

    private SubscriptionPlanResponse mapToResponse(SubscriptionPlan subscriptionPlan) {
        return subscriptionPlanMapper.entityMapToResponse(subscriptionPlan);
    }

    private SubscriptionPlan mapToEntity(SubscriptionPlanRequest request) {
        return subscriptionPlanMapper.requestToEntity(request);
    }

    // plan tồn tại khi name, maxReposPerMember, maxMembersPerRepo, maxStorage, price giống nhau
    private void validatePlanNotExist(SubscriptionPlanRequest request) {
        validatePlanNameNotExist(request.getName());
        validateLimitsPlanNotExist(request);
    }

    private void validateLimitsPlanNotExist(SubscriptionPlanRequest request) {
        if (planRepo.existsPlanLimit(request.getMaxReposPerMember(), request.getMaxMembersPerRepo(), request.getMaxStorage(), request.getPrice())) {
            log.error("Gói plan đã tồn tại");
            throw new DuplicateResourceException("Gói plan đã tồn tại");
        }
    }

    private void validatePlanNameNotExist(String name) {
        if (planRepo.existsPlanByName(name)) {
            log.error("Gói plan đã tồn tại");
            throw new DuplicateResourceException("Gói plan đã tồn tại");
        }
    }

    @Override
    public SubscriptionPlanResponse updateSubscriptionPlan(Long planId, SubscriptionPlanRequest request) {
        SubscriptionPlan subscriptionPlanExist = getSubscriptionPlanByIdOrThrow(planId);
        validatePlanNotExistExceptId(request, planId);
        subscriptionPlanMapper.updateFromRequest(request, subscriptionPlanExist);
        subscriptionPlanExist = planRepo.save(subscriptionPlanExist);
        return mapToResponse(subscriptionPlanExist);
    }

    private void validatePlanNotExistExceptId(SubscriptionPlanRequest request, Long planId) {
        validatePlanNameNotExistExceptId(request.getName(), planId);
        validateLimitsPlanNotExistExceptId(request, planId);
    }
    private void validatePlanNameNotExistExceptId(String name, Long id) {
        if (planRepo.existsPlanByNameExceptId(name, id)) {
            log.error("Gói plan đã tồn tại");
            throw new DuplicateResourceException("Gói plan đã tồn tại");
        }
    }
    private void validateLimitsPlanNotExistExceptId(SubscriptionPlanRequest request, Long id) {
        if (planRepo.existPlanLimitExceptId(request.getMaxReposPerMember(), request.getMaxMembersPerRepo(), request.getMaxStorage(), request.getPrice(), id)) {
            log.error("Gói plan đã tồn tại");
            throw new DuplicateResourceException("Gói plan đã tồn tại");
        }
    }

    @Override
    public SubscriptionPlan getSubscriptionPlanByIdOrThrow(Long planId) {
        return planRepo.findById(planId).orElseThrow(() -> {
            log.warn("Không tìm thấy gói plan với id: {}", planId);
            return new ResourceNotFoundException("Không tìm thấy gói plan với id: " + planId);
        });
    }
}
