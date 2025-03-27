package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.AccessResourceResponse;
import vn.kltn.entity.AccessResource;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface AccessResourceMapper {
    @Mapping(target = "recipientName", source = "recipient.fullName")
    @Mapping(target = "recipientEmail", source = "recipient.email")
    AccessResourceResponse toAccessResourceResponse(AccessResource accessResource);

}
