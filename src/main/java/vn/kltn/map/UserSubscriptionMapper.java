package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.UserSubscriptionResponse;
import vn.kltn.entity.UserSubscription;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface UserSubscriptionMapper {
    @Mapping(target = "subscription", source = "plan")
    UserSubscriptionResponse entityToResponse(UserSubscription entity);
}
