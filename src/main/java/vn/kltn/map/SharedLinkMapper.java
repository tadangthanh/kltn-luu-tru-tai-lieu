package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.kltn.dto.request.CreateSharedLinkRequest;
import vn.kltn.dto.response.SharedLinkResponse;
import vn.kltn.entity.SharedLink;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface SharedLinkMapper {
    @Mapping(target = "itemId", source = "item.id")
    SharedLinkResponse toResponse(SharedLink sharedLink);

    SharedLink toEntity(CreateSharedLinkRequest createSharedLinkRequest);

    void updateSharedLink(CreateSharedLinkRequest createSharedLinkRequest,@MappingTarget SharedLink sharedLink);
}
