package vn.kltn.map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.kltn.dto.request.ItemRequest;
import vn.kltn.dto.response.ItemResponse;
import vn.kltn.entity.Item;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface ItemMapper {
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "ownerName", source = "owner.fullName")
    @Mapping(target = "ownerEmail", source = "owner.email")
    @Mapping(target = "itemType", source = "itemType")
    ItemResponse toResponse(Item item);

    void updateItem(@MappingTarget Item item, ItemRequest itemRequest);

    List<ItemResponse> toResponse(List<Item> items);
}
