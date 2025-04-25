package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.entity.Item;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface ItemMapper {
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "ownerName", source = "owner.fullName")
    @Mapping(target = "ownerEmail", source = "owner.email")
    @Mapping(target = "itemType", source = "itemType")
    ItemResponse toResponse(Item item);
}
