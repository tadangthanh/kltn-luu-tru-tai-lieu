package vn.kltn.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.kltn.dto.request.SubscriptionPlanRequest;
import vn.kltn.dto.response.PageResponse;
import vn.kltn.dto.response.ResponseData;
import vn.kltn.dto.response.SubscriptionPlanResponse;
import vn.kltn.service.ISubscriptionPlanService;

import java.util.List;

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

    @DeleteMapping("/{planId}")
    public ResponseData<SubscriptionPlanResponse> softDeleteSubscriptionPlan(@PathVariable Long planId) {
        return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "Xóa thành công",
                subscriptionPlanService.softDeleteSubscriptionPlan(planId));
    }

    @GetMapping
    public ResponseData<PageResponse<List<SubscriptionPlanResponse>>> getPagePlan(Pageable pageable) {
        return new ResponseData<>(HttpStatus.OK.value(), "thành công",
                subscriptionPlanService.getPage(pageable));
    }
}
