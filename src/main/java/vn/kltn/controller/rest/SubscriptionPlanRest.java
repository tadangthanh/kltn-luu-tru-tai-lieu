package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.SubscriptionPlanRequest;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.dto.response.SubscriptionPlanResponse;
import vn.kltn.service.ISubscriptionPlanService;

@RequiredArgsConstructor
@RequestMapping("/api/v1/subscription-plan")
@RestController
@Validated
public class SubscriptionPlanRest {
    private final ISubscriptionPlanService subscriptionPlanService;

    @PostMapping
    public ResponseData<SubscriptionPlanResponse> createSubscriptionPlan(@Valid @RequestBody SubscriptionPlanRequest subscriptionPlanRequest) {
        return new ResponseData<>(HttpStatus.CREATED.value(), "Thành công",
                subscriptionPlanService.createSubscriptionPlan(subscriptionPlanRequest));
    }

    @PatchMapping("/{planId}")
    public ResponseData<SubscriptionPlanResponse> updateSubscriptionPlan(@PathVariable Long planId,
                                                                         @Valid @RequestBody SubscriptionPlanRequest subscriptionPlanRequest) {
        return new ResponseData<>(HttpStatus.OK.value(), "Cập nhật thành công",
                subscriptionPlanService.updateSubscriptionPlan(planId, subscriptionPlanRequest));
    }

}
