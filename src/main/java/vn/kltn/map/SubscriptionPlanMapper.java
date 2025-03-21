package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import vn.kltn.dto.request.SubscriptionPlanRequest;
import vn.kltn.dto.response.SubscriptionPlanResponse;
import vn.kltn.entity.SubscriptionPlan;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface SubscriptionPlanMapper {
    SubscriptionPlanResponse entityMapToResponse(SubscriptionPlan subscriptionPlan);

    SubscriptionPlan requestToEntity(SubscriptionPlanRequest request);

    void updateFromRequest(SubscriptionPlanRequest request, @MappingTarget SubscriptionPlan subscriptionPlan);
}
