package vn.kltn.service;

import org.springframework.data.domain.Pageable;
import vn.kltn.dto.request.SubscriptionPlanRequest;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.SubscriptionPlanResponse;
import vn.kltn.entity.SubscriptionPlan;

import java.util.List;

public interface ISubscriptionPlanService {
    SubscriptionPlanResponse createSubscriptionPlan(SubscriptionPlanRequest request);

    SubscriptionPlanResponse updateSubscriptionPlan(Long planId, SubscriptionPlanRequest request);

    PageResponse<List<SubscriptionPlanResponse>> getPage(Pageable pageable);

    SubscriptionPlan getSubscriptionPlanByIdOrThrow(Long planId);
}
