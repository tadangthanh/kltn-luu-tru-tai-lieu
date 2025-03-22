package vn.kltn.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.kltn.dto.request.SubscriptionPlanRequest;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.SubscriptionPlanResponse;
import vn.kltn.entity.Role;
import vn.kltn.entity.SubscriptionPlan;
import vn.kltn.entity.User;
import vn.kltn.exception.ConflictResourceException;
import vn.kltn.exception.DuplicateResourceException;
import vn.kltn.exception.ResourceNotFoundException;
import vn.kltn.map.SubscriptionPlanMapper;
import vn.kltn.repository.SubscriptionPlanRepo;
import vn.kltn.repository.util.PaginationUtils;
import vn.kltn.service.IAuthenticationService;
import vn.kltn.service.ISubscriptionPlanService;

import java.util.List;

@Service
@Transactional
@Slf4j(topic = "SUBSCRIPTION_PLAN_SERVICE")
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements ISubscriptionPlanService {
    private final SubscriptionPlanMapper subscriptionPlanMapper;
    private final SubscriptionPlanRepo planRepo;
    private final IAuthenticationService authenticationService;

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

    @Override
    public PageResponse<List<SubscriptionPlanResponse>> getPage(Pageable pageable) {
        Page<SubscriptionPlan> planPage = planRepo.findAll(pageable);
        return PaginationUtils.convertToPageResponse(planPage, pageable, this::mapToResponse);
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

    @Override
    public SubscriptionPlanResponse softDeleteSubscriptionPlan(Long planId) {
        validatePlanNotDeleted(planId);
        SubscriptionPlan subscriptionPlan = getSubscriptionPlanByIdOrThrow(planId);
        subscriptionPlan.setDeleted(true);
        return mapToResponse(planRepo.save(subscriptionPlan));
    }

    private void validatePlanNotDeleted(Long planId) {
        if (planRepo.existsByIdAndDeletedTrue(planId)) {
            log.error("Gói plan đã bị xóa");
            throw new ConflictResourceException("Gói plan đã bị xóa");
        }
    }
}
