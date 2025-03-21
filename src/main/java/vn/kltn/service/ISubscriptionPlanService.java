package vn.kltn.service;

import vn.kltn.dto.request.SubscriptionPlanRequest;
import vn.kltn.dto.response.SubscriptionPlanResponse;
import vn.kltn.entity.SubscriptionPlan;

public interface ISubscriptionPlanService {
    SubscriptionPlanResponse createSubscriptionPlan(SubscriptionPlanRequest request);

    SubscriptionPlanResponse updateSubscriptionPlan(Long planId, SubscriptionPlanRequest request);

    SubscriptionPlan getSubscriptionPlanByIdOrThrow(Long planId);
}
